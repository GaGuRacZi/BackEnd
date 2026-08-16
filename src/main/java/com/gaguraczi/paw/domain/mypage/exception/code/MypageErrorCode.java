package com.gaguraczi.paw.domain.mypage.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MypageErrorCode implements BaseErrorCode {

    MYPAGE_400(HttpStatus.BAD_REQUEST, "MYPAGE_400", "요청 처리에 실패했습니다."),
    NOTIFICATION_SETTING_INVALID(HttpStatus.BAD_REQUEST, "MYPAGE_400_1", "방해 금지 시간대는 시작/종료 시각을 함께 입력해야 합니다."),
    ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "MYPAGE_400_2", "이미 탈퇴한 계정입니다."),
    REGION_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "MYPAGE_400_3", "regionCode는 필수입니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "MYPAGE_404_1", "문의 내역을 찾을 수 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "MYPAGE_404_2", "존재하지 않는 지역 코드입니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "MYPAGE_404_3", "공지사항을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
