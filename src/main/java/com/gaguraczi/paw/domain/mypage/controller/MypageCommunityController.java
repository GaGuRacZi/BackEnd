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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage/community")
@RequiredArgsConstructor
public class MypageCommunityController {

    private final MypageCommunityService mypageCommunityService;

    @Operation(
            summary = "내가 작성한 글 목록",
            description = "Access Token(JWT) 필수. postType 미지정 시 전체(소통/장터/리뷰). 커서 기반 페이지네이션."
    )
    @GetMapping("/posts")
    public ApiResponse<CursorPageRes<MyPostItemRes>> getMyPosts(
            @Parameter(description = "게시글 타입, 미지정 시 전체") @RequestParam(required = false) PostType postType,
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기, 기본 20 최대 50") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.MYPAGE_COMMUNITY_POSTS_200,
                mypageCommunityService.getMyPosts(postType, cursor, size)
        );
    }

    @Operation(
            summary = "내가 찜한 글 목록",
            description = "Access Token(JWT) 필수. 커서 기반 페이지네이션."
    )
    @GetMapping("/likes")
    public ApiResponse<CursorPageRes<MyPostItemRes>> getMyLikes(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기, 기본 20 최대 50") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.MYPAGE_COMMUNITY_LIKES_200,
                mypageCommunityService.getMyLikes(cursor, size)
        );
    }

    @Operation(
            summary = "내가 댓글 단 글 목록",
            description = "Access Token(JWT) 필수. 댓글 미리보기 포함, 커서 기반 페이지네이션."
    )
    @GetMapping("/comments")
    public ApiResponse<CursorPageRes<MyCommentItemRes>> getMyComments(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기, 기본 20 최대 50") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.MYPAGE_COMMUNITY_COMMENTS_200,
                mypageCommunityService.getMyComments(cursor, size)
        );
    }
}
