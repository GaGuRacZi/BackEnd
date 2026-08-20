package com.gaguraczi.paw.domain.community.service;

import com.gaguraczi.paw.domain.community.dto.req.CommentCreateReq;
import com.gaguraczi.paw.domain.community.entity.Comment;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.event.CommentCreatedEvent;
import com.gaguraczi.paw.domain.community.repository.CommentRepository;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CommentService commentService;

    @Test
    void 댓글_저장_후_FCM은_이벤트로만_발행한다() {
        User user = User.builder().uid(UUID.randomUUID()).nickname("닉").build();
        Community community = mock(Community.class);
        when(community.getPostType()).thenReturn(PostType.COMMUNICATION);
        when(community.getPostId()).thenReturn(10L);
        when(communityRepository.findById(10L)).thenReturn(Optional.of(community));
        when(securityUtils.currentUser()).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "commentId", 99L);
            return saved;
        });

        commentService.create(10L, new CommentCreateReq("좋은 정보 감사합니다", null));

        ArgumentCaptor<CommentCreatedEvent> captor = ArgumentCaptor.forClass(CommentCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().commentId()).isEqualTo(99L);
        verify(communityRepository).increaseCommentCount(10L);
    }
}
