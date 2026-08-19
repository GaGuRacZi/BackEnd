package com.gaguraczi.paw.domain.visit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "paw.visit")
public class VisitProperties {

    private String sttModel = "gpt-4o-transcribe-diarize";
    private String chatModel = "gpt-5.6-luna";
    private String aiSummaryChatModel = "gpt-5.6-luna";
    private String reasoningEffort = "none";
    private int aiSummaryCoinCost = 1;
    private long maxAudioBytes = 104857600L;
    private int maxAudioDurationSec = 3600;
    private int aiSummaryMinChars = 1000;
    private int aiSummaryMaxChars = 1500;
    private boolean aiSummaryIncludeSources = true;

    public void setSttModel(String sttModel) {
        if (sttModel != null && !sttModel.isBlank()) {
            this.sttModel = sttModel;
        }
    }

    public void setChatModel(String chatModel) {
        if (chatModel != null && !chatModel.isBlank()) {
            this.chatModel = chatModel;
        }
    }

    public String getAiSummaryChatModel() {
        if (aiSummaryChatModel == null || aiSummaryChatModel.isBlank()) {
            return chatModel;
        }
        return aiSummaryChatModel;
    }

    public void setAiSummaryChatModel(String aiSummaryChatModel) {
        if (aiSummaryChatModel != null && !aiSummaryChatModel.isBlank()) {
            this.aiSummaryChatModel = aiSummaryChatModel;
        }
    }

    public void setReasoningEffort(String reasoningEffort) {
        if (reasoningEffort != null && !reasoningEffort.isBlank()) {
            this.reasoningEffort = reasoningEffort;
        }
    }
}
