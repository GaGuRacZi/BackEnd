package com.gaguraczi.paw.global.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "커서 기반 페이지 응답")
public class CursorPageRes<T> {

    @Schema(description = "현재 페이지 아이템")
    private final List<T> content;
    @Schema(
            description = "다음 페이지 커서 (없으면 null). API가 반환한 opaque 값을 다음 요청에 그대로 전달하세요. 클라이언트에서 해석하거나 변조하지 마세요."
    )
    private final String nextCursor;
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;
    @Schema(description = "요청 size (실제 적용된 값)", example = "20")
    private final int size;

    public static <T> CursorPageRes<T> of(List<T> content, String nextCursor, boolean hasNext, int size) {
        return CursorPageRes.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(size)
                .build();
    }
}
