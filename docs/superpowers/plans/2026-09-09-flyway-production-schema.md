# Flyway Production Schema Implementation Plan

1. Add Flyway core and PostgreSQL support through Spring Boot dependency management.
2. Add a V1 pre-V2 PostgreSQL schema and keep V2-V4 as ordered forward migrations.
3. Add a PostgreSQL Testcontainers test that migrates an empty database and starts the application
   with `ddl-auto=validate`.
4. Add an explicit-existing-schema baseline test and a checksum validation failure test.
5. Configure production and staging for Flyway startup, Hibernate validation, disabled clean,
   disabled auto-baseline, and disabled sample SQL initialization.
6. Remove the Compose `ddl-auto=update` override and declare the same safe settings explicitly.
7. Replace the nonfunctional Makefile migration command with the authoritative application-startup
   migration and health wait.
8. Update deployment/database documentation with clean install, existing-schema baseline,
   verification, rollback, and failure behavior.
9. Add an explicit migration-and-validation step to pull-request, development, and production CI.
10. Run formatting, focused migration tests, configuration contract tests, and the relevant build
    verification before opening the issue-closing PR.
