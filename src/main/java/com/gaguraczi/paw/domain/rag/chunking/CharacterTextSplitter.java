package com.gaguraczi.paw.domain.rag.chunking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Recursive character splitter: paragraphs, then sentences, then a hard cut with overlap.
 */
public final class CharacterTextSplitter {

    private static final Pattern MULTI_NEWLINE = Pattern.compile("\\n{3,}");
    private static final Pattern PARAGRAPH = Pattern.compile("\\n\\n+");
    private static final Pattern SENTENCE = Pattern.compile("(?<=[.!?다요음임])\\s+|\\n");

    private final int chunkSize;
    private final int overlap;

    public CharacterTextSplitter(int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and < chunkSize");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public List<String> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = MULTI_NEWLINE.matcher(text.replace("\r\n", "\n").replace('\r', '\n'))
                .replaceAll("\n\n")
                .trim();
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        List<String> pieces = new ArrayList<>();
        for (String paragraph : PARAGRAPH.split(normalized)) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() <= chunkSize) {
                pieces.add(trimmed);
            } else {
                pieces.addAll(splitSentences(trimmed));
            }
        }
        return pack(pieces);
    }

    private List<String> splitSentences(String text) {
        String[] sentences = SENTENCE.split(text);
        List<String> pieces = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() <= chunkSize) {
                pieces.add(trimmed);
            } else {
                pieces.addAll(hardSplit(trimmed));
            }
        }
        return pieces;
    }

    private List<String> hardSplit(String text) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            parts.add(text.substring(start, end).trim());
            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return parts.stream().filter(s -> !s.isBlank()).toList();
    }

    private List<String> pack(List<String> pieces) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String piece : pieces) {
            if (piece.length() > chunkSize) {
                flush(current, chunks);
                chunks.addAll(hardSplit(piece));
                continue;
            }
            int extra = current.isEmpty() ? 0 : 1;
            if (!current.isEmpty() && current.length() + extra + piece.length() > chunkSize) {
                flush(current, chunks);
                String overlapText = tail(chunks.getLast(), overlap);
                if (!overlapText.isEmpty()) {
                    current.append(overlapText);
                }
            }
            if (!current.isEmpty()) {
                current.append('\n');
            }
            current.append(piece);
        }
        flush(current, chunks);
        return chunks;
    }

    private static void flush(StringBuilder current, List<String> chunks) {
        if (current.isEmpty()) {
            return;
        }
        String value = current.toString().trim();
        if (!value.isEmpty()) {
            chunks.add(value);
        }
        current.setLength(0);
    }

    private static String tail(String text, int size) {
        if (text == null || text.isEmpty() || size <= 0) {
            return "";
        }
        return text.substring(Math.max(0, text.length() - size)).trim();
    }
}
