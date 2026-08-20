package com.gaguraczi.paw.domain.chat.entity;

import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/** postId는 FK가 아닌 단순 참조. 게시글이 삭제돼도 채팅방은 남아야 해서 매번 실시간 조회한다. */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_room",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_post_buyer", columnNames = {"post_id", "buyer_uid"}),
        indexes = {
                @Index(name = "idx_chat_room_seller_last_msg", columnList = "seller_uid, last_message_at"),
                @Index(name = "idx_chat_room_buyer_last_msg", columnList = "buyer_uid, last_message_at")
        }
)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_uid", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_uid", nullable = false)
    private User buyer;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    public boolean hasParticipant(UUID uid) {
        return seller.getUid().equals(uid) || buyer.getUid().equals(uid);
    }

    public User opponentOf(UUID uid) {
        return buyer.getUid().equals(uid) ? seller : buyer;
    }

    public void recordLastMessage(String preview, LocalDateTime at) {
        this.lastMessagePreview = preview;
        this.lastMessageAt = at;
    }
}
