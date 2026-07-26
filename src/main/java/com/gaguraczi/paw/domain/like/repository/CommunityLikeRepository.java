package com.gaguraczi.paw.domain.like.repository;

import com.gaguraczi.paw.domain.like.entity.CommunityLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    Optional<CommunityLike> findByCommunity_PostIdAndUser_Uid(Long postId, UUID uid);
}
