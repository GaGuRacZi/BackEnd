-- Comment cursor pagination index (post_id, created_at, comment_id).
-- Replaces legacy single-column idx_comment_post_id.
-- For large production tables, prefer manual:
--   CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_comment_post_created_id ON public.comment (post_id, created_at, comment_id);
--   DROP INDEX CONCURRENTLY IF EXISTS idx_comment_post_id;

DROP INDEX IF EXISTS idx_comment_post_id;;

CREATE INDEX IF NOT EXISTS idx_comment_post_created_id
    ON public.comment (post_id, created_at, comment_id);;
