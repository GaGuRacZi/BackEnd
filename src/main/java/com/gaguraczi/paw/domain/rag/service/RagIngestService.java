package com.gaguraczi.paw.domain.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.rag.chunking.CharacterTextSplitter;
import com.gaguraczi.paw.domain.rag.config.RagProperties;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.rag.exception.code.RagErrorCode;
import com.gaguraczi.paw.domain.rag.model.RagChunk;
import com.gaguraczi.paw.domain.rag.parser.QaCorpusParser;
import com.gaguraczi.paw.domain.rag.parser.SourceCorpusParser;
import com.gaguraczi.paw.domain.rag.repository.RagDocumentJdbcRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@Profile("rag-ingest")
public class RagIngestService {

    private static final String QA_PATH_MARKER = "TL_질의응답";
    private static final String CORPUS_PATH_MARKER = "TS_말뭉치";

    private final RagProperties ragProperties;
    private final RagDocumentJdbcRepository ragDocumentJdbcRepository;
    private final EmbeddingModel embeddingModel;
    private final QaCorpusParser qaCorpusParser;
    private final SourceCorpusParser sourceCorpusParser;

    public RagIngestService(
            RagProperties ragProperties,
            RagDocumentJdbcRepository ragDocumentJdbcRepository,
            EmbeddingModel embeddingModel,
            ObjectMapper objectMapper
    ) {
        this.ragProperties = ragProperties;
        this.ragDocumentJdbcRepository = ragDocumentJdbcRepository;
        this.embeddingModel = embeddingModel;
        this.qaCorpusParser = new QaCorpusParser(objectMapper);
        this.sourceCorpusParser = new SourceCorpusParser(
                objectMapper,
                new CharacterTextSplitter(
                        ragProperties.getChunkSizeChars(),
                        ragProperties.getChunkOverlapChars()
                )
        );
    }

    public IngestResult ingest() {
        Path trainingDir = resolveTrainingDir(Path.of(ragProperties.getCorpusPath()));
        List<Path> files = listJsonFiles(trainingDir);
        Integer limit = ragProperties.getLimit();
        int batchSize = Math.max(1, ragProperties.getIngestBatchSize());

        List<RagChunk> buffer = new ArrayList<>(batchSize);
        int processed = 0;
        int skipped = 0;
        int failed = 0;
        int filesAccepted = 0;

        for (Path file : files) {
            if (limit != null && filesAccepted >= limit) {
                break;
            }
            try {
                List<RagChunk> chunks = parseFile(file).stream()
                        .filter(this::matchesDepartment)
                        .toList();
                if (chunks.isEmpty()) {
                    continue;
                }
                filesAccepted++;
                for (RagChunk chunk : chunks) {
                    buffer.add(chunk);
                    if (buffer.size() >= batchSize) {
                        try {
                            FlushResult flush = flush(buffer);
                            processed += flush.processed();
                            skipped += flush.skipped();
                            if (processed > 0 && processed % 512 == 0) {
                                log.info("RAG ingest progress processed={} skipped={} failed={}",
                                        processed, skipped, failed);
                            }
                        } finally {
                            buffer.clear();
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof GeneralException ge
                        && ge.getCode() == RagErrorCode.RAG_EMBEDDING_FAILED) {
                    log.error("OpenAI embedding failed. stopping ingest. file={}", file, e);
                    throw ge;
                }
                failed++;
                log.warn("RAG ingest skipped file: {} ({})", file, e.toString());
            }
        }
        if (!buffer.isEmpty()) {
            try {
                FlushResult flush = flush(buffer);
                processed += flush.processed();
                skipped += flush.skipped();
            } finally {
                buffer.clear();
            }
        }

        log.info("RAG ingest files accepted={}", filesAccepted);
        return new IngestResult(processed, skipped, failed, filesAccepted);
    }

    private List<RagChunk> parseFile(Path file) throws IOException {
        return switch (classifyCorpusPath(file)) {
            case QA -> qaCorpusParser.parseToList(file);
            case CORPUS -> sourceCorpusParser.parse(file);
            case NONE -> List.of();
        };
    }

    private boolean matchesDepartment(RagChunk chunk) {
        String filter = ragProperties.getDepartment();
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return filter.equals(chunk.department());
    }

    private FlushResult flush(List<RagChunk> chunks) {
        List<String> sourceIds = chunks.stream().map(RagChunk::sourceId).distinct().toList();
        Map<String, String> existing = ragDocumentJdbcRepository.findHashes(sourceIds);

        List<RagChunk> toEmbed = new ArrayList<>();
        List<String> hashes = new ArrayList<>();
        int skipped = 0;
        for (RagChunk chunk : chunks) {
            String hash = sha256(chunk.content());
            String previous = existing.get(chunk.sourceId() + ":" + chunk.chunkIndex());
            if (!ragProperties.isForceReembed() && hash.equals(previous)) {
                skipped++;
                continue;
            }
            toEmbed.add(chunk);
            hashes.add(hash);
        }
        if (toEmbed.isEmpty()) {
            return new FlushResult(0, skipped);
        }

        List<float[]> embeddings;
        try {
            embeddings = embeddingModel.embed(toEmbed.stream().map(RagChunk::content).toList());
        } catch (RuntimeException e) {
            log.error("OpenAI embedding request failed: {}", rootMessage(e), e);
            throw GeneralException.of(RagErrorCode.RAG_EMBEDDING_FAILED, e);
        }
        if (embeddings.size() != toEmbed.size()) {
            throw GeneralException.of(RagErrorCode.RAG_EMBEDDING_FAILED);
        }

        int expectedDim = ragProperties.getEmbeddingDimensions();
        for (int i = 0; i < toEmbed.size(); i++) {
            float[] embedding = embeddings.get(i);
            if (embedding == null || embedding.length != expectedDim) {
                throw GeneralException.of(RagErrorCode.RAG_EMBEDDING_FAILED);
            }
        }
        ragDocumentJdbcRepository.upsertAll(toEmbed, hashes, embeddings);
        return new FlushResult(toEmbed.size(), skipped);
    }

    private Path resolveTrainingDir(Path root) {
        Path nested = root.resolve("3.개방데이터").resolve("1.데이터").resolve("Training");
        if (Files.isDirectory(nested)) {
            return nested;
        }
        if (Files.isDirectory(root.resolve("02.라벨링데이터"))
                || Files.isDirectory(root.resolve("01.원천데이터"))) {
            return root;
        }
        throw GeneralException.of(RagErrorCode.RAG_CORPUS_PATH_INVALID);
    }

    private List<Path> listJsonFiles(Path trainingDir) {
        try (Stream<Path> stream = Files.walk(trainingDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(this::matchesSourceTypePath)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw GeneralException.of(RagErrorCode.RAG_CORPUS_PATH_INVALID, e);
        }
    }

    private boolean matchesSourceTypePath(Path path) {
        CorpusKind kind = classifyCorpusPath(path);
        RagSourceType filter = ragProperties.getSourceType();
        if (filter == RagSourceType.QA) {
            return kind == CorpusKind.QA;
        }
        if (filter == RagSourceType.CORPUS) {
            return kind == CorpusKind.CORPUS;
        }
        return kind != CorpusKind.NONE;
    }

    private static CorpusKind classifyCorpusPath(Path path) {
        String value = path.toString();
        if (value.contains(QA_PATH_MARKER)) {
            return CorpusKind.QA;
        }
        if (value.contains(CORPUS_PATH_MARKER)) {
            return CorpusKind.CORPUS;
        }
        return CorpusKind.NONE;
    }

    private static String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }

    private static String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record IngestResult(int processed, int skipped, int failed, int files) {
    }

    private record FlushResult(int processed, int skipped) {
    }

    private enum CorpusKind {
        QA,
        CORPUS,
        NONE
    }
}
