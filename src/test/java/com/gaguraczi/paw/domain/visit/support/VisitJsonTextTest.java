package com.gaguraczi.paw.domain.visit.support;

import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisitJsonTextTest {

    @Test
    void extractsObjectAfterWarningPrefix() {
        String json = VisitJsonText.extractJson("[주의] 화자 매핑 결과입니다.\n{\"A\":\"VET\",\"B\":\"OWNER\"}");

        assertThat(json).isEqualTo("{\"A\":\"VET\",\"B\":\"OWNER\"}");
    }

    @Test
    void stripsCodeFencesThenExtractsObject() {
        String json = VisitJsonText.extractJson("""
                ```json
                {"A":"VET"}
                ```
                """);

        assertThat(json).isEqualTo("{\"A\":\"VET\"}");
    }

    @Test
    void extractsFirstCompleteArray() {
        String json = VisitJsonText.extractJson("설명입니다. [1,2] 그리고 {\"ignored\":true}");

        assertThat(json).isEqualTo("[1,2]");
    }

    @Test
    void failsWhenNoJsonPresent() {
        assertThatThrownBy(() -> VisitJsonText.extractJson("[주의] 설명만 있습니다."))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }
}
