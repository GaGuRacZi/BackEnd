package com.gaguraczi.paw.domain.chat.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChatSuccessCode implements BaseSuccessCode {

    CHAT_ROOM_CREATE_200(HttpStatus.OK, "CHAT_ROOM_CREATE_200", "채팅방이 준비되었습니다."),
    CHAT_ROOM_LIST_200(HttpStatus.OK, "CHAT_ROOM_LIST_200", "채팅방 목록 조회에 성공했습니다."),
    CHAT_ROOM_DETAIL_200(HttpStatus.OK, "CHAT_ROOM_DETAIL_200", "채팅방 조회에 성공했습니다."),
    CHAT_MESSAGE_LIST_200(HttpStatus.OK, "CHAT_MESSAGE_LIST_200", "메시지 목록 조회에 성공했습니다."),
    CHAT_MESSAGE_SEND_200(HttpStatus.OK, "CHAT_MESSAGE_SEND_200", "메시지가 전송되었습니다."),
    CHAT_ROOM_READ_200(HttpStatus.OK, "CHAT_ROOM_READ_200", "읽음 처리되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
