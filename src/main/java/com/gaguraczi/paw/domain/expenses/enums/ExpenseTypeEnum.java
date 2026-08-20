package com.gaguraczi.paw.domain.expenses.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExpenseTypeEnum { // 비용 타입 (항목별 지출 차트 카테고리)

    SURGERY("수술"),
    TESTS("검진"),
    MEDICINE("약"),
    GOODS("용품"),
    ETC("기타");

    private final String label;
}
