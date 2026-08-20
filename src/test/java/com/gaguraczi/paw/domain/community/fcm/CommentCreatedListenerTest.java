package com.gaguraczi.paw.domain.community.fcm;

import com.gaguraczi.paw.domain.community.entity.Comment;
import com.gaguraczi.paw.domain.community.event.CommentCreatedEvent;
import com.gaguraczi.paw.domain.community.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentCreatedListenerTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommunityFcmService communityFcmService;

    @InjectMocks
    private CommentCreatedListener listener;

    @Test
    void 댓글을_다시_로드해_알림한다() {
        Comment comment = mock(Comment.class);
        when(commentRepository.findDetailById(9L)).thenReturn(Optional.of(comment));

        listener.onCommentCreated(new CommentCreatedEvent(9L));

        verify(communityFcmService).notifyCommentCreated(comment);
    }

    @Test
    void commentId가_없으면_알림하지_않는다() {
        listener.onCommentCreated(new CommentCreatedEvent(null));

        verify(communityFcmService, never()).notifyCommentCreated(any());
        verify(commentRepository, never()).findDetailById(any());
    }
}
