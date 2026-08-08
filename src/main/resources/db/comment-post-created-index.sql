-- Comment cursor pagination index (post_id, created_at, comment_id).
-- Replaces legacy single-column idx_comment_post_id.
-- CREATE/DROP INDEX CONCURRENTLY must run outside a transaction.

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_comment_post_created_id
    ON public.comment (post_id, created_at, comment_id);;

DROP INDEX CONCURRENTLY IF EXISTS idx_comment_post_id;;
