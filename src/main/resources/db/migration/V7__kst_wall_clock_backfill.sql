-- Convert UTC wall-clock `timestamp without time zone` values to KST (+9h).
-- Do not touch timestamptz (absolute instants) or client/Clock-already-KST columns
-- (subscription period_*, payment_history.paid_at, notification.read_at, pet_weight.recorded_at).

-- Walk timer rows: server now() was UTC, so start_time ≈ created_at. Client-sent KST times are ~9h ahead of created_at.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema() AND table_name = 'walk'
  ) THEN
    UPDATE walk
    SET
      start_time = start_time + interval '9 hours',
      end_time = CASE
        WHEN end_time IS NULL THEN NULL
        ELSE end_time + interval '9 hours'
      END,
      walk_date = (start_time + interval '9 hours')::date
    WHERE start_time IS NOT NULL
      AND created_at IS NOT NULL
      AND abs(extract(epoch FROM (start_time - created_at))) <= 120;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'chat_room'
      AND column_name = 'last_message_at'
      AND data_type = 'timestamp without time zone'
  ) THEN
    UPDATE chat_room
    SET last_message_at = last_message_at + interval '9 hours'
    WHERE last_message_at IS NOT NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'users'
      AND column_name = 'deleted_at'
      AND data_type = 'timestamp without time zone'
  ) THEN
    UPDATE users
    SET deleted_at = deleted_at + interval '9 hours'
    WHERE deleted_at IS NOT NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'todo_date'
      AND column_name = 'completed_at'
      AND data_type = 'timestamp without time zone'
  ) THEN
    UPDATE todo_date
    SET completed_at = completed_at + interval '9 hours'
    WHERE completed_at IS NOT NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'visit'
      AND column_name = 'ai_summary_generated_at'
      AND data_type = 'timestamp without time zone'
  ) THEN
    UPDATE visit
    SET ai_summary_generated_at = ai_summary_generated_at + interval '9 hours'
    WHERE ai_summary_generated_at IS NOT NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'user_agreement'
      AND column_name = 'agreed_at'
      AND data_type = 'timestamp without time zone'
  ) THEN
    UPDATE user_agreement
    SET agreed_at = agreed_at + interval '9 hours'
    WHERE agreed_at IS NOT NULL;
  END IF;
END $$;

-- Hibernate @CreationTimestamp / @UpdateTimestamp wall clocks (UTC on AWS).
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT table_name, column_name
    FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND column_name IN ('created_at', 'updated_at')
      AND data_type = 'timestamp without time zone'
  LOOP
    EXECUTE format(
      'UPDATE %I SET %I = %I + interval ''9 hours'' WHERE %I IS NOT NULL',
      r.table_name, r.column_name, r.column_name, r.column_name
    );
  END LOOP;
END $$;
