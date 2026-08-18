package com.gaguraczi.paw.domain.rag.support;

import com.gaguraczi.paw.domain.rag.dto.RagSearchHit;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OpenAiVectorStoreHitMapper {

    private static final String FIELD_START = "(?:^|\\|\\s*)";
    private static final Pattern SOURCE_ID = Pattern.compile(FIELD_START + "source_id:\\s*([^|\\n]+)", Pattern.MULTILINE);
    private static final Pattern CHUNK = Pattern.compile(FIELD_START + "chunk:\\s*(\\d+)", Pattern.MULTILINE);
    private static final Pattern TYPE = Pattern.compile(FIELD_START + "type:\\s*(QA|CORPUS)", Pattern.MULTILINE);
    private static final Pattern DEPARTMENT = Pattern.compile(FIELD_START + "과목:\\s*([^|\\n]+)", Pattern.MULTILINE);
    private static final Pattern LIFE_CYCLE = Pattern.compile(FIELD_START + "생애주기:\\s*([^|\\n]+)", Pattern.MULTILINE);
    private static final Pattern DISEASE = Pattern.compile(FIELD_START + "질환:\\s*([^|\\n]+)", Pattern.MULTILINE);
    private static final Pattern TITLE = Pattern.compile(FIELD_START + "제목:\\s*([^|\\n]+)", Pattern.MULTILINE);

    private OpenAiVectorStoreHitMapper() {
    }

    public static RagSearchHit map(String fileId, String fileName, String text, double score) {
        String content = text == null ? "" : text;
        String sourceId = firstGroup(SOURCE_ID, content);
        Integer chunkIndex = parseInt(firstGroup(CHUNK, content));
        RagSourceType sourceType = parseSourceType(firstGroup(TYPE, content));
        String department = firstGroup(DEPARTMENT, content);
        String lifeCycle = firstGroup(LIFE_CYCLE, content);
        String disease = firstGroup(DISEASE, content);
        String title = firstGroup(TITLE, content);

        FilenameMeta fileMeta = parseFilename(fileName);
        if (sourceType == null) {
            sourceType = fileMeta.sourceType();
        }
        if (department == null) {
            department = fileMeta.department();
        }
        return new RagSearchHit(
                fileId,
                fileName,
                sourceId,
                chunkIndex,
                sourceType,
                department,
                lifeCycle,
                disease,
                title,
                content,
                score
        );
    }

    public static boolean matches(
            RagSearchHit hit,
            RagSourceType sourceType,
            String department,
            String lifeCycle
    ) {
        if (sourceType != null && hit.sourceType() != sourceType) {
            return false;
        }
        if (department != null && !department.equals(hit.department())) {
            return false;
        }
        return lifeCycle == null || lifeCycle.equals(hit.lifeCycle());
    }

    static FilenameMeta parseFilename(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return FilenameMeta.empty();
        }
        String stem = fileName;
        int slash = Math.max(stem.lastIndexOf('/'), stem.lastIndexOf('\\'));
        if (slash >= 0) {
            stem = stem.substring(slash + 1);
        }
        int dot = stem.lastIndexOf('.');
        if (dot > 0) {
            stem = stem.substring(0, dot);
        }
        String[] parts = stem.split("_");
        if (parts.length < 2) {
            return FilenameMeta.empty();
        }
        RagSourceType sourceType = parseSourceType(parts[parts.length - 2]);
        if (sourceType == null) {
            return FilenameMeta.empty();
        }
        String department = String.join("_", java.util.Arrays.copyOf(parts, parts.length - 2));
        return new FilenameMeta(department.isBlank() ? null : department, sourceType);
    }

    private static RagSourceType parseSourceType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RagSourceType.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String firstGroup(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1).trim();
        return value.isEmpty() ? null : value;
    }

    record FilenameMeta(String department, RagSourceType sourceType) {
        static FilenameMeta empty() {
            return new FilenameMeta(null, null);
        }
    }
}
