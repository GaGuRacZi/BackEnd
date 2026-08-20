-- Removed pet features: animal registration, blood type, food ingredients,
-- surgery history, and care areas. Hibernate ddl-auto=update does not DROP.

DROP TABLE IF EXISTS pet_care_selection;;

DROP TABLE IF EXISTS pet_care_code;;

DROP TABLE IF EXISTS pet_registration;;

ALTER TABLE pet DROP COLUMN IF EXISTS blood_type;;
