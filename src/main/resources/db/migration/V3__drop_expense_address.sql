-- Expense records no longer store a hospital address.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'expense'
      AND column_name = 'expense_address'
  ) THEN
    ALTER TABLE expense DROP COLUMN expense_address;
  END IF;
END $$;
