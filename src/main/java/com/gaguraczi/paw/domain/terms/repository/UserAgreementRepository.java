package com.gaguraczi.paw.domain.terms.repository;

import com.gaguraczi.paw.domain.terms.entity.UserAgreement;
import com.gaguraczi.paw.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    void deleteByUser(User user);

    long countByUser_Uid(UUID uid);

    List<UserAgreement> findByUser(User user);
}
