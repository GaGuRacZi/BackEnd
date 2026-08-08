package com.gaguraczi.paw.domain.community.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CommunitySuccessCode implements BaseSuccessCode {

    COMMUNITY_LIST_200(HttpStatus.OK, "COMMUNITY_LIST_200", "커뮤니티 목록 조회에 성공했습니다."),
    COMMUNITY_DETAIL_200(HttpStatus.OK, "COMMUNITY_DETAIL_200", "커뮤니티 상세 조회에 성공했습니다."),
    COMMUNITY_TAG_LIST_200(HttpStatus.OK, "COMMUNITY_TAG_LIST_200", "커뮤니티 태그 목록 조회에 성공했습니다."),
    COMMUNITY_CREATE_200(HttpStatus.OK, "COMMUNITY_CREATE_200", "게시글이 등록되었습니다."),
    COMMUNITY_UPDATE_200(HttpStatus.OK, "COMMUNITY_UPDATE_200", "게시글이 수정되었습니다."),
    COMMUNITY_DELETE_200(HttpStatus.OK, "COMMUNITY_DELETE_200", "게시글이 삭제되었습니다."),
    COMMENT_LIST_200(HttpStatus.OK, "COMMENT_LIST_200", "댓글 목록 조회에 성공했습니다."),
    COMMENT_CREATE_200(HttpStatus.OK, "COMMENT_CREATE_200", "댓글이 등록되었습니다."),
    COMMENT_UPDATE_200(HttpStatus.OK, "COMMENT_UPDATE_200", "댓글이 수정되었습니다."),
    COMMENT_DELETE_200(HttpStatus.OK, "COMMENT_DELETE_200", "댓글이 삭제되었습니다."),
    LIKE_TOGGLE_200(HttpStatus.OK, "LIKE_TOGGLE_200", "좋아요 상태가 변경되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
