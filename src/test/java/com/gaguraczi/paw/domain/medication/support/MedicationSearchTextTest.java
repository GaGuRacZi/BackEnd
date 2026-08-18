package com.gaguraczi.paw.domain.medication.support;

import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedicationSearchTextTest {

    @Test
    void concatenatesOriginalAndRefined() {
        MedicineStagingRow row = new MedicineStagingRow(
                "202311921",
                "카미녹스",
                "Carprofen 25mg",
                "카르프로펜",
                "관절염 통증 완화",
                "1일 2회",
                "다른 소염제와 병용 금지",
                "개"
        );

        String text = MedicationSearchText.of(
                row,
                "관절 통증을 줄여주는 **NSAID**예요.",
                "- 다른 소염제와 함께 쓰면 안 돼요"
        );

        assertThat(text).contains("이름: 카미녹스");
        assertThat(text).contains("영문명: Carprofen 25mg");
        assertThat(text).contains("성분: 카르프로펜");
        assertThat(text).contains("대상: 개");
        assertThat(text).contains("효능: 관절염 통증 완화");
        assertThat(text).contains("용법: 1일 2회");
        assertThat(text).contains("주의(원본): 다른 소염제와 병용 금지");
        assertThat(text).contains("약 설명:\n관절 통증을 줄여주는 **NSAID**예요.");
        assertThat(text).contains("주의할 점:\n- 다른 소염제와 함께 쓰면 안 돼요");
    }

    @Test
    void omitsBlankFields() {
        MedicineStagingRow row = new MedicineStagingRow(
                "1", "샴푸", null, "  ", null, null, null, ""
        );

        String text = MedicationSearchText.of(row, "세정용 샴푸예요.", "- 눈에 넣지 마세요");

        assertThat(text).startsWith("이름: 샴푸");
        assertThat(text).doesNotContain("영문명:");
        assertThat(text).doesNotContain("성분:");
        assertThat(text).doesNotContain("대상:");
        assertThat(text).contains("약 설명:");
        assertThat(text).contains("주의할 점:");
    }
}
