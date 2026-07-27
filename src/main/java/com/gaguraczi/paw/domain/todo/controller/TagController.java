package com.gaguraczi.paw.domain.todo.controller;

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
                .body(ApiResponse.success(response));
    }


    @GetMapping
    public ResponseEntity<List<TagResponse>> getMyTags(
            @AuthenticationPrincipal String uid
    ) {
        return ResponseEntity.ok(tagService.getTagsByUser(uid));
    }


    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(tagService.getTag(uid, tagId));
    }


    @PatchMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return ResponseEntity.ok(tagService.updateTag(uid, tagId, request));
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