package com.gaguraczi.paw.domain.rag.dto;

import java.util.List;

public record RagAskResult(String answer, List<RagSearchHit> sources) {
}
