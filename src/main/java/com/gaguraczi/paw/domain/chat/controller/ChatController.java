package com.gaguraczi.paw.domain.chat.controller;

import com.gaguraczi.paw.domain.chat.dto.req.ChatMessageSendReq;
import com.gaguraczi.paw.domain.chat.dto.req.ChatRoomCreateReq;
import com.gaguraczi.paw.domain.chat.dto.req.ChatRoomReadReq;
import com.gaguraczi.paw.domain.chat.dto.res.ChatMessageRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomCreateRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomDetailRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomListItemRes;
import com.gaguraczi.paw.domain.chat.exception.code.ChatSuccessCode;
import com.gaguraczi.paw.domain.chat.service.ChatMessageService;
import com.gaguraczi.paw.domain.chat.service.ChatRoomService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "chat", description = "장터 게시글 기반 1:1 채팅. 실시간성은 REST 새로고침/재진입 기준이며, FCM은 다른 화면/백그라운드용 트리거로만 사용된다. JWT Bearer 필수.")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(
            summary = "채팅방 생성/조회 (idempotent get-or-create)",
            description = "postId + 요청자 조합의 방이 있으면 반환, 없으면 생성한다. 본인 게시글이면 403(CHAT_403_1)."
    )
    @PostMapping("/chat/rooms")
    public ApiResponse<ChatRoomCreateRes> createOrGet(
            @org.springframework.web.bind.annotation.RequestBody @Valid ChatRoomCreateReq req
    ) {
        return ApiResponse.onSuccess(
                ChatSuccessCode.CHAT_ROOM_CREATE_200,
                chatRoomService.createOrGet(req.postId())
        );
    }

    @Operation(summary = "채팅방 목록", description = "마지막 메시지 순 정렬. 커서 기반 페이지네이션.")
    @GetMapping("/chat/rooms")
    public ApiResponse<CursorPageRes<ChatRoomListItemRes>> list(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_ROOM_LIST_200, chatRoomService.list(cursor, size));
    }

    @Operation(
            summary = "채팅방 상세",
            description = "상단 게시글 요약 카드는 postId로 실시간 조회한다. 게시글이 삭제되었으면 post.deleted=true로 표시되고 대화는 유지된다."
    )
    @GetMapping("/chat/rooms/{roomId}")
    public ApiResponse<ChatRoomDetailRes> detail(@PathVariable Long roomId) {
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_ROOM_DETAIL_200, chatRoomService.getDetail(roomId));
    }

    @Operation(summary = "메시지 목록 (커서 슬라이딩)", description = "최신 메시지부터 과거 방향으로 커서 이동.")
    @GetMapping("/chat/rooms/{roomId}/messages")
    public ApiResponse<CursorPageRes<ChatMessageRes>> messages(
            @PathVariable Long roomId,
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 30, 최대 50)") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_MESSAGE_LIST_200, chatMessageService.list(roomId, cursor, size));
    }

    @Operation(
            summary = "메시지 전송",
            description = """
                    multipart/form-data: data(JSON) + image(선택, IMAGE 타입일 때 필수)
                    - TEXT: data.content 필수
                    - IMAGE: image 파일 필수 (5MB 이하, JPEG/PNG/GIF/WEBP/HEIC/HEIF)
                    전송 성공 시 상대방의 채팅 알림 설정·방해금지 시간을 확인해 FCM을 발송한다.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ChatMessageSendReq.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @PostMapping(value = "/chat/rooms/{roomId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ChatMessageRes> sendMessage(
            @PathVariable Long roomId,
            @RequestPart("data") @Valid ChatMessageSendReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                ChatSuccessCode.CHAT_MESSAGE_SEND_200,
                chatMessageService.send(roomId, data.type(), data.content(), image)
        );
    }

    @Operation(summary = "읽음 처리", description = "방 단위 lastReadMessageId를 갱신한다.")
    @PatchMapping("/chat/rooms/{roomId}/read")
    public ApiResponse<Void> read(
            @PathVariable Long roomId,
            @org.springframework.web.bind.annotation.RequestBody @Valid ChatRoomReadReq req
    ) {
        chatRoomService.markRead(roomId, req.lastReadMessageId());
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_ROOM_READ_200, null);
    }
}
