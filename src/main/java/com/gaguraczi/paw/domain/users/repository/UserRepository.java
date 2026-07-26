package com.gaguraczi.paw.domain.users.repository;

import com.gaguraczi.paw.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
