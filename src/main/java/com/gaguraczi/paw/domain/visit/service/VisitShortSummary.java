package com.gaguraczi.paw.domain.visit.service;

import java.util.List;

public record VisitShortSummary(
        String visitName,
        List<String> diagnosisFindings,
        String oneLineSummary,
        List<String> careItems,
        String careNote,
        String hospitalName
) {
}
