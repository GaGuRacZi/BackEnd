package com.gaguraczi.paw.domain.mypage.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MypageSuccessCode implements BaseSuccessCode {

    MYPAGE_HOME_200(HttpStatus.OK, "MYPAGE_HOME_200", "마이페이지 홈 조회에 성공했습니다."),
    MYPAGE_PROFILE_200(HttpStatus.OK, "MYPAGE_PROFILE_200", "프로필 상세 조회에 성공했습니다."),
    MYPAGE_PROFILE_IMAGE_DELETE_200(HttpStatus.OK, "MYPAGE_PROFILE_IMAGE_DELETE_200", "프로필 사진이 삭제되었습니다."),
    MYPAGE_REGION_UPDATE_200(HttpStatus.OK, "MYPAGE_REGION_UPDATE_200", "지역이 설정되었습니다."),
    MYPAGE_COMMUNITY_POSTS_200(HttpStatus.OK, "MYPAGE_COMMUNITY_POSTS_200", "작성한 글 조회에 성공했습니다."),
    MYPAGE_COMMUNITY_LIKES_200(HttpStatus.OK, "MYPAGE_COMMUNITY_LIKES_200", "찜한 글 조회에 성공했습니다."),
    MYPAGE_COMMUNITY_COMMENTS_200(HttpStatus.OK, "MYPAGE_COMMUNITY_COMMENTS_200", "댓글 단 글 조회에 성공했습니다."),
    NOTIFICATION_SETTING_GET_200(HttpStatus.OK, "MYPAGE_NOTI_200", "알림 설정 조회에 성공했습니다."),
    NOTIFICATION_SETTING_UPDATE_200(HttpStatus.OK, "MYPAGE_NOTI_UPDATE_200", "알림 설정이 수정되었습니다."),
    TERMS_LIST_200(HttpStatus.OK, "MYPAGE_TERMS_200", "약관 목록 조회에 성공했습니다."),
    NOTICE_LIST_200(HttpStatus.OK, "MYPAGE_NOTICE_LIST_200", "공지사항 목록 조회에 성공했습니다."),
    NOTICE_DETAIL_200(HttpStatus.OK, "MYPAGE_NOTICE_DETAIL_200", "공지사항 상세 조회에 성공했습니다."),
    INQUIRY_CREATE_200(HttpStatus.OK, "MYPAGE_INQUIRY_CREATE_200", "문의가 등록되었습니다."),
    INQUIRY_LIST_200(HttpStatus.OK, "MYPAGE_INQUIRY_LIST_200", "문의 내역 조회에 성공했습니다."),
    INQUIRY_DETAIL_200(HttpStatus.OK, "MYPAGE_INQUIRY_DETAIL_200", "문의 상세 조회에 성공했습니다."),
    ADMIN_INQUIRY_LIST_200(HttpStatus.OK, "ADMIN_INQUIRY_LIST_200", "문의 목록 조회에 성공했습니다."),
    ADMIN_INQUIRY_DETAIL_200(HttpStatus.OK, "ADMIN_INQUIRY_DETAIL_200", "문의 상세 조회에 성공했습니다."),
    ADMIN_INQUIRY_ANSWER_200(HttpStatus.OK, "ADMIN_INQUIRY_ANSWER_200", "문의 답변이 등록되었습니다."),
    WITHDRAWAL_PREVIEW_200(HttpStatus.OK, "MYPAGE_WITHDRAWAL_PREVIEW_200", "탈퇴 전 확인 정보 조회에 성공했습니다."),
    WITHDRAWAL_200(HttpStatus.OK, "MYPAGE_WITHDRAWAL_200", "회원 탈퇴가 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
