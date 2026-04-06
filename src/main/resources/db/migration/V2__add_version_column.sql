-- Migration: Add version column for optimistic locking support
-- Required for environments with spring.jpa.hibernate.ddl-auto=validate (staging, production)
ALTER TABLE projects ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
