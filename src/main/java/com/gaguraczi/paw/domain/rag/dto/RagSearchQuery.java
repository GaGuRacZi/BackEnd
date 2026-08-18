package com.gaguraczi.paw.domain.rag.dto;

import com.gaguraczi.paw.domain.rag.enums.RagSourceType;

public record RagSearchQuery(
        String query,
        Integer topK,
        RagSourceType sourceType,
        String department,
        String lifeCycle
) {
}
