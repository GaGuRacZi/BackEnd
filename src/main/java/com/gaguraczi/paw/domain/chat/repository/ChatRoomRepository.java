package com.gaguraczi.paw.domain.chat.repository;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByPostIdAndBuyer_Uid(Long postId, UUID buyerUid);

    @EntityGraph(attributePaths = {"seller", "buyer"})
    Optional<ChatRoom> findWithParticipantsByRoomId(Long roomId);

    /** 메시지 전송 시 사용. 동시 전송 시 lastMessageAt/preview가 오래된 메시지로 덮어써지지 않도록 방 행을 잠근다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"seller", "buyer"})
    @Query("SELECT r FROM ChatRoom r WHERE r.roomId = :roomId")
    Optional<ChatRoom> findWithParticipantsForUpdate(@Param("roomId") Long roomId);

    @EntityGraph(attributePaths = {"seller", "buyer"})
    @Query("""
            SELECT r FROM ChatRoom r
            WHERE (r.seller.uid = :uid OR r.buyer.uid = :uid)
              AND (
                    :#{#cursorLastMessageAt == null} = true
                    OR r.lastMessageAt < :cursorLastMessageAt
                    OR (r.lastMessageAt = :cursorLastMessageAt AND r.roomId < :cursorRoomId)
                  )
            ORDER BY r.lastMessageAt DESC, r.roomId DESC
            """)
    List<ChatRoom> findRoomsByUser(
            @Param("uid") UUID uid,
            @Param("cursorLastMessageAt") LocalDateTime cursorLastMessageAt,
            @Param("cursorRoomId") Long cursorRoomId,
            Pageable pageable
    );
}
