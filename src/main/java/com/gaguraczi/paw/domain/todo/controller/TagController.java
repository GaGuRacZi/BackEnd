package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import com.gaguraczi.paw.domain.todo.exception.code.TagSuccessCode;
import com.gaguraczi.paw.domain.todo.service.TagService;
import com.gaguraczi.paw.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ApiResponse<TagResponse> createTag(
            @Valid @RequestBody TagCreateRequest request
    ) {
        TagResponse response = tagService.create(request);

        return ApiResponse.onSuccess(TagSuccessCode.TAG_CREATE_201, response);
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> getMyTags() {
        return ApiResponse.onSuccess(TagSuccessCode.TAG_LIST_200, tagService.getMyTags()
        );
    }

    @GetMapping("/{tagId}")
    public ApiResponse<TagResponse> getTag(@PathVariable Long tagId) {
        return ApiResponse.onSuccess(TagSuccessCode.TAG_GET_200, tagService.getTag(tagId))
        ;
    }

    @PatchMapping("/{tagId}")
    public ApiResponse<TagResponse> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return ApiResponse.onSuccess(TagSuccessCode.TAG_UPDATE_200, tagService.updateTag(tagId, request))
        ;
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        tagService.deleteTag(tagId, force);
        return ApiResponse.onSuccess(TagSuccessCode.TAG_DELETE_200, null);
    }
}