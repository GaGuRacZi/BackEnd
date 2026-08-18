package com.gaguraczi.paw.domain.rag.service;

import com.gaguraczi.paw.domain.rag.client.OpenAiVectorStoreClient;
import com.gaguraczi.paw.domain.rag.config.RagProperties;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.dto.RagSearchQuery;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.rag.exception.code.RagErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagSearchServiceTest {

    @Mock
    private OpenAiVectorStoreClient client;

    private RagSearchService searchService;

    @BeforeEach
    void setUp() {
        RagProperties properties = new RagProperties();
        properties.setSearchTopK(8);
        properties.setVectorStoreId("vs_test");
        properties.setChatModel("gpt-5.6-luna");
        properties.setReasoningEffort("none");
        searchService = new RagSearchService(client, properties);
    }

    @Test
    void rejectsBlankQuery() {
        assertThatThrownBy(() -> searchService.ask(new RagSearchQuery("  ", null, null, null, null)))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(RagErrorCode.RAG_QUERY_REQUIRED);
    }

    @Test
    void rejectsMissingVectorStore() {
        RagProperties properties = new RagProperties();
        searchService = new RagSearchService(client, properties);

        assertThatThrownBy(() -> searchService.ask(new RagSearchQuery("관절염", null, null, null, null)))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(RagErrorCode.RAG_VECTOR_STORE_MISSING);
    }

    @Test
    void asksModelWithFileSearch() {
        RagAskResult result = new RagAskResult("관절을 아낄 필요가 있어요.", List.of());
        when(client.ask("vs_test", "gpt-5.6-luna", "none", "앞다리 절뚝임\n과목: 내과\n자료유형: QA", 5))
                .thenReturn(result);

        RagAskResult actual = searchService.ask(
                new RagSearchQuery("앞다리 절뚝임", 5, RagSourceType.QA, "내과", null));

        assertThat(actual).isEqualTo(result);
        verify(client).ask("vs_test", "gpt-5.6-luna", "none", "앞다리 절뚝임\n과목: 내과\n자료유형: QA", 5);
    }

    @Test
    void usesDefaultTopK() {
        when(client.ask("vs_test", "gpt-5.6-luna", "none", "관절염", 8))
                .thenReturn(new RagAskResult("", List.of()));

        searchService.ask(new RagSearchQuery("관절염", null, null, null, null));

        verify(client).ask("vs_test", "gpt-5.6-luna", "none", "관절염", 8);
    }
}
