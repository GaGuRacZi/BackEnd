package com.gaguraczi.paw.domain.community.fcm;

import com.gaguraczi.paw.domain.community.entity.Comment;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.service.NotificationSettingService;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.notification.service.NotificationInboxService;
import com.gaguraczi.paw.domain.notification.service.NotificationPolicy;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.fcm.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityFcmService {

    static final String POST_COMMENT_TITLE = "내 게시글에 댓글이 달렸어요";
    static final String REPLY_TITLE = "내 댓글에 답글이 달렸어요";
    static final String MARKET_REPLY_TITLE = "나눔 장터 문의에 답글이 왔어요";
    static final String MARKET_REPLY_BODY = "나눔 가능 시간 확인해보세요";

    private final NotificationSettingService notificationSettingService;
    private final NotificationPolicy notificationPolicy;
    private final NotificationInboxService notificationInboxService;
    private final FcmPushService fcmPushService;

    public void notifyCommentCreated(Comment comment) {
        try {
            Community post = comment.getCommunity();
            User author = comment.getUser();
            UUID authorUid = author.getUid();
            boolean market = post.getPostType() == PostType.MARKET;
            String excerpt = quoteExcerpt(comment.getContent());

            if (comment.getParent() == null) {
                notifyRecipient(post.getUser(), authorUid, post.getPostId(), POST_COMMENT_TITLE, excerpt, false);
                return;
            }

            User parentAuthor = comment.getParent().getUser();
            if (market) {
                notifyRecipient(parentAuthor, authorUid, post.getPostId(), MARKET_REPLY_TITLE, MARKET_REPLY_BODY, true);
            } else {
                notifyRecipient(parentAuthor, authorUid, post.getPostId(), REPLY_TITLE, excerpt, false);
            }
            if (!Objects.equals(post.getUser().getUid(), parentAuthor.getUid())) {
                    notifyRecipient(post.getUser(), authorUid, post.getPostId(), POST_COMMENT_TITLE, excerpt, false);
            }
        } catch (Exception e) {
            log.warn("Community FCM skipped commentId={}: {}", comment.getCommentId(), e.getMessage());
        }
    }

    private void notifyRecipient(
            User recipient,
            UUID authorUid,
            Long postId,
            String title,
            String body,
            boolean marketCta
    ) {
        if (recipient == null || Objects.equals(recipient.getUid(), authorUid)) {
            return;
        }
        NotificationSetting setting = notificationSettingService.getOrCreate(recipient);
        if (!notificationPolicy.allowInbox(setting, NotificationCategory.COMMUNITY)) {
            return;
        }
        String cta = marketCta ? "확인" : "글 보기";
        notificationInboxService.insert(
                recipient.getUid(),
                NotificationCategory.COMMUNITY,
                title,
                body,
                NotificationTargetType.POST,
                postId,
                null,
                cta
        );
        if (notificationPolicy.allowFcm(setting, NotificationCategory.COMMUNITY)) {
            fcmPushService.send(recipient.getPushToken(), title, body, Map.of(
                    "type", "COMMUNITY_COMMENT",
                    "postId", String.valueOf(postId)
            ));
        }
    }

    static String quoteExcerpt(String content) {
        String text = content == null ? "" : content.strip();
        if (text.length() > 40) {
            text = text.substring(0, 40);
        }
        return "\"" + text + "\"";
    }
}
