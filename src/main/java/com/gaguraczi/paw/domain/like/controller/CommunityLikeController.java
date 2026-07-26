package com.gaguraczi.paw.domain.like.controller;

import com.gaguraczi.paw.domain.like.dto.response.LikeToggleResponse;
import com.gaguraczi.paw.domain.like.service.CommunityLikeService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "community-like", description = "커뮤니티 좋아요 관련 api 입니다.")
@RestController
@RequestMapping("/api/communities/{postId}/likes")
@RequiredArgsConstructor
public class CommunityLikeController {

    private final CommunityLikeService communityLikeService;

    @Operation(summary = "게시글 좋아요 토글", description = "좋아요가 없으면 등록하고, 있으면 취소합니다.")
    @PostMapping
    public ApiResponse<LikeToggleResponse> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal String uid
    ) {
        LikeToggleResponse response = communityLikeService.toggleLike(postId, UUID.fromString(uid));
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
