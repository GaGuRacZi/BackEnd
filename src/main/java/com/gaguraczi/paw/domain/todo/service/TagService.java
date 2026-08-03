package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface TagService {
    @Transactional

    TagResponse createTag(String uid, TagCreateRequest request);

    List<TagResponse> getTagsByUser(String uid);

    TagResponse getTag(String uid, Long tagId);

    TagResponse updateTag(String uid, Long tagId, TagUpdateRequest request);

    void deleteTag(String uid, Long tagId);
}
