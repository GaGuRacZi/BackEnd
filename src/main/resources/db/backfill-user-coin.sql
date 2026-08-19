-- Backfill NULL coin balances before Hibernate applies NOT NULL via ddl-auto=update.
-- Existing unknown balances stay 0 (matches User.coinBalance / usedCoinBalance).
-- New rows use DB defaults coin=10, used_coin=0.
-- Ends with ;; (spring.sql.init.separator).
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'users'
      AND column_name = 'coin'
  ) THEN
    UPDATE users SET coin = 0 WHERE coin IS NULL;
    ALTER TABLE users ALTER COLUMN coin SET DEFAULT 10;
    ALTER TABLE users ALTER COLUMN coin SET NOT NULL;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'users'
      AND column_name = 'used_coin'
  ) THEN
    UPDATE users SET used_coin = 0 WHERE used_coin IS NULL;
    ALTER TABLE users ALTER COLUMN used_coin SET DEFAULT 0;
    ALTER TABLE users ALTER COLUMN used_coin SET NOT NULL;
  END IF;
END $$;;
