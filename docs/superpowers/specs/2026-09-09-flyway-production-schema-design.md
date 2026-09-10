# Flyway Production Schema Design

## Objective

Make versioned Flyway migrations the only production and staging schema mutation mechanism.
Flyway runs during Spring startup, completes before JPA initializes, and Hibernate then validates
the migrated schema. A migration or validation failure prevents the application from starting.

## Migration history

The supported history starts from the schema immediately before the existing V2 migration:

- V1 creates every relational table, foreign key, collection table, uniqueness rule, and index
  required by the supported baseline, except objects deliberately introduced later.
- V2 adds `projects.version` for optimistic locking.
- V3 adds `project_applications.review_message`.
- V4 creates `pending_index` and its retry indexes.

This preserves the checked-in migration order instead of folding later changes into V1. New
features continue at V5 and above.

## Runtime workflow

Production and staging set Flyway enabled, checksum validation enabled, clean disabled, and
baseline-on-migrate disabled. SQL sample-data initialization is disabled. Hibernate uses
`ddl-auto=validate` only. The Compose app service exposes the same settings explicitly so no
environment override can restore `update`.

The normal operational migration command recreates and waits for the application service. This
uses the same Spring startup sequence as deployment rather than a second migration configuration.
CI starts an empty PostgreSQL container, runs V1 through V4, and proves that a full application
context starts with Hibernate validation.

## Existing databases

Automatic baselining is intentionally prohibited. Before enabling this release against an existing
database, operators must:

1. take and verify a recoverable backup;
2. restore or clone the schema in an isolated environment;
3. compare all tables, columns, types, constraints, and indexes with the documented V4 schema;
4. repair drift explicitly;
5. use Flyway's explicit `baseline` command at version 4 on the verified copy;
6. run Flyway validation and start the application with Hibernate validation;
7. repeat the explicit baseline on production only after the copy passes.

A test exercises this explicit V4-baseline procedure on a schema copy. A non-empty schema without
a history table still fails startup, preventing accidental assumptions about unknown live state.

## Safety properties

- No production or staging profile uses Hibernate create/update.
- Flyway clean is disabled.
- Sample `data.sql` loading is disabled outside development/test workflows.
- Missing, reordered, or checksum-mismatched migrations fail validation.
- No migration deletes or recreates user data.
- Existing databases are never auto-baselined.
