package com.gaguraczi.paw.domain.billing.repository;

import com.gaguraczi.paw.domain.billing.entity.Subscription;
import com.gaguraczi.paw.domain.users.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.user.uid = :uid")
    Optional<Subscription> findByUserUidForUpdate(@Param("uid") UUID uid);

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.user
            WHERE s.periodEnd IS NOT NULL
              AND s.periodEnd <= :now
            """)
    List<Subscription> findDue(@Param("now") LocalDateTime now);
}
