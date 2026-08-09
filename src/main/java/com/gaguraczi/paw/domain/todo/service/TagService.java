package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.todo.exception.code.TagErrorCode;
import com.gaguraczi.paw.domain.todo.repository.TagRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final SecurityUtils securityUtils;

    private TagEntity getMyTagOrThrow(UUID userId, Long tagId, BaseErrorCode errorCode) {
        return tagRepository.findByTagIdAndUser_Uid(tagId, userId)
                .orElseThrow(() -> new GeneralException(errorCode));
    }

    @Transactional
    public TagResponse create(TagCreateRequest request) {
        User user = securityUtils.currentUser();
        UUID userId = user.getUid();

        String tagName = request.tagName().trim();
        if (tagName.isEmpty()) {
            throw new GeneralException(TagErrorCode.TAG_CREATE_400_1);
        }

        if (tagRepository.existsByUser_UidAndTagName(userId, tagName)) {
            throw new GeneralException(TagErrorCode.TAG_CREATE_400_1);
        }

        TagEntity tag = TagEntity.create(user, tagName, request.tagColorEnum());

        try {
            tagRepository.save(tag);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(TagErrorCode.TAG_CREATE_400_1);
        }

        return TagResponse.from(tag);
    }

    public List<TagResponse> getMyTags() {
        UUID userId = securityUtils.currentUser().getUid();

        return tagRepository.findAllByUser_UidOrderByTagNameAsc(userId).stream()
                .map(TagResponse::from)
                .toList();
    }

    public TagResponse getTag(Long tagId) {
        UUID userId = securityUtils.currentUser().getUid();

        return TagResponse.from(
                getMyTagOrThrow(userId, tagId, TagErrorCode.TAG_GET_404_2)
        );
    }

    @Transactional
    public TagResponse updateTag(Long tagId, TagUpdateRequest request) {
        UUID userId = securityUtils.currentUser().getUid();

        if (request.isEmpty()) {
            throw new GeneralException(TagErrorCode.TAG_UPDATE_400_2);
        }

        TagEntity tag = getMyTagOrThrow(userId, tagId, TagErrorCode.TAG_UPDATE_400_2);

        String tagName = (request.tagName() == null)
                ? tag.getTagName()
                : request.tagName().trim();
        TagColorEnum tagColor = (request.tagColorEnum() == null)
                ? tag.getTagColorEnum()
                : request.tagColorEnum();

        if (!tagName.equals(tag.getTagName())
                && tagRepository.existsByUser_UidAndTagNameAndTagIdNot(userId, tagName, tagId)) {
            throw new GeneralException(TagErrorCode.TAG_UPDATE_400_2);
        }

        tag.change(tagName, tagColor);

        try {
            tagRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(TagErrorCode.TAG_UPDATE_400_2);
        }

        return TagResponse.from(tag);
    }

    @Transactional
    public void deleteTag(Long tagId) {
        UUID userId = securityUtils.currentUser().getUid();

        TagEntity tag = getMyTagOrThrow(userId, tagId, TagErrorCode.TAG_DELETE_404_3);
        tagRepository.delete(tag);
    }
}