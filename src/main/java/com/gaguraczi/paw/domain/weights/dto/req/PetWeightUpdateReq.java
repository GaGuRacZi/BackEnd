package com.gaguraczi.paw.domain.weights.dto.req;

import com.gaguraczi.paw.domain.weights.enums.AppetiteTypeEnum;
import com.gaguraczi.paw.domain.weights.enums.BodyTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "PetWeightUpdateReq", description = "체중 기록 수정 요청 (보낸 값만 반영)")
public record PetWeightUpdateReq(

        @Schema(description = "몸무게(kg)", example = "4.30")
        @DecimalMin(value = "0.01", message = "몸무게는 0보다 커야 합니다.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal weight,

        @Schema(description = "체형 상태", example = "OVER_WEIGHT")
        BodyTypeEnum bodyType,

        @Schema(description = "컨디션(식욕) 체크", example = "Middle")
        AppetiteTypeEnum appetiteType,

        @Schema(description = "메모", example = "산책 직후 측정")
        @Size(max = 1000)
        String memoContent,

        @Schema(description = "기록일자(측정 날짜 + 시간)", example = "2026-07-06T20:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recordedAt,

        @Schema(description = """
                유지할 기존 메모 사진 URL 목록.
                생략(null)하면 기존 사진은 그대로 두고 신규 images만 추가된다.
                목록을 보내면 목록에 없는 기존 사진은 삭제된다(빈 배열이면 기존 사진 전체 삭제).
                """)
        List<String> keepPhotoUrls
) {
}
