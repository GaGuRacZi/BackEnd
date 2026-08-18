package com.gaguraczi.paw.domain.rag.model;

import com.gaguraczi.paw.domain.rag.enums.RagSourceType;

public record RagChunk(
        String sourceId,
        int chunkIndex,
        RagSourceType sourceType,
        String department,
        String lifeCycle,
        String disease,
        String title,
        String content
) {
}
