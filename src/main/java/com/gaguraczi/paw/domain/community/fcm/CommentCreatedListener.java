package com.gaguraczi.paw.domain.community.fcm;

import com.gaguraczi.paw.domain.community.event.CommentCreatedEvent;
import com.gaguraczi.paw.domain.community.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentCreatedListener {

    private final CommentRepository commentRepository;
    private final CommunityFcmService communityFcmService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCommentCreated(CommentCreatedEvent event) {
        if (event == null || event.commentId() == null) {
            return;
        }
        commentRepository.findDetailById(event.commentId())
                .ifPresent(communityFcmService::notifyCommentCreated);
    }
}
