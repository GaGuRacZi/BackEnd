package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.todo.exception.code.TagErrorCode;
import com.gaguraczi.paw.domain.todo.repository.TagRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
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
    private final UserRepository userRepository;


    private UUID toUserId(String uid) {
        try {
            return UUID.fromString(uid);
        } catch (IllegalArgumentException e) {
            throw new GeneralException(TagErrorCode.TAG_GET_404_2);
        }
    }


    private TagEntity getMyTagOrThrow(UUID userId, Long tagId, BaseErrorCode errorCode) {
        return tagRepository.findByTagIdAndUser_Uid(tagId, userId)
                .orElseThrow(() -> new GeneralException(errorCode));
    }

    @Transactional
    public TagResponse createTag(String uid, TagCreateRequest request) {

        UUID userId = toUserId(uid);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(TagErrorCode.TAG_GET_404_2));

        String tagName = request.tagName().trim();


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

    public List<TagResponse> getTagsByUser(String uid) {
        return tagRepository.findAllByUser_UidOrderByTagNameAsc(toUserId(uid)).stream()
                .map(TagResponse::from)
                .toList();
    }

    public TagResponse getTag(String uid, Long tagId) {
        TagEntity tag = getMyTagOrThrow(toUserId(uid), tagId, TagErrorCode.TAG_GET_404_2);
        return TagResponse.from(tag);
    }

    @Transactional
    public TagResponse updateTag(String uid, Long tagId, TagUpdateRequest request) {

        UUID userId = toUserId(uid);

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

    }
