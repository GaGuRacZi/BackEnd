package com.gaguraczi.paw.domain.rag.support;

import com.gaguraczi.paw.domain.rag.dto.RagSearchHit;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiVectorStoreHitMapperTest {

    @Test
    void mapsPackedContentAndFilename() {
        String text = """
                source_id: SRC-9 | chunk: 2 | type: CORPUS
                과목: 외과 | 제목: 슬개골
                본문입니다.
                """;

        RagSearchHit hit = OpenAiVectorStoreHitMapper.map("file-1", "외과_CORPUS_003.md", text, 0.5);

        assertThat(hit.sourceId()).isEqualTo("SRC-9");
        assertThat(hit.chunkIndex()).isEqualTo(2);
        assertThat(hit.sourceType()).isEqualTo(RagSourceType.CORPUS);
        assertThat(hit.department()).isEqualTo("외과");
        assertThat(hit.title()).isEqualTo("슬개골");
    }

    @Test
    void fallsBackToFilename() {
        RagSearchHit hit = OpenAiVectorStoreHitMapper.map("file-1", "피부과_QA_001.md", "본문만", 0.1);

        assertThat(hit.department()).isEqualTo("피부과");
        assertThat(hit.sourceType()).isEqualTo(RagSourceType.QA);
    }
}
