package com.gaguraczi.paw.domain.chat.repository;

import com.gaguraczi.paw.domain.chat.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    Optional<ChatRoomParticipant> findByRoom_RoomIdAndUser_Uid(Long roomId, UUID uid);
}
