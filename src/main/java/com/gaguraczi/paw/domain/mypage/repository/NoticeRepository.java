package com.gaguraczi.paw.domain.mypage.repository;

import com.gaguraczi.paw.domain.mypage.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            SELECT n FROM Notice n
            WHERE (:#{#keyword == null} = true OR n.title LIKE CONCAT('%', CAST(:keyword AS string), '%'))
              AND (
                    :#{#cursorCreatedAt == null} = true
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

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = COALESCE(n.viewCount, 0) + 1 WHERE n.noticeId = :noticeId")
    int increaseViewCount(@Param("noticeId") Long noticeId);
}
