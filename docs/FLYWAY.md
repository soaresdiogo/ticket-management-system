# Flyway strategy

## Overview

Initial database schema is created by **`docker/init-dbs.sql`** when Postgres starts (one database per service: `tms_auth`, `tms_tickets`, `tms_files`, `tms_notifications`). Flyway is configured to **baseline** on that existing schema and only run **future** migrations.

## Configuration

Each service that uses a database has:

- **`spring.flyway.baseline-on-migrate=true`** — If the schema already exists (e.g. from `init-dbs.sql`) and there is no Flyway history table, Flyway creates `flyway_schema_history` and records a baseline instead of failing.
- **`spring.flyway.baseline-version=1`** — Baseline is recorded as version `1`. Any migration with version **greater than 1** will be applied on subsequent runs.

Migrations live under **`src/main/resources/db/migration/`** per service. Default naming: `V{n}__description.sql` (e.g. `V2__add_user_preferences.sql`).

## Table structure (initial)

Initial DDL is in **`docker/init-dbs.sql`**:

| Database         | Tables |
|------------------|--------|
| `tms_auth`       | `tenants`, `users`, `mfa_codes`, `refresh_tokens`, `auth_audit_log` |
| `tms_tickets`    | `tickets`, `ticket_status_history`, `ticket_comments` |
| `tms_files`      | `attachments`, `download_log` |
| `tms_notifications` | `notifications`, `email_log` |

Do not duplicate this DDL in Flyway. Use Flyway only for **changes** after the baseline.

## Adding a new migration

1. Add a new file in the **relevant service’s** `db/migration/` folder.
2. Use the next version number and a short description:  
   `V2__add_column_xyz.sql`, `V3__create_index_on_tickets_status.sql`, etc.
3. Write **plain SQL** (DDL/DML). Keep each migration small and idempotent where possible (e.g. `IF NOT EXISTS` for PostgreSQL where supported).
4. Run the service (or `mvn flyway:migrate` with the correct datasource); Flyway will apply only migrations with version > 1.

## Checksum mismatch (repair)

If a migration file was **edited after it was already applied**, Flyway will fail with e.g. `Migration checksum mismatch for migration version 2`. Each service has its **own database** and its **own** `V2__*.sql` (e.g. `V2__create_ticket_tables.sql`, `V2__create_notification_tables.sql`); different content and checksums between services are expected. Fix the schema history for the failing service so it matches that service’s current file:

- **ticket-service:** From repo root run `make flyway-repair-ticket` (or `mvn -pl ticket-service flyway:repair`). Requires Postgres with `tms_tickets` DB.
- **notification-service:** From repo root run `make flyway-repair-notification` (or `mvn -pl notification-service flyway:repair`). Requires Postgres with `tms_notifications` DB.
- **file-service:** From repo root run `make flyway-repair-file` (or `mvn -pl file-service flyway:repair`). Requires Postgres with `tms_files` DB.

Ensure Postgres is up with the corresponding DB (e.g. `make docker-up` and `make init-dbs`).

## Services using Flyway

- **auth-service**
- **ticket-service**
- **file-service**
- **notification-service**

All use the same baseline strategy; each has its own `db/migration/` directory and connects to its own database.
