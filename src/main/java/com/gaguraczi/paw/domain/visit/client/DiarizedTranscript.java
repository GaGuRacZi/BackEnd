package com.gaguraczi.paw.domain.visit.client;

import java.util.List;

public record DiarizedTranscript(
        String text,
        Integer durationSec,
        List<DiarizedSegment> segments
) {
}
