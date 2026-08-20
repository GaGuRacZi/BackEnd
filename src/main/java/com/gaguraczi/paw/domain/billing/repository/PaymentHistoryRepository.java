package com.gaguraczi.paw.domain.billing.repository;

import com.gaguraczi.paw.domain.billing.entity.PaymentHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    @Query("""
            SELECT p FROM PaymentHistory p
            WHERE p.user.uid = :uid
              AND (
                    :#{#cursorCreatedAt == null} = true
                    OR p.createdAt < :cursorCreatedAt
                    OR (p.createdAt = :cursorCreatedAt AND p.paymentId < :cursorPaymentId)
                  )
            ORDER BY p.createdAt DESC, p.paymentId DESC
            """)
    List<PaymentHistory> findMyPayments(
            @Param("uid") UUID uid,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPaymentId") Long cursorPaymentId,
            Pageable pageable
    );

    Optional<PaymentHistory> findByPaymentIdAndUser_Uid(Long paymentId, UUID uid);
}
