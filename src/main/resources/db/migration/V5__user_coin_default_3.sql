-- New users start with BASIC's 3 coins. Existing balances are left unchanged.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'users'
      AND column_name = 'coin'
  ) THEN
    ALTER TABLE users ALTER COLUMN coin SET DEFAULT 3;
  END IF;
END $$;
