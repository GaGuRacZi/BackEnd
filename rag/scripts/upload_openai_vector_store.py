#!/usr/bin/env python3
"""Pack local rag_document rows and upload them to an OpenAI vector store.

OpenAI vector stores accept files, not precomputed pgvector embeddings.
This script groups already-chunked rows into markdown files, then lets OpenAI
re-chunk and embed them for file_search.
"""

from __future__ import annotations

import argparse
import json
import sys
import time
from collections import defaultdict
from pathlib import Path

from openai import OpenAI

ROOT = Path(__file__).resolve().parents[2]
EXPORT_DIR = ROOT / "rag" / "openai-export"
PACK_DIR = EXPORT_DIR / "files"
JSONL_PATH = EXPORT_DIR / "rag_document.jsonl"
ENV_PATH = ROOT / ".env"
STORE_NAME = "paw-rag-knowledge"
CHUNKS_PER_FILE = 250
DB_URL_KEY = "LOCAL_DB_URL"
DB_USER_KEY = "LOCAL_DB_USERNAME"
DB_PASSWORD_KEY = "LOCAL_DB_PASSWORD"


def load_env() -> dict[str, str]:
    values: dict[str, str] = {}
    for line in ENV_PATH.read_text(encoding="utf-8").splitlines():
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key] = value.strip().strip('"').strip("'")
    return values


def load_api_key(env: dict[str, str] | None = None) -> str:
    values = env or load_env()
    key = values.get("OPENAI_API_KEY")
    if not key:
        raise SystemExit("OPENAI_API_KEY not found in .env")
    return key


def jdbc_to_dsn(env: dict[str, str]) -> str:
    url = env.get(DB_URL_KEY, "")
    if not url.startswith("jdbc:postgresql://"):
        raise SystemExit(f"{DB_URL_KEY} is not a postgres jdbc url")
    host_db = url.removeprefix("jdbc:postgresql://")
    return f"postgresql://{env[DB_USER_KEY]}:{env[DB_PASSWORD_KEY]}@{host_db}"


def export_rows(env: dict[str, str], path: Path) -> int:
    import psycopg

    path.parent.mkdir(parents=True, exist_ok=True)
    sql = """
        SELECT source_id, chunk_index, source_type,
               COALESCE(department, '') AS department,
               COALESCE(life_cycle, '') AS life_cycle,
               COALESCE(disease, '') AS disease,
               COALESCE(title, '') AS title,
               content
        FROM rag_document
        ORDER BY source_type, department, source_id, chunk_index
    """
    count = 0
    with psycopg.connect(jdbc_to_dsn(env)) as conn, path.open("w", encoding="utf-8") as handle:
        with conn.cursor(name="rag_export") as cursor:
            cursor.itersize = 1000
            cursor.execute(sql)
            for row in cursor:
                payload = {
                    "source_id": row[0],
                    "chunk_index": row[1],
                    "source_type": row[2],
                    "department": row[3],
                    "life_cycle": row[4],
                    "disease": row[5],
                    "title": row[6],
                    "content": row[7],
                }
                handle.write(json.dumps(payload, ensure_ascii=False) + "\n")
                count += 1
    return count


def load_rows(path: Path) -> list[dict]:
    rows = []
    with path.open(encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def group_key(row: dict) -> tuple[str, str]:
    department = row.get("department") or "미분류"
    source_type = row.get("source_type") or "UNKNOWN"
    return department, source_type


def render_chunk(row: dict) -> str:
    bits = []
    if row.get("department"):
        bits.append(f"과목: {row['department']}")
    if row.get("life_cycle"):
        bits.append(f"생애주기: {row['life_cycle']}")
    if row.get("disease") and row["disease"] != "기타":
        bits.append(f"질환: {row['disease']}")
    if row.get("title"):
        bits.append(f"제목: {row['title']}")
    header = " | ".join(bits)
    meta = (
        f"source_id: {row.get('source_id', '')} | "
        f"chunk: {row.get('chunk_index', 0)} | "
        f"type: {row.get('source_type', '')}"
    )
    body = (row.get("content") or "").strip()
    parts = [meta]
    if header:
        parts.append(header)
    parts.append("")
    parts.append(body)
    return "\n".join(parts)


def pack_files(rows: list[dict], out_dir: Path, chunks_per_file: int) -> list[Path]:
    out_dir.mkdir(parents=True, exist_ok=True)
    for old in out_dir.glob("*.md"):
        old.unlink()

    grouped: dict[tuple[str, str], list[dict]] = defaultdict(list)
    for row in rows:
        grouped[group_key(row)].append(row)

    written: list[Path] = []
    for (department, source_type), group in sorted(grouped.items()):
        for start in range(0, len(group), chunks_per_file):
            batch = group[start : start + chunks_per_file]
            index = start // chunks_per_file
            name = f"{department}_{source_type}_{index:03d}.md"
            path = out_dir / name
            sections = [render_chunk(row) for row in batch]
            path.write_text("\n\n---\n\n".join(sections) + "\n", encoding="utf-8")
            written.append(path)
    return written


def find_store(client: OpenAI, name: str):
    after = None
    while True:
        page = client.vector_stores.list(limit=100, after=after) if after else client.vector_stores.list(limit=100)
        for store in page.data:
            if store.name == name:
                return store
        if not page.has_more or not page.data:
            return None
        after = page.data[-1].id


def list_store_file_ids(client: OpenAI, store_id: str) -> list[str]:
    ids: list[str] = []
    after = None
    while True:
        page = (
            client.vector_stores.files.list(vector_store_id=store_id, limit=100, after=after)
            if after
            else client.vector_stores.files.list(vector_store_id=store_id, limit=100)
        )
        ids.extend(file.id for file in page.data)
        if not page.has_more or not page.data:
            return ids
        after = page.data[-1].id


def clear_store_files(client: OpenAI, store_id: str) -> int:
    file_ids = list_store_file_ids(client, store_id)
    for file_id in file_ids:
        client.vector_stores.files.delete(vector_store_id=store_id, file_id=file_id)
    return len(file_ids)


def failed_file_count(file_counts) -> int:
    failed = getattr(file_counts, "failed", None)
    if failed is None and isinstance(file_counts, dict):
        failed = file_counts.get("failed")
    return int(failed or 0)


def upload(client: OpenAI, files: list[Path], store_name: str) -> str:
    store = find_store(client, store_name)
    if store is None:
        store = client.vector_stores.create(
            name=store_name,
            metadata={
                "source": "rag_document",
                "corpus": "dog-growth-disease",
            },
        )
        print(f"created vector store {store.id} name={store.name}")
    else:
        deleted = clear_store_files(client, store.id)
        print(f"reusing vector store {store.id} name={store.name} cleared_files={deleted}")

    handles = [path.open("rb") for path in files]
    try:
        batch = client.vector_stores.file_batches.upload_and_poll(
            vector_store_id=store.id,
            files=handles,
            chunking_strategy={
                "type": "static",
                "static": {
                    "max_chunk_size_tokens": 1200,
                    "chunk_overlap_tokens": 200,
                },
            },
        )
    finally:
        for handle in handles:
            handle.close()

    print(
        "batch "
        f"id={batch.id} status={batch.status} "
        f"counts={batch.file_counts.model_dump() if hasattr(batch.file_counts, 'model_dump') else batch.file_counts}"
    )
    if batch.status != "completed" or failed_file_count(batch.file_counts) != 0:
        raise SystemExit(
            f"vector store batch did not complete: status={batch.status} "
            f"failed={failed_file_count(batch.file_counts)}"
        )

    refreshed = client.vector_stores.retrieve(store.id)
    deadline = time.time() + 30 * 60
    while refreshed.status == "in_progress":
        if time.time() >= deadline:
            raise SystemExit(f"vector store status poll timed out: {refreshed.status}")
        time.sleep(5)
        refreshed = client.vector_stores.retrieve(store.id)
        print(f"store status={refreshed.status} files={refreshed.file_counts}")
    if refreshed.status != "completed":
        raise SystemExit(f"vector store did not complete: {refreshed.status}")
    print(
        f"ready id={refreshed.id} status={refreshed.status} "
        f"usage_bytes={refreshed.usage_bytes} files={refreshed.file_counts}"
    )
    (EXPORT_DIR / "vector_store.json").write_text(
        json.dumps(
            {
                "id": refreshed.id,
                "name": refreshed.name,
                "status": refreshed.status,
                "usage_bytes": refreshed.usage_bytes,
                "file_counts": (
                    refreshed.file_counts.model_dump()
                    if hasattr(refreshed.file_counts, "model_dump")
                    else str(refreshed.file_counts)
                ),
            },
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    return refreshed.id


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--pack-only", action="store_true")
    parser.add_argument("--chunks-per-file", type=int, default=CHUNKS_PER_FILE)
    parser.add_argument("--name", default=STORE_NAME)
    args = parser.parse_args()

    env = load_env()
    exported = export_rows(env, JSONL_PATH)
    print(f"exported rows={exported} path={JSONL_PATH}")
    rows = load_rows(JSONL_PATH)
    files = pack_files(rows, PACK_DIR, args.chunks_per_file)
    print(f"packed rows={len(rows)} files={len(files)} dir={PACK_DIR}")
    if args.pack_only:
        return 0

    client = OpenAI(api_key=load_api_key(env))
    store_id = upload(client, files, args.name)
    print(store_id)
    return 0


if __name__ == "__main__":
    sys.exit(main())
