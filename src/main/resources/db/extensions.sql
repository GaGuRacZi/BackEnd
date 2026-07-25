-- Runs at app startup before Hibernate DDL (spring.sql.init).
-- RDS master is the app role, so CREATE EXTENSION is allowed; IF NOT EXISTS keeps it idempotent.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS vector;
