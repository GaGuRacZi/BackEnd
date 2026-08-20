package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.community.entity.Comment;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.CommunitySort;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.repository.CommentRepository;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.community.support.CommunityCursorCodec;
import com.gaguraczi.paw.domain.like.entity.CommunityLike;
import com.gaguraczi.paw.domain.like.repository.CommunityLikeRepository;
import com.gaguraczi.paw.domain.mypage.dto.res.MyCommentItemRes;
import com.gaguraczi.paw.domain.mypage.dto.res.MyPostItemRes;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageCommunityService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommentRepository commentRepository;
    private final SecurityUtils securityUtils;

    public CursorPageRes<MyPostItemRes> getMyPosts(PostType postType, String cursor, Integer size) {
        UUID uid = securityUtils.currentUid();
        List<PostType> postTypes = postType == null
                ? List.copyOf(EnumSet.allOf(PostType.class))
                : List.of(postType);
        int pageSize = normalizeSize(size);
        CommunityCursorCodec.Cursor decoded = CommunityCursorCodec.decode(cursor, CommunitySort.LATEST);

        List<Community> rows = communityRepository.findMyPosts(
                uid,
                postTypes,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.postId(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<Community> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<MyPostItemRes> content = page.stream().map(MyPostItemRes::from).toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? CommunityCursorCodec.encodeLatest(page.getLast().getCreatedAt(), page.getLast().getPostId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    public CursorPageRes<MyPostItemRes> getMyLikes(String cursor, Integer size) {
        UUID uid = securityUtils.currentUid();
        int pageSize = normalizeSize(size);
        CommunityCursorCodec.Cursor decoded = CommunityCursorCodec.decode(cursor, CommunitySort.LATEST);

        List<CommunityLike> rows = communityLikeRepository.findMyLikes(
                uid,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.postId(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<CommunityLike> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<MyPostItemRes> content = page.stream()
                .map(like -> MyPostItemRes.from(like.getCommunity()))
                .toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? CommunityCursorCodec.encodeLatest(page.getLast().getCreatedAt(), page.getLast().getCommunityLikeId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    public CursorPageRes<MyCommentItemRes> getMyComments(String cursor, Integer size) {
        UUID uid = securityUtils.currentUid();
        int pageSize = normalizeSize(size);
        CommunityCursorCodec.Cursor decoded = CommunityCursorCodec.decode(cursor, CommunitySort.LATEST);

        List<Comment> rows = commentRepository.findMyComments(
                uid,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.postId(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<Comment> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<MyCommentItemRes> content = page.stream().map(MyCommentItemRes::from).toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? CommunityCursorCodec.encodeLatest(page.getLast().getCreatedAt(), page.getLast().getCommentId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
