package com.gaguraczi.paw.domain.chat.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChatErrorCode implements BaseErrorCode {

    ROOM_NOT_FOUND_404(HttpStatus.NOT_FOUND, "CHAT_404_1", "채팅방을 찾을 수 없습니다."),
    POST_NOT_FOUND_404(HttpStatus.NOT_FOUND, "CHAT_404_2", "게시글을 찾을 수 없습니다."),
    MESSAGE_NOT_FOUND_404(HttpStatus.NOT_FOUND, "CHAT_404_3", "해당 채팅방의 메시지를 찾을 수 없습니다."),

    POST_TYPE_UNSUPPORTED_400(HttpStatus.BAD_REQUEST, "CHAT_400_1", "장터 게시글에만 채팅을 시작할 수 있습니다."),
    INVALID_CURSOR_400(HttpStatus.BAD_REQUEST, "CHAT_400_2", "유효하지 않은 커서입니다."),
    MESSAGE_CONTENT_REQUIRED_400(HttpStatus.BAD_REQUEST, "CHAT_400_3", "텍스트 메시지에는 내용이 필요합니다."),
    MESSAGE_IMAGE_REQUIRED_400(HttpStatus.BAD_REQUEST, "CHAT_400_4", "이미지 메시지에는 이미지 파일이 필요합니다."),

    SELF_CHAT_FORBIDDEN_403(HttpStatus.FORBIDDEN, "CHAT_403_1", "본인 게시글에는 채팅방을 생성할 수 없습니다."),
    NOT_PARTICIPANT_403(HttpStatus.FORBIDDEN, "CHAT_403_2", "채팅방 참여자만 접근할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
