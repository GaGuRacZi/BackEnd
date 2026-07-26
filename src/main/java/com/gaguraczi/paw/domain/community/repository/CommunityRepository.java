package com.gaguraczi.paw.domain.community.repository;

import com.gaguraczi.paw.domain.community.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
}
