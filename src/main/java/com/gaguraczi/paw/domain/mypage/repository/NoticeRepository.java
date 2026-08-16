package com.gaguraczi.paw.domain.mypage.repository;

import com.gaguraczi.paw.domain.mypage.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            SELECT n FROM Notice n
            WHERE (:keyword IS NULL OR n.title LIKE CONCAT('%', :keyword, '%'))
              AND (
                    :cursorCreatedAt IS NULL
                    OR n.createdAt < :cursorCreatedAt
                    OR (n.createdAt = :cursorCreatedAt AND n.noticeId < :cursorNoticeId)
                  )
            ORDER BY n.createdAt DESC, n.noticeId DESC
            """)
    List<Notice> search(
            @Param("keyword") String keyword,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorNoticeId") Long cursorNoticeId,
            Pageable pageable
    );
}
