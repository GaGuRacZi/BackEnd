package com.gaguraczi.paw.domain.users.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AdminUserHardDeleteJdbcRepository {

    private static final String[] DELETE_SQL = {
            """
            DELETE FROM chat_message
            WHERE room_id IN (SELECT room_id FROM chat_room WHERE seller_uid = ? OR buyer_uid = ?)
               OR sender_uid = ?
            """,
            """
            DELETE FROM chat_room_participant
            WHERE uid = ?
               OR room_id IN (SELECT room_id FROM chat_room WHERE seller_uid = ? OR buyer_uid = ?)
            """,
            "DELETE FROM chat_room WHERE seller_uid = ? OR buyer_uid = ?",
            """
            WITH RECURSIVE doomed AS (
              SELECT comment_id FROM comment
              WHERE uid = ? OR post_id IN (SELECT post_id FROM community WHERE uid = ?)
              UNION
              SELECT c.comment_id FROM comment c
              INNER JOIN doomed d ON c.parent_id = d.comment_id
            )
            DELETE FROM comment WHERE comment_id IN (SELECT comment_id FROM doomed)
            """,
            """
            DELETE FROM community_like
            WHERE uid = ? OR post_id IN (SELECT post_id FROM community WHERE uid = ?)
            """,
            "DELETE FROM community_photo WHERE post_id IN (SELECT post_id FROM community WHERE uid = ?)",
            "DELETE FROM community WHERE uid = ?",
            """
            DELETE FROM pet_weight_photo
            WHERE pet_weight_id IN (
              SELECT pet_weight_id FROM pet_weight WHERE pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)
            )
            """,
            "DELETE FROM pet_weight WHERE pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)",
            """
            DELETE FROM expense_detail
            WHERE expense_id IN (
              SELECT expense_id FROM expense WHERE pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)
            )
            """,
            "DELETE FROM expense WHERE pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)",
            "DELETE FROM walk WHERE pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)",
            """
            DELETE FROM visit_transcript_turn
            WHERE visit_id IN (
              SELECT visit_id FROM visit WHERE uid = ? OR pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)
            )
            """,
            """
            DELETE FROM visit_prescription
            WHERE visit_id IN (
              SELECT visit_id FROM visit WHERE uid = ? OR pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)
            )
            """,
            "DELETE FROM visit WHERE uid = ? OR pet_id IN (SELECT pet_id FROM pet WHERE uid = ?)",
            "DELETE FROM pet WHERE uid = ?",
            "DELETE FROM payment_history WHERE uid = ?",
            "DELETE FROM subscription WHERE uid = ?",
            "DELETE FROM oauth WHERE uid = ?",
            "DELETE FROM inquiry WHERE uid = ?",
            "DELETE FROM notification WHERE uid = ?",
            "DELETE FROM notification_setting WHERE uid = ?",
            "DELETE FROM user_agreement WHERE uid = ?",
            "DELETE FROM todo_date WHERE todo_id IN (SELECT todo_id FROM todo WHERE uid = ?)",
            "DELETE FROM todo WHERE uid = ?",
            "DELETE FROM tag WHERE uid = ?",
            "DELETE FROM users WHERE uid = ?"
    };

    private final JdbcTemplate jdbcTemplate;

    public void deleteAllByUid(UUID uid) {
        for (String sql : DELETE_SQL) {
            jdbcTemplate.update(sql, args(sql, uid));
        }
    }

    private static Object[] args(String sql, UUID uid) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        Object[] values = new Object[count];
        java.util.Arrays.fill(values, uid);
        return values;
    }
}
