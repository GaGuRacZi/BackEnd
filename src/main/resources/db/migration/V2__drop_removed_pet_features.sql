-- Drop removed pet features: animal registration, blood type, and care codes.
-- Hibernate ddl-auto=update does not DROP tables or columns.
--
-- Backup before applying on a populated database:
--   pg_dump -t pet_care_selection -t pet_care_code -t pet_registration > pet-features.bak.sql
--   COPY (SELECT pet_id, blood_type FROM pet WHERE blood_type IS NOT NULL) TO 'pet-blood-type.bak.csv' CSV HEADER;
--
-- Rollback requires that backup. DROP TABLE cannot be undone in place.
-- Restore dumped tables from pet-features.bak.sql.
-- Recreate blood_type from CSV:
--   ALTER TABLE pet ADD COLUMN IF NOT EXISTS blood_type varchar(32);
--   UPDATE pet p SET blood_type = b.blood_type FROM ... backup ...
--
-- Flyway runs before Hibernate DDL, so skip objects that do not exist yet.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema() AND table_name = 'pet_care_selection'
  ) THEN
    DROP TABLE pet_care_selection;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema() AND table_name = 'pet_care_code'
  ) THEN
    DROP TABLE pet_care_code;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema() AND table_name = 'pet_registration'
  ) THEN
    DROP TABLE pet_registration;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'pet'
      AND column_name = 'blood_type'
  ) THEN
    ALTER TABLE pet DROP COLUMN blood_type;
  END IF;
END $$;
