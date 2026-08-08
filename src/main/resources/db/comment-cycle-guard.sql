-- Prevent circular parent references on comment.self-join.
-- Prefer CommunitySchemaInitializer (runs after Hibernate DDL).
CREATE OR REPLACE FUNCTION prevent_comment_cycle()
RETURNS TRIGGER AS $$
DECLARE
  current_id BIGINT;
  walk_depth INT := 0;
BEGIN
  IF NEW.parent_id IS NULL THEN
    RETURN NEW;
  END IF;

  IF NEW.comment_id IS NOT NULL AND NEW.parent_id = NEW.comment_id THEN
    RAISE EXCEPTION 'comment cannot be its own parent';
  END IF;

  current_id := NEW.parent_id;
  WHILE current_id IS NOT NULL LOOP
    IF NEW.comment_id IS NOT NULL AND current_id = NEW.comment_id THEN
      RAISE EXCEPTION 'circular comment reference detected';
    END IF;
    walk_depth := walk_depth + 1;
    IF walk_depth > 100 THEN
      RAISE EXCEPTION 'comment parent chain too deep';
    END IF;
    SELECT parent_id INTO current_id FROM comment WHERE comment_id = current_id;
  END LOOP;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema()
      AND table_name = 'comment'
  ) THEN
    DROP TRIGGER IF EXISTS trg_prevent_comment_cycle ON comment;
    CREATE TRIGGER trg_prevent_comment_cycle
      BEFORE INSERT OR UPDATE OF parent_id ON comment
      FOR EACH ROW
      EXECUTE PROCEDURE prevent_comment_cycle();
  END IF;
END $$;;
