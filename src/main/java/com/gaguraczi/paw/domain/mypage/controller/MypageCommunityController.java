package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.mypage.dto.res.MyCommentItemRes;
import com.gaguraczi.paw.domain.mypage.dto.res.MyPostItemRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.MypageCommunityService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage/community")
@RequiredArgsConstructor
public class MypageCommunityController {

    private final MypageCommunityService mypageCommunityService;

    @Operation(
            summary = "내가 작성한 글 목록",
            description = """
                    Access Token(JWT) 필수. 최신순 커서 페이지네이션.
                    - postType 생략 시 소통/장터/리뷰 전체
                    - size 기본 20, 최대 50. 1 미만이면 20
                    - cursor는 이전 응답의 nextCursor를 그대로 전달 (커뮤니티 목록 커서와 포맷이 같습니다)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_COMMUNITY_POSTS_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "작성글 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_COMMUNITY_POSTS_200",
                                              "message": "작성한 글 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "postId": 10,
                                                    "postType": "COMMUNICATION",
                                                    "tagName": "건강상담",
                                                    "commentCount": 4,
                                                    "likeCount": 12,
                                                    "createdAt": "2026-08-20T15:00:00"
                                                  }
                                                ],
                                                "nextCursor": "TEFURVNUfDIwMjYtMDgtMjBUMTU6MDA6MDB8MTA",
                                                "hasNext": true,
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
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMUNITY_400_2",
                                    value = """
                                            {"isSuccess":false,"code":"COMMUNITY_400_2","message":"유효하지 않은 커서입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/posts")
    public ApiResponse<CursorPageRes<MyPostItemRes>> getMyPosts(
            @Parameter(description = "게시글 타입. 미지정 시 전체", example = "COMMUNICATION")
            @RequestParam(required = false) PostType postType,
            @Parameter(description = "이전 응답의 nextCursor", example = "TEFURVNUfDIwMjYtMDgtMjBUMTU6MDA6MDB8MTA")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.MYPAGE_COMMUNITY_POSTS_200,
                mypageCommunityService.getMyPosts(postType, cursor, size)
        );
    }

    @Operation(
            summary = "내가 찜한 글 목록",
            description = """
                    Access Token(JWT) 필수. 찜한 시각 최신순 커서 페이지네이션.
                    - size 기본 20, 최대 50
                    - 응답 아이템 형태는 작성글 목록과 동일합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_COMMUNITY_LIKES_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "찜한 글 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_COMMUNITY_LIKES_200",
                                              "message": "찜한 글 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "postId": 22,
                                                    "postType": "MARKET",
                                                    "tagName": "사료·간식",
                                                    "commentCount": 1,
                                                    "likeCount": 8,
                                                    "createdAt": "2026-08-19T12:00:00"
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
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMUNITY_400_2",
                                    value = """
                                            {"isSuccess":false,"code":"COMMUNITY_400_2","message":"유효하지 않은 커서입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/likes")
    public ApiResponse<CursorPageRes<MyPostItemRes>> getMyLikes(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.MYPAGE_COMMUNITY_LIKES_200,
                mypageCommunityService.getMyLikes(cursor, size)
        );
    }

    @Operation(
            summary = "내가 댓글 단 글 목록",
            description = """
                    Access Token(JWT) 필수. 댓글 작성 시각 최신순.
                    - commentPreview는 최대 80자입니다.
                    - size 기본 20, 최대 50
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_COMMUNITY_COMMENTS_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "댓글 단 글",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_COMMUNITY_COMMENTS_200",
                                              "message": "댓글 단 글 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "postId": 10,
                                                    "title": "산책 코스 추천해주세요",
                                                    "commentPreview": "한강공원 쪽 추천해요.",
                                                    "commentedAt": "2026-08-20T16:30:00"
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
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMUNITY_400_2",
                                    value = """
                                            {"isSuccess":false,"code":"COMMUNITY_400_2","message":"유효하지 않은 커서입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/comments")
    public ApiResponse<CursorPageRes<MyCommentItemRes>> getMyComments(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.MYPAGE_COMMUNITY_COMMENTS_200,
                mypageCommunityService.getMyComments(cursor, size)
        );
    }
}
