-- Backfill NULL roles before Hibernate applies NOT NULL via ddl-auto=update.
-- Safe on fresh DBs where users table does not exist yet.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema()
      AND table_name = 'users'
  ) THEN
    UPDATE users SET role = 'USER' WHERE role IS NULL;
  END IF;
END $$;
