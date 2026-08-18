package com.gaguraczi.paw.domain.visit.client;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public final class OpenAiSttResponseParser {

    private OpenAiSttResponseParser() {
    }

    public static DiarizedTranscript parse(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return new DiarizedTranscript("", 0, List.of());
        }
        String text = root.path("text").asString("");
        int durationSec = (int) Math.round(root.path("duration").asDouble(0d));
        List<DiarizedSegment> segments = new ArrayList<>();
        JsonNode segmentNode = root.path("segments");
        if (segmentNode.isArray()) {
            for (JsonNode item : segmentNode) {
                String speaker = item.path("speaker").asString("");
                String segmentText = item.path("text").asString("");
                if (speaker.isBlank() && segmentText.isBlank()) {
                    continue;
                }
                Double start = item.path("start").isMissingNode() || item.path("start").isNull()
                        ? null
                        : item.path("start").asDouble(0d);
                Double end = item.path("end").isMissingNode() || item.path("end").isNull()
                        ? null
                        : item.path("end").asDouble(0d);
                segments.add(new DiarizedSegment(
                        speaker.isBlank() ? "A" : speaker,
                        segmentText,
                        start,
                        end
                ));
            }
        }
        return new DiarizedTranscript(text, Math.max(durationSec, 0), List.copyOf(segments));
    }
}
