package com.gaguraczi.paw.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiEmbeddingModelValidator {

    public OpenAiEmbeddingModelValidator(
            @Value("${spring.ai.openai.embedding.model:}") String embeddingModel
    ) {
        if (embeddingModel == null || embeddingModel.isBlank()) {
            throw new IllegalStateException("spring.ai.openai.embedding.model must not be blank");
        }
    }
}
