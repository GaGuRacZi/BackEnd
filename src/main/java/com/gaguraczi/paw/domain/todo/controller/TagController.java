package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.exception.code.TagSuccessCode;
import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import com.gaguraczi.paw.domain.todo.service.TagService;
import com.gaguraczi.paw.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody TagCreateRequest request
    ) {
        TagResponse response = tagService.createTag(uid, request);

        return ResponseEntity
                .created(URI.create("/api/tags/" + response.tagId()))
                .body(ApiResponse.onSuccess(TagSuccessCode.TAG_CREATE_201, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getMyTags(
            @AuthenticationPrincipal String uid
    ) {
        List<TagResponse> responses = tagService.getTagsByUser(uid);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(TagSuccessCode.TAG_LIST_200, responses)
        );
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> getTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId
    ) {
        TagResponse response = tagService.getTag(uid, tagId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(TagSuccessCode.TAG_GET_200, response)
        );
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        TagResponse response = tagService.updateTag(uid, tagId, request);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(TagSuccessCode.TAG_UPDATE_200, response)
        );
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId
    ) {
        tagService.deleteTag(uid, tagId);
        return ResponseEntity.noContent().build();
    }
}