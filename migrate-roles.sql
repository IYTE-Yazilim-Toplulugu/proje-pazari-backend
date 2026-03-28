-- =============================================================================
-- Role System Migration: APPLICANT/PROJECT_OWNER → USER/ADMIN
-- =============================================================================
--
-- WHEN TO RUN:
--   Production deployments only. Not needed in development — Hibernate
--   (ddl-auto=update) creates the user_roles table on app startup, and
--   new registrations default to USER automatically.
--
-- ORDER OF OPERATIONS (production):
--   1. Deploy the new application build (Hibernate creates the user_roles table)
--   2. Run this script while the app is live — it is safe to run under load
--   3. Admins are locked out of /api/v1/admin/* until this script completes
--      (window is only as long as the script takes to run)
--
-- SAFETY:
--   - INSERT only — no updates, no deletes, no schema changes
--   - Idempotent — safe to run multiple times, duplicates are skipped
--   - The legacy users.role column is left untouched (Hibernate never drops
--     columns on ddl-auto=update); it can be dropped manually later
--
-- ROLLBACK:
--   If something goes wrong, simply truncate user_roles and redeploy the
--   previous build. The old users.role column still has the original data.
--
-- USAGE:
--   psql -U yazilim -d proje_pazari_db -h localhost -f migrate-roles.sql
-- =============================================================================

BEGIN;

-- Migrate existing users into the user_roles join table.
-- ADMIN stays ADMIN; APPLICANT and PROJECT_OWNER both become USER.
INSERT INTO user_roles (user_id, role)
SELECT
    id,
    CASE WHEN role = 'ADMIN' THEN 'ADMIN' ELSE 'USER' END
FROM users
WHERE id NOT IN (SELECT DISTINCT user_id FROM user_roles);

COMMIT;

-- =============================================================================
-- To verify after running:
--   SELECT r.role, COUNT(*) FROM user_roles r GROUP BY r.role;
--   SELECT COUNT(*) FROM users WHERE id NOT IN (SELECT user_id FROM user_roles);
-- The second query should return 0.
-- =============================================================================
