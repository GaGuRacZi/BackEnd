package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import com.gaguraczi.paw.domain.todo.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * 태그 API
 *
 * 설계 원칙
 * - uid는 URL에 노출하지 않고 인증 주체(@AuthenticationPrincipal)에서 획득한다.
 * - 소유권 검증 / 중복 검사 등 비즈니스 규칙은 전부 Service에 위임한다.
 * - Entity를 직접 반환하지 않고 DTO로만 응답한다.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 태그 생성
     * 201 Created + Location 헤더
     */
    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody TagCreateRequest request
    ) {
        TagResponse response = tagService.createTag(uid, request);

        return ResponseEntity
                .created(URI.create("/api/tags/" + response.tagId()))
                .body(response);
    }

    /**
     * 내 태그 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> getMyTags(
            @AuthenticationPrincipal String uid
    ) {
        return ResponseEntity.ok(tagService.getTagsByUser(uid));
    }

    /**
     * 태그 단건 조회
     */
    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(tagService.getTag(uid, tagId));
    }

    /**
     * 태그 수정 (이름 / 색상 부분 수정)
     */
    @PatchMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return ResponseEntity.ok(tagService.updateTag(uid, tagId, request));
    }

    /**
     * 태그 삭제
     * 204 No Content
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @AuthenticationPrincipal String uid,
            @PathVariable Long tagId
    ) {
        tagService.deleteTag(uid, tagId);
        return ResponseEntity.noContent().build();
    }
}