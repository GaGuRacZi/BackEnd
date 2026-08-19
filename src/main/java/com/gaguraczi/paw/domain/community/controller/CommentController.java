package com.gaguraczi.paw.domain.community.controller;

import com.gaguraczi.paw.domain.community.dto.req.CommentCreateReq;
import com.gaguraczi.paw.domain.community.dto.req.CommentUpdateReq;
import com.gaguraczi.paw.domain.community.dto.res.CommentRes;
import com.gaguraczi.paw.domain.community.exception.code.CommunitySuccessCode;
import com.gaguraczi.paw.domain.community.service.CommentService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "comments", description = "커뮤니티 댓글 API. JWT Bearer 필수. flat 목록 + parentId로 트리 구성.")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "게시글 댓글 목록 (flat + parentId, 커서)",
            description = """
                    커서 기반 flat 댓글 목록입니다. parentId로 클라이언트에서 트리를 구성합니다.
                    - soft delete된 댓글도 포함 (deleted: true, content: null)
                    - size 기본 20, 최대 50
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMENT_LIST_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "댓글 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMENT_LIST_200",
                                              "message": "댓글 목록 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "commentId": 1,
                                                    "postId": 10,
                                                    "parentId": null,
                                                    "content": "좋은 정보 감사합니다",
                                                    "deleted": false,
                                                    "authorNickname": "길동이",
                                                    "createdAt": "2026-08-08T11:00:00"
                                                  },
                                                  {
                                                    "commentId": 2,
                                                    "postId": 10,
                                                    "parentId": 1,
                                                    "content": "저도 같은 고민이에요",
                                                    "deleted": false,
                                                    "authorNickname": "냥집사",
                                                    "createdAt": "2026-08-08T11:05:00"
                                                  },
                                                  {
                                                    "commentId": 3,
                                                    "postId": 10,
                                                    "parentId": null,
                                                    "content": null,
                                                    "deleted": true,
                                                    "authorNickname": "삭제유저",
                                                    "createdAt": "2026-08-08T11:10:00"
                                                  }
                                                ],
                                                "nextCursor": null,
                                                "hasNext": false,
                                                "size": 20
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 커서 (COMMUNITY_400_2)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "커서 오류",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_400_2",
                                              "message": "유효하지 않은 커서입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 (COMMUNITY_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "게시글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404",
                                              "message": "게시글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/communities/{postId}/comments")
    public ApiResponse<CursorPageRes<CommentRes>> list(
            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long postId,
            @Parameter(description = "이전 응답의 nextCursor")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMENT_LIST_200,
                commentService.list(postId, cursor, size)
        );
    }

    @Operation(
            summary = "댓글/대댓글 작성",
            description = """
                    parentId가 없으면 루트 댓글, 있으면 대댓글입니다.
                    - 부모 댓글은 같은 게시글이어야 함
                    - 순환 참조 불가
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "루트 댓글",
                                            value = """
                                                    {
                                                      "content": "좋은 정보 감사합니다",
                                                      "parentId": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "대댓글",
                                            value = """
                                                    {
                                                      "content": "저도 같은 고민이에요",
                                                      "parentId": 1
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMENT_CREATE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "작성 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMENT_CREATE_200",
                                              "message": "댓글이 등록되었습니다.",
                                              "result": {
                                                "commentId": 4,
                                                "postId": 10,
                                                "parentId": 1,
                                                "content": "저도 같은 고민이에요",
                                                "deleted": false,
                                                "authorNickname": "냥집사",
                                                "createdAt": "2026-08-08T12:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "검증/비즈니스 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "다른 게시글 대댓글",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_4",
                                                      "message": "다른 게시글의 댓글에는 대댓글을 달 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "순환 참조",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_3",
                                                      "message": "댓글 순환 참조는 허용되지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "내용 오류",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_400_12",
                                                      "message": "댓글 내용이 올바르지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "게시글/댓글 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "게시글 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_404",
                                                      "message": "게시글을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "부모 댓글 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMUNITY_404_3",
                                                      "message": "댓글을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/communities/{postId}/comments")
    public ApiResponse<CommentRes> create(
            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateReq req
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMENT_CREATE_200,
                commentService.create(postId, req)
        );
    }

    @Operation(
            summary = "댓글 수정",
            description = "작성자만 수정할 수 있습니다. soft delete된 댓글은 수정할 수 없습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "댓글 수정",
                                    value = """
                                            {
                                              "content": "수정된 댓글 내용입니다"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMENT_UPDATE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "수정 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMENT_UPDATE_200",
                                              "message": "댓글이 수정되었습니다.",
                                              "result": {
                                                "commentId": 1,
                                                "postId": 10,
                                                "parentId": null,
                                                "content": "수정된 댓글 내용입니다",
                                                "deleted": false,
                                                "authorNickname": "길동이",
                                                "createdAt": "2026-08-08T11:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "작성자 아님 (COMMUNITY_403)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_403",
                                              "message": "권한이 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "댓글 없음 (COMMUNITY_404_3)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "댓글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404_3",
                                              "message": "댓글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "내용 오류 (COMMUNITY_400_12)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "내용 오류",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_400_12",
                                              "message": "댓글 내용이 올바르지 않습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/comments/{commentId}")
    public ApiResponse<CommentRes> update(
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateReq req
    ) {
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMENT_UPDATE_200,
                commentService.update(commentId, req)
        );
    }

    @Operation(
            summary = "댓글 soft delete",
            description = "작성자만 삭제할 수 있습니다. 실제 행은 유지되며 deleted=true, content=null로 노출됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (COMMENT_DELETE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "삭제 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMENT_DELETE_200",
                                              "message": "댓글이 삭제되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "작성자 아님 (COMMUNITY_403)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_403",
                                              "message": "권한이 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "댓글 없음 (COMMUNITY_404_3)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "댓글 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMUNITY_404_3",
                                              "message": "댓글을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId
    ) {
        commentService.delete(commentId);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMENT_DELETE_200, null);
    }
}
