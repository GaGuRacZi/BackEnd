package com.gaguraczi.paw.domain.mypage.repository;

import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("""
            SELECT i FROM Inquiry i
            WHERE i.user.uid = :uid
              AND (
                    :#{#cursorCreatedAt == null} = true
                    OR i.createdAt < :cursorCreatedAt
                    OR (i.createdAt = :cursorCreatedAt AND i.inquiryId < :cursorInquiryId)
                  )
            ORDER BY i.createdAt DESC, i.inquiryId DESC
            """)
    List<Inquiry> findMyInquiries(
            @Param("uid") UUID uid,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorInquiryId") Long cursorInquiryId,
            Pageable pageable
    );

    @Query("""
            SELECT i FROM Inquiry i
            JOIN FETCH i.user
            WHERE (:#{#status == null} = true OR i.status = :status)
              AND (:#{#inquiryType == null} = true OR i.inquiryType = :inquiryType)
              AND (
                    :#{#cursorCreatedAt == null} = true
                    OR i.createdAt < :cursorCreatedAt
                    OR (i.createdAt = :cursorCreatedAt AND i.inquiryId < :cursorInquiryId)
                  )
            ORDER BY i.createdAt DESC, i.inquiryId DESC
            """)
    List<Inquiry> findAllForAdmin(
            @Param("status") InquiryStatus status,
            @Param("inquiryType") InquiryType inquiryType,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorInquiryId") Long cursorInquiryId,
            Pageable pageable
    );

    @Query("SELECT i FROM Inquiry i JOIN FETCH i.user WHERE i.inquiryId = :inquiryId")
    Optional<Inquiry> findByIdWithUser(@Param("inquiryId") Long inquiryId);
}
