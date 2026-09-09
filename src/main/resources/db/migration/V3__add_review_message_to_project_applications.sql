-- Migration: Persist owner review messages for project applications
-- Required for environments with spring.jpa.hibernate.ddl-auto=validate (staging, production)
ALTER TABLE project_applications ADD COLUMN IF NOT EXISTS review_message TEXT;
