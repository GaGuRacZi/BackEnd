package com.gaguraczi.paw.domain.visit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VisitSttModelValidator {

    public VisitSttModelValidator(@Value("${paw.visit.stt-model:}") String sttModel) {
        if (sttModel == null || sttModel.isBlank()) {
            throw new IllegalStateException("paw.visit.stt-model must not be blank");
        }
    }
}
