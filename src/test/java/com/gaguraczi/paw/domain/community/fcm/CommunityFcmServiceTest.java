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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityFcmServiceTest {

    @Mock
    private NotificationSettingService notificationSettingService;
    @Mock
    private NotificationPolicy notificationPolicy;
    @Mock
    private NotificationInboxService notificationInboxService;
    @Mock
    private FcmPushService fcmPushService;

    @InjectMocks
    private CommunityFcmService communityFcmService;

    @Test
    void 내_글_댓글은_작성자에게_보낸다() {
        User author = User.builder().uid(UUID.randomUUID()).build();
        User postOwner = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        Community post = mock(Community.class);
        Comment comment = mock(Comment.class);
        NotificationSetting setting = NotificationSetting.builder().user(postOwner).communityAlarm(true).build();
        when(post.getPostId()).thenReturn(3L);
        when(post.getPostType()).thenReturn(PostType.COMMUNICATION);
        when(post.getUser()).thenReturn(postOwner);
        when(comment.getCommunity()).thenReturn(post);
        when(comment.getUser()).thenReturn(author);
        when(comment.getParent()).thenReturn(null);
        when(comment.getContent()).thenReturn("오메가3는 식후 급여가 좋아요");
        when(notificationSettingService.getOrCreate(postOwner)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.COMMUNITY)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.COMMUNITY)).thenReturn(true);

        communityFcmService.notifyCommentCreated(comment);

        verify(notificationInboxService).insert(
                eq(postOwner.getUid()),
                eq(NotificationCategory.COMMUNITY),
                eq("내 게시글에 댓글이 달렸어요"),
                eq("\"오메가3는 식후 급여가 좋아요\""),
                eq(NotificationTargetType.POST),
                eq(3L),
                eq(null),
                eq("글 보기")
        );
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(fcmPushService).send(eq("tok"), eq("내 게시글에 댓글이 달렸어요"), eq("\"오메가3는 식후 급여가 좋아요\""), data.capture());
        assertThat(data.getValue()).containsEntry("type", "COMMUNITY_COMMENT").containsEntry("postId", "3");
    }

    @Test
    void 자기_댓글은_보내지_않는다() {
        User owner = User.builder().uid(UUID.randomUUID()).build();
        Community post = mock(Community.class);
        Comment comment = mock(Comment.class);
        when(post.getPostType()).thenReturn(PostType.COMMUNICATION);
        when(post.getUser()).thenReturn(owner);
        when(comment.getCommunity()).thenReturn(post);
        when(comment.getUser()).thenReturn(owner);
        when(comment.getParent()).thenReturn(null);
        when(comment.getContent()).thenReturn("hi");

        communityFcmService.notifyCommentCreated(comment);

        verify(notificationInboxService, never()).insert(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void 장터_답글은_확인_CTA와_고정_본문을_쓴다() {
        User seller = User.builder().uid(UUID.randomUUID()).build();
        User inquirer = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        User replier = User.builder().uid(UUID.randomUUID()).build();
        Community post = mock(Community.class);
        Comment parent = mock(Comment.class);
        Comment reply = mock(Comment.class);
        NotificationSetting setting = NotificationSetting.builder().user(inquirer).communityAlarm(true).build();
        when(post.getPostId()).thenReturn(9L);
        when(post.getPostType()).thenReturn(PostType.MARKET);
        when(post.getUser()).thenReturn(seller);
        when(parent.getUser()).thenReturn(inquirer);
        when(reply.getCommunity()).thenReturn(post);
        when(reply.getUser()).thenReturn(replier);
        when(reply.getParent()).thenReturn(parent);
        when(reply.getContent()).thenReturn("오늘 저녁 가능해요");
        when(notificationSettingService.getOrCreate(inquirer)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.COMMUNITY)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.COMMUNITY)).thenReturn(true);
        NotificationSetting sellerSetting = NotificationSetting.builder().user(seller).communityAlarm(true).build();
        when(notificationSettingService.getOrCreate(seller)).thenReturn(sellerSetting);
        when(notificationPolicy.allowInbox(sellerSetting, NotificationCategory.COMMUNITY)).thenReturn(false);

        communityFcmService.notifyCommentCreated(reply);

        verify(notificationInboxService).insert(
                eq(inquirer.getUid()),
                eq(NotificationCategory.COMMUNITY),
                eq("나눔 장터 문의에 답글이 왔어요"),
                eq("나눔 가능 시간 확인해보세요"),
                eq(NotificationTargetType.POST),
                eq(9L),
                eq(null),
                eq("확인")
        );
    }

    @Test
    void 댓글_앞_40자만_본문에_넣는다() {
        String longText = "가".repeat(50);
        assertThat(CommunityFcmService.quoteExcerpt(longText)).isEqualTo("\"" + "가".repeat(40) + "\"");
    }
}
