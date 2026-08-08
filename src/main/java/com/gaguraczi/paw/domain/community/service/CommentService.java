package com.gaguraczi.paw.domain.community.service;

import com.gaguraczi.paw.domain.community.dto.req.CommentCreateReq;
import com.gaguraczi.paw.domain.community.dto.req.CommentUpdateReq;
import com.gaguraczi.paw.domain.community.dto.res.CommentRes;
import com.gaguraczi.paw.domain.community.entity.Comment;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.domain.community.repository.CommentRepository;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.community.support.CommentCursorCodec;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Set<PostType> SUPPORTED = EnumSet.of(PostType.COMMUNICATION, PostType.MARKET);
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public CursorPageRes<CommentRes> list(Long postId, String cursor, Integer size) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMUNITY_NOT_FOUND_404));
        assertSupported(community);

        int pageSize = normalizeSize(size);
        CommentCursorCodec.Cursor decoded = CommentCursorCodec.decode(cursor);
        List<Comment> rows = commentRepository.findByPost(
                postId,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.commentId(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<Comment> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<CommentRes> content = new ArrayList<>(page.size());
        for (Comment comment : page) {
            content.add(CommentRes.from(comment));
        }
        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            Comment last = page.getLast();
            nextCursor = CommentCursorCodec.encode(last.getCreatedAt(), last.getCommentId());
        }
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    @Transactional
    public CommentRes create(Long postId, CommentCreateReq req) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMUNITY_NOT_FOUND_404));
        assertSupported(community);
        User user = securityUtils.currentUser();

        Comment parent = null;
        if (req.parentId() != null) {
            parent = commentRepository.findDetailById(req.parentId())
                    .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMENT_NOT_FOUND_404));
            if (!Objects.equals(parent.getCommunity().getPostId(), postId)) {
                throw GeneralException.of(CommunityErrorCode.COMMENT_POST_MISMATCH_400);
            }
        }

        Comment comment = Comment.builder()
                .community(community)
                .user(user)
                .content(req.content())
                .build();
        if (parent != null) {
            comment.attachTo(parent);
        }
        commentRepository.save(comment);
        community.increaseCommentCount();
        return CommentRes.from(comment);
    }

    @Transactional
    public CommentRes update(Long commentId, CommentUpdateReq req) {
        Comment comment = commentRepository.findDetailById(commentId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMENT_NOT_FOUND_404));
        assertAuthor(comment);
        if (Boolean.TRUE.equals(comment.getIsDel())) {
            throw GeneralException.of(CommunityErrorCode.COMMENT_NOT_FOUND_404);
        }
        comment.updateContent(req.content());
        return CommentRes.from(comment);
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findDetailById(commentId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMENT_NOT_FOUND_404));
        assertAuthor(comment);
        if (Boolean.TRUE.equals(comment.getIsDel())) {
            return;
        }
        comment.softDelete();
        comment.getCommunity().decreaseCommentCount();
    }

    private void assertAuthor(Comment comment) {
        UUID uid = securityUtils.currentUid();
        if (!Objects.equals(comment.getUser().getUid(), uid)) {
            throw GeneralException.of(CommunityErrorCode.FORBIDDEN_403);
        }
    }

    private void assertSupported(Community community) {
        if (!SUPPORTED.contains(community.getPostType())) {
            throw GeneralException.of(CommunityErrorCode.POST_TYPE_UNSUPPORTED_400);
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
