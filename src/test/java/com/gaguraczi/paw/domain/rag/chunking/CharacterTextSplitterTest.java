package com.gaguraczi.paw.domain.rag.chunking;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharacterTextSplitterTest {

    private final CharacterTextSplitter splitter = new CharacterTextSplitter(40, 8);

    @Test
    void blankReturnsEmpty() {
        assertThat(splitter.split("   ")).isEmpty();
        assertThat(splitter.split(null)).isEmpty();
    }

    @Test
    void shortTextStaysSingleChunk() {
        assertThat(splitter.split("짧은 본문입니다.")).containsExactly("짧은 본문입니다.");
    }

    @Test
    void longTextSplitsWithOverlap() {
        String text = """
                첫 번째 문단입니다. 관절염 관리에 대한 설명입니다.

                두 번째 문단입니다. 미끄러운 바닥을 피하고 약물 복용을 안내합니다.

                세 번째 문단입니다. 수중 재활과 재방문을 권장합니다.
                """;

        List<String> chunks = splitter.split(text);
        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.getFirst()).contains("첫 번째");
        assertThat(String.join("", chunks)).contains("수중 재활");
    }

    @Test
    void consecutiveMaxSizePiecesDoNotExceedChunkSize() {
        CharacterTextSplitter maxSplitter = new CharacterTextSplitter(10, 3);
        String first = "a".repeat(10);
        String second = "b".repeat(10);

        List<String> chunks = maxSplitter.split(first + "\n\n" + second);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allMatch(chunk -> chunk.length() <= 10);
        assertThat(chunks.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void rejectsInvalidOverlap() {
        assertThatThrownBy(() -> new CharacterTextSplitter(10, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
