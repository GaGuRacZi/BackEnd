package com.gaguraczi.paw.domain.visit.client;

public record DiarizedSegment(
        String speaker,
        String text,
        Double startSec,
        Double endSec
) {
}
