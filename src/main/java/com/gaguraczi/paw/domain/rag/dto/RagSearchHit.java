package com.gaguraczi.paw.domain.rag.dto;

import com.gaguraczi.paw.domain.rag.enums.RagSourceType;

public record RagSearchHit(
        String fileId,
        String fileName,
        String sourceId,
        Integer chunkIndex,
        RagSourceType sourceType,
        String department,
        String lifeCycle,
        String disease,
        String title,
        String content,
        double score
) {
}
