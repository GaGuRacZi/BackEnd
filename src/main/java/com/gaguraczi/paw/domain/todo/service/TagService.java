package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TagCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TagUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TagResponse;
import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColor;
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


    private TagEntity getMyTagOrThrow(String uid, Long tagId, BaseErrorCode TagErrorCode) {
        return tagRepository.findByTagIdAndUser_Uid(tagId, uid)
                .orElseThrow(() -> new GeneralException(TagErrorCode));
    }



    @Transactional
    public TagResponse createTag(String uid, TagCreateRequest request) {

        User user = userRepository.findById(UUID.fromString(uid))
                .orElseThrow(() -> new GeneralException(TagErrorCode.TAG_GET_404_2));

        String tagName = request.tagName().trim();

        if (tagRepository.existsByUser_UidAndTagName(uid, tagName)) {
            throw new GeneralException(TagErrorCode.TAG_CREATE_400_1);
        }

        TagEntity tag = TagEntity.create(user, tagName, request.tagColor());

        try {
            tagRepository.save(tag);
        } catch (DataIntegrityViolationException e) {

            throw new GeneralException(TagErrorCode.TAG_CREATE_400_1);
        }

        return TagResponse.from(tag);
    }

    public List<TagResponse> getTagsByUser(String uid) {
        return tagRepository.findAllByUser_UidOrderByTagNameAsc(uid).stream()
                .map(TagResponse::from)
                .toList();
    }

    public TagResponse getTag(String uid, Long tagId) {
        TagEntity tag = getMyTagOrThrow(uid, tagId, TagErrorCode.TAG_GET_404_2);
        return TagResponse.from(tag);
    }

    @Transactional
    public TagResponse updateTag(String uid, Long tagId, TagUpdateRequest request) {

        if (request.isEmpty()) {
            throw new GeneralException(TagErrorCode.TAG_UPDATE_400_2);
        }

        TagEntity tag = getMyTagOrThrow(uid, tagId, TagErrorCode.TAG_UPDATE_400_2);

        String tagName = (request.tagName() == null) ? tag.getTagName() : request.tagName();
        TagColor tagColor = (request.tagColor() == null) ? tag.getTagColor() : request.tagColor();

        if (!tagName.equals(tag.getTagName())
                && tagRepository.existsByUser_UidAndTagNameAndTagIdNot(uid, tagName, tagId)) {
            throw new GeneralException(TagErrorCode.TAG_UPDATE_400_2);
        }


        try {
            tagRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(TagErrorCode.TAG_UPDATE_400_2);
        }

        return TagResponse.from(tag);
    }

    @Transactional
    public void deleteTag(String uid, Long tagId) {
        TagEntity tag = getMyTagOrThrow(uid, tagId, TagErrorCode.TAG_GET_404_2);
        tagRepository.delete(tag);
    }
}