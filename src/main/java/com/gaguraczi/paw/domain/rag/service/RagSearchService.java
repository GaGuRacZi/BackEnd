package com.gaguraczi.paw.domain.rag.service;

import com.gaguraczi.paw.domain.rag.client.OpenAiVectorStoreClient;
import com.gaguraczi.paw.domain.rag.config.RagProperties;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.dto.RagSearchQuery;
import com.gaguraczi.paw.domain.rag.exception.code.RagErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagSearchService {

    public static final int MAX_RESULTS = 50;

    private final OpenAiVectorStoreClient openAiVectorStoreClient;
    private final RagProperties ragProperties;

    public RagAskResult ask(RagSearchQuery query) {
        if (query == null || query.query() == null || query.query().isBlank()) {
            throw GeneralException.of(RagErrorCode.RAG_QUERY_REQUIRED);
        }
        String vectorStoreId = ragProperties.getVectorStoreId();
        if (vectorStoreId == null || vectorStoreId.isBlank()) {
            log.warn("OpenAI vector store ID is missing. Set OPENAI_VECTOR_STORE_ID.");
            throw GeneralException.of(RagErrorCode.RAG_VECTOR_STORE_MISSING);
        }
        int topK = query.topK() == null || query.topK() <= 0
                ? ragProperties.getSearchTopK()
                : Math.min(query.topK(), MAX_RESULTS);
        return openAiVectorStoreClient.ask(
                vectorStoreId,
                ragProperties.getChatModel(),
                ragProperties.getReasoningEffort(),
                buildInput(query),
                topK
        );
    }

    static String buildInput(RagSearchQuery query) {
        StringBuilder input = new StringBuilder(query.query().trim());
        if (notBlank(query.department())) {
            input.append("\n과목: ").append(query.department().trim());
        }
        if (notBlank(query.lifeCycle())) {
            input.append("\n생애주기: ").append(query.lifeCycle().trim());
        }
        if (query.sourceType() != null) {
            input.append("\n자료유형: ").append(query.sourceType().name());
        }
        return input.toString();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
