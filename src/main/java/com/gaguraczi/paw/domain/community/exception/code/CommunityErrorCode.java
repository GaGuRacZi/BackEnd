package com.gaguraczi.paw.domain.community.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CommunityErrorCode implements BaseErrorCode {

    COMMUNITY_NOT_FOUND_404(HttpStatus.NOT_FOUND, "COMMUNITY_404", "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND_404(HttpStatus.NOT_FOUND, "COMMUNITY_404_3", "댓글을 찾을 수 없습니다."),
    TAG_NOT_FOUND_404(HttpStatus.NOT_FOUND, "COMMUNITY_404_2", "커뮤니티 태그를 찾을 수 없습니다."),
    REGION_NOT_FOUND_404(HttpStatus.NOT_FOUND, "COMMUNITY_404_4", "지역을 찾을 수 없습니다."),

    POST_TYPE_UNSUPPORTED_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_1", "지원하지 않는 게시글 유형입니다."),
    INVALID_CURSOR_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_2", "유효하지 않은 커서입니다."),
    COMMENT_CYCLE_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_3", "댓글 순환 참조는 허용되지 않습니다."),
    COMMENT_POST_MISMATCH_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_4", "다른 게시글의 댓글에는 대댓글을 달 수 없습니다."),
    TAG_TYPE_MISMATCH_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_5", "게시글 유형과 태그가 일치하지 않습니다."),
    MARKET_FIELD_REQUIRED_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_6", "장터 게시글에 필수 값이 누락되었습니다."),
    PHOTO_LIMIT_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_7", "사진은 최대 5장까지 업로드할 수 있습니다."),
    PHOTO_EMPTY_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_8", "이미지 파일이 비어 있습니다."),
    PHOTO_TOO_LARGE_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_9", "이미지 용량은 5MB 이하여야 합니다."),
    PHOTO_INVALID_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_10", "지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다."),
    THUMBNAIL_INVALID_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_11", "썸네일로 지정할 사진을 찾을 수 없습니다."),
    COMMENT_CONTENT_400(HttpStatus.BAD_REQUEST, "COMMUNITY_400_12", "댓글 내용이 올바르지 않습니다."),

    FORBIDDEN_403(HttpStatus.FORBIDDEN, "COMMUNITY_403", "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
