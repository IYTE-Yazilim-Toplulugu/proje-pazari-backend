-- =============================================================================
-- Migration: Add missing tables for banned_ips, scheduled_emails, system_config
-- =============================================================================
--
-- WHEN TO RUN:
--   Production deployments where these tables are missing.
--   Safe to run multiple times — all statements use IF NOT EXISTS.
--
-- USAGE:
--   psql -U <user> -d <db> -h localhost -f migrate-missing-tables.sql
--
--   Or via Docker:
--   docker exec -i proje-pazari-db psql -U $POSTGRES_USER -d $POSTGRES_DB \
--     -f migrate-missing-tables.sql
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- banned_ips
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS banned_ips (
    id          VARCHAR(26)  NOT NULL PRIMARY KEY,
    ip_address  VARCHAR(255) NOT NULL UNIQUE,
    reason      VARCHAR(255),
    banned_by   VARCHAR(255),
    banned_at   TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- scheduled_emails
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scheduled_emails (
    id           VARCHAR(26)  NOT NULL PRIMARY KEY,
    subject      VARCHAR(255) NOT NULL,
    body         TEXT         NOT NULL,
    target_role  VARCHAR(255),
    scheduled_at TIMESTAMP    NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_by   VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL
);

-- -----------------------------------------------------------------------------
-- system_config
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_config (
    id           VARCHAR(26)  NOT NULL PRIMARY KEY,
    config_key   VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    description  VARCHAR(255),
    updated_at   TIMESTAMP
);

COMMIT;

-- =============================================================================
-- To verify after running:
--   \dt banned_ips
--   \dt scheduled_emails
--   \dt system_config
-- =============================================================================
