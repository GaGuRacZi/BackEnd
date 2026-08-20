-- Incomplete timer rows were stored in DB; in-progress walks now live in Redis only.
-- Idempotent for fresh DBs where Hibernate has not created the tables yet.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema()
      AND table_name = 'walk'
  ) THEN
    DELETE FROM walk WHERE walk_status = 'IN_PROGRESS';
    ALTER TABLE walk DROP COLUMN IF EXISTS course_id;
  END IF;
END $$;

DROP TABLE IF EXISTS walk_course;
