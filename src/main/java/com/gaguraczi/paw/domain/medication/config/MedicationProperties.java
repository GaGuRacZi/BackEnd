package com.gaguraczi.paw.domain.medication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "paw.medication")
public class MedicationProperties {

    private int ingestBatchSize = 8;
    private int embeddingDimensions = 1536;
    private int searchTopK = 10;
    private Integer limit;
    private String chatModel = "gpt-5.6-luna";
    private String reasoningEffort = "none";

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
