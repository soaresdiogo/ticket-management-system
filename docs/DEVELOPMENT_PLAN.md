# TMS Development Plan

A phased todo list for building the Ticket Management System. Database naming: use `tms_auth`, `tms_tickets`, `tms_files`, `tms_notifications` everywhere (Docker, env, and each service’s datasource URL).

---

## Phase 0 — Infrastructure and parent POM (1–2 days)

- [X] Create `init-dbs.sql` with schemas per service (databases + full DDL, one file under `docker/`)
- [X] Introduce Parent POM with centralised versions (dependencyManagement + pluginManagement in root `pom.xml`; modules use root as parent)

---

## Phase 1 — Auth Service (4–6 days)

Auth is first because other services and the gateway depend on it for JWT validation.

- [X] Configure Flyway and table structure (or rely on init-dbs.sql and use Flyway for future migrations only)
- [X] Implement entities and repositories (JPA + Lombok): `Tenant`, `User`, `MfaCode`, `RefreshToken`, `AuthAuditLog`
- [X] Generate and store RSA key pair (public/private) for JWT signing (file or env)
- [X] User registration (created by office/tenant)
- [X] **POST /auth/login** — Validate credentials, generate MFA code, store in Redis with TTL, send via Resend
- [X] **POST /auth/verify-mfa** — Validate code from Redis, issue RS256 JWT and optional refresh token
- [X] **POST /auth/refresh** — Refresh token flow
- [ ] **POST /auth/change-password** — Change password (and set `first_access = false` if applicable)
- [ ] **GET /auth/public-key** — Expose public key for gateway and other services to validate JWT
- [ ] Test with Postman/Insomnia (login → MFA → JWT → refresh → change-password)

---

## Phase 2 — API Gateway (1–2 days)

Done after Auth so the JWT filter can be tested with real tokens.

- [ ] Configure routes for each service in `application.yml`
- [ ] JWT validation filter using auth-service public key
- [ ] Propagate headers (e.g. `X-User-Id`, `X-User-Role`, `X-Tenant-Id`) to downstream services
- [ ] Configure CORS for the Angular app
- [ ] Test authenticated vs unauthenticated routes

---

## Phase 3 — Ticket Service (4–5 days)

- [ ] Flyway + tables (or align with init-dbs.sql); entities and repositories: `Ticket`, `TicketStatusHistory`, `TicketComment`
- [ ] **POST /tickets** — Create ticket
- [ ] **GET /tickets** — List tickets for current user (client; filter by JWT `userId`)
- [ ] **GET /tickets/all** — List all tickets (role ACCOUNTANT)
- [ ] **PATCH /tickets/{id}/status** — Change status; on change, publish event to Kafka topic `ticket.status.changed` (payload: ticketId, userId, oldStatus, newStatus, timestamp)
- [ ] **POST /tickets/{id}/comments** — Add comment
- [ ] Configure Kafka producer and event payload

---

## Phase 4 — File Service (3–4 days)

- [ ] Flyway + `attachments`, `download_log` (or align with init-dbs.sql); configure MinIO client
- [ ] **POST /files/upload** — Multipart upload to MinIO, save metadata in Postgres, publish Kafka event `ticket.document.uploaded`
- [ ] **GET /files/{id}/download** — Return presigned MinIO URL (e.g. 15 min TTL)
- [ ] **GET /files/ticket/{ticketId}** — List files for a ticket
- [ ] Validations: max size, allowed types (e.g. pdf, jpg, png, xlsx)

---

## Phase 5 — Notification Service (3–4 days)

- [ ] Kafka consumer for `ticket.status.changed` and `ticket.document.uploaded`
- [ ] On event: send email via Resend (e.g. “Your ticket #123 has been updated to Processing”)
- [ ] WebSocket endpoint (e.g. `/ws`): on Kafka event, push to Angular client
- [ ] Persist notifications in DB (`notifications` table); optional `email_log` for sent emails
- [ ] **PATCH /notifications/{id}/read** — Mark notification as read

---

## Phase 6 — Integration and E2E (2–3 days)

- [ ] Run full stack with `docker-compose` (Postgres, Redis, Kafka, MinIO, Kafka UI, and all services)
- [ ] End-to-end test via Postman: login → MFA → JWT → create ticket → upload file → change status → confirm email received
- [ ] Verify events in Kafka UI (e.g. localhost:8090)
- [ ] Fix cross-service communication and configuration
- [ ] Security check: client cannot see tickets of another tenant

---

## Phase 7 — Angular frontend (after backend is stable)

- [ ] Authentication (login + MFA)
- [ ] Client dashboard: list of tickets and status
- [ ] Create ticket and upload files
- [ ] Office dashboard: full list and filters
- [ ] Change ticket status
- [ ] WebSocket for real-time updates
- [ ] File download

---

## Summary

| Phase | Focus |
|-------|--------|
| 0 | Docker + init-dbs.sql + Parent POM |
| 1 | Auth Service (JWT, MFA, Resend, Redis) |
| 2 | API Gateway (routes, JWT filter, headers) |
| 3 | Ticket Service (CRUD, status, Kafka) |
| 4 | File Service (MinIO, Kafka event) |
| 5 | Notification Service (Kafka consumer, Resend, WebSocket, DB) |
| 6 | Integration and E2E |
| 7 | Angular frontend |

No FKs are added between services (e.g. `tickets.tenant_id` and `tickets.client_id` remain logical references only), preserving service isolation.
