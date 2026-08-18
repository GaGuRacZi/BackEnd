package com.gaguraczi.paw.domain.rag.config;

import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "paw.rag")
public class RagProperties {

    private String corpusPath = "rag/data/59.반려견 성장 및 질병 관련 말뭉치 데이터";
    private int chunkSizeChars = 1000;
    private int chunkOverlapChars = 150;
    private int ingestBatchSize = 64;
    private int embeddingDimensions = 1536;
    private int searchTopK = 8;
    private String vectorStoreId;
    private String chatModel = "gpt-5.6-luna";
    private String reasoningEffort = "none";
    private Integer limit;
    private boolean forceReembed = false;
    private RagSourceType sourceType;
    private String department;

    public void setChatModel(String chatModel) {
        if (chatModel != null && !chatModel.isBlank()) {
            this.chatModel = chatModel;
        }
    }

    public void setReasoningEffort(String reasoningEffort) {
        if (reasoningEffort != null && !reasoningEffort.isBlank()) {
            this.reasoningEffort = reasoningEffort;
        }
    }
}
