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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "chat", description = ChatApiDocs.TAG_DESCRIPTION)
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(
            summary = "채팅방 생성/조회 (idempotent)",
            description = ChatApiDocs.CREATE_DESCRIPTION,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatRoomCreateReq.class),
                            examples = @ExampleObject(name = "장터 글", value = ChatApiDocs.CREATE_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CHAT_ROOM_CREATE_200. 새 방이든 기존 방이든 동일 코드.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_ROOM_CREATE_200", value = ChatApiDocs.CREATE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "CHAT_400_1. 장터(MARKET) 글이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_400_1", value = ChatApiDocs.CHAT_400_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHAT_403_1. 본인 게시글.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_403_1", value = ChatApiDocs.CHAT_403_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CHAT_404_2. 게시글 없음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_404_2", value = ChatApiDocs.CHAT_404_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ChatApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ChatApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @PostMapping("/chat/rooms")
    public ApiResponse<ChatRoomCreateRes> createOrGet(
            @org.springframework.web.bind.annotation.RequestBody @Valid ChatRoomCreateReq req
    ) {
        return ApiResponse.onSuccess(
                ChatSuccessCode.CHAT_ROOM_CREATE_200,
                chatRoomService.createOrGet(req.postId())
        );
    }

    @Operation(summary = "채팅방 목록", description = ChatApiDocs.LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CHAT_ROOM_LIST_200.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_ROOM_LIST_200", value = ChatApiDocs.LIST_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "CHAT_400_2. 커서 변조/파싱 실패.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_400_2", value = ChatApiDocs.CHAT_400_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ChatApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ChatApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @GetMapping("/chat/rooms")
    public ApiResponse<CursorPageRes<ChatRoomListItemRes>> list(
            @Parameter(description = "이전 응답 nextCursor. 첫 페이지는 생략", example = "MjAyNi0wOC0yMFQxMTozMDowMF8xMg")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_ROOM_LIST_200, chatRoomService.list(cursor, size));
    }

    @Operation(summary = "채팅방 상세", description = ChatApiDocs.DETAIL_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CHAT_ROOM_DETAIL_200.",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "글 있음", value = ChatApiDocs.DETAIL_200_EXAMPLE),
                            @ExampleObject(name = "글 삭제됨", value = ChatApiDocs.DETAIL_DELETED_200_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHAT_403_2. 참여자가 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_403_2", value = ChatApiDocs.CHAT_403_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CHAT_404_1. 방 없음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_404_1", value = ChatApiDocs.CHAT_404_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ChatApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ChatApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @GetMapping("/chat/rooms/{roomId}")
    public ApiResponse<ChatRoomDetailRes> detail(
            @Parameter(description = "채팅방 ID", example = "12", required = true) @PathVariable Long roomId
    ) {
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_ROOM_DETAIL_200, chatRoomService.getDetail(roomId));
    }

    @Operation(summary = "메시지 목록", description = ChatApiDocs.MESSAGES_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CHAT_MESSAGE_LIST_200. 최신 → 과거.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_MESSAGE_LIST_200", value = ChatApiDocs.MESSAGES_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "CHAT_400_2. 커서 변조/파싱 실패.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_400_2", value = ChatApiDocs.CHAT_400_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHAT_403_2. 참여자가 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_403_2", value = ChatApiDocs.CHAT_403_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CHAT_404_1. 방 없음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_404_1", value = ChatApiDocs.CHAT_404_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ChatApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ChatApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @GetMapping("/chat/rooms/{roomId}/messages")
    public ApiResponse<CursorPageRes<ChatMessageRes>> messages(
            @Parameter(description = "채팅방 ID", example = "12", required = true) @PathVariable Long roomId,
            @Parameter(description = "이전 응답 nextCursor. 더 과거 페이지", example = "NDk5")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 30, 최대 50", example = "30")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_MESSAGE_LIST_200, chatMessageService.list(roomId, cursor, size));
    }

    @Operation(
            summary = "메시지 전송",
            description = ChatApiDocs.SEND_DESCRIPTION,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ChatMessageMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = {
                                    @ExampleObject(name = "TEXT", value = ChatApiDocs.SEND_TEXT_DATA_EXAMPLE),
                                    @ExampleObject(name = "IMAGE", value = ChatApiDocs.SEND_IMAGE_DATA_EXAMPLE)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CHAT_MESSAGE_SEND_200. 저장 성공. 상대 알림은 설정·DND·토큰에 따름.",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "TEXT", value = ChatApiDocs.SEND_TEXT_200_EXAMPLE),
                            @ExampleObject(name = "IMAGE", value = ChatApiDocs.SEND_IMAGE_200_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "CHAT_400_3 텍스트 내용 / CHAT_400_4 이미지 파일 / COMMUNITY_400_9 5MB 초과 / COMMUNITY_400_10 포맷.",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "CHAT_400_3", value = ChatApiDocs.CHAT_400_3_EXAMPLE),
                            @ExampleObject(name = "CHAT_400_4", value = ChatApiDocs.CHAT_400_4_EXAMPLE),
                            @ExampleObject(name = "COMMUNITY_400_9", value = ChatApiDocs.COMMUNITY_400_9_EXAMPLE),
                            @ExampleObject(name = "COMMUNITY_400_10", value = ChatApiDocs.COMMUNITY_400_10_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHAT_403_2. 참여자가 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_403_2", value = ChatApiDocs.CHAT_403_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CHAT_404_1. 방 없음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_404_1", value = ChatApiDocs.CHAT_404_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ChatApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ChatApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @PostMapping(value = "/chat/rooms/{roomId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ChatMessageRes> sendMessage(
            @Parameter(description = "채팅방 ID", example = "12", required = true) @PathVariable Long roomId,
            @RequestPart("data") @Valid ChatMessageSendReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                ChatSuccessCode.CHAT_MESSAGE_SEND_200,
                chatMessageService.send(roomId, data.type(), data.content(), image)
        );
    }

    @Operation(
            summary = "읽음 처리",
            description = ChatApiDocs.READ_DESCRIPTION,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatRoomReadReq.class),
                            examples = @ExampleObject(name = "최신 메시지", value = ChatApiDocs.READ_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CHAT_ROOM_READ_200. result는 null.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_ROOM_READ_200", value = ChatApiDocs.READ_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "CHAT_403_2. 참여자가 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "CHAT_403_2", value = ChatApiDocs.CHAT_403_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "CHAT_404_1 방 없음 / CHAT_404_3 이 방에 없는 messageId.",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "CHAT_404_1", value = ChatApiDocs.CHAT_404_1_EXAMPLE),
                            @ExampleObject(name = "CHAT_404_3", value = ChatApiDocs.CHAT_404_3_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ChatApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ChatApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @PatchMapping("/chat/rooms/{roomId}/read")
    public ApiResponse<Void> read(
            @Parameter(description = "채팅방 ID", example = "12", required = true) @PathVariable Long roomId,
            @org.springframework.web.bind.annotation.RequestBody @Valid ChatRoomReadReq req
    ) {
        chatRoomService.markRead(roomId, req.lastReadMessageId());
        return ApiResponse.onSuccess(ChatSuccessCode.CHAT_ROOM_READ_200, null);
    }

    @Schema(name = "ChatMessageMultipart", description = "메시지 전송 multipart. data는 JSON, IMAGE면 image 파일 필수.")
    public static class ChatMessageMultipart {
        @Schema(description = "메시지 JSON", implementation = ChatMessageSendReq.class, requiredMode = Schema.RequiredMode.REQUIRED)
        public ChatMessageSendReq data;

        @Schema(description = "IMAGE일 때 필수. 최대 5MB. JPEG/PNG/GIF/WEBP/HEIC/HEIF", type = "string", format = "binary")
        public MultipartFile image;
    }
}
