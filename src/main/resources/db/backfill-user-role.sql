-- Backfill NULL roles before Hibernate applies NOT NULL via ddl-auto=update.
-- Safe on fresh DBs where users table does not exist yet.
-- Ends with ;; (spring.sql.init.separator) so ScriptUtils does not split on PL/pgSQL semicolons.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema()
      AND table_name = 'users'
  ) THEN
    UPDATE users SET role = 'USER' WHERE role IS NULL;
  END IF;
END $$;;
