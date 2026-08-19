package com.gaguraczi.paw.domain.chat.repository;

import com.gaguraczi.paw.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"sender"})
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.room.roomId = :roomId
              AND (:cursor IS NULL OR m.messageId < :cursor)
            ORDER BY m.messageId DESC
            """)
    List<ChatMessage> findPageByRoom(
            @Param("roomId") Long roomId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    boolean existsByMessageIdAndRoom_RoomId(Long messageId, Long roomId);

    interface UnreadCount {
        Long getRoomId();

        Long getCnt();
    }

    @Query("""
            SELECT m.room.roomId AS roomId, COUNT(m) AS cnt
            FROM ChatMessage m
            WHERE m.room.roomId IN :roomIds
              AND m.sender.uid <> :uid
              AND m.messageId > COALESCE((
                    SELECT p.lastReadMessageId
                    FROM ChatRoomParticipant p
                    WHERE p.room.roomId = m.room.roomId AND p.user.uid = :uid
              ), 0)
            GROUP BY m.room.roomId
            """)
    List<UnreadCount> countUnreadByRooms(@Param("roomIds") List<Long> roomIds, @Param("uid") UUID uid);
}
