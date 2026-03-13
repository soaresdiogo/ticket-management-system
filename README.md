# Ticket Management System (TMS)

A microservices-based ticket management system built with Spring Boot 4 and Java 25.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Angular Frontend                  │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/WebSocket
┌──────────────────────▼──────────────────────────────┐
│                   API Gateway                        │
│            (Spring Cloud Gateway)                    │
│         Routing + Rate limiting + Auth            │
└────┬──────────────┬──────────────┬───────────────────┘
     │              │              │
┌────▼────┐  ┌──────▼─────┐  ┌────▼──────────┐
│  Auth   │  │  Tickets   │  │   Files       │
│ Service │  │  Service   │  │   Service     │
│         │  │            │  │               │
│ JWT/MFA │  │ CRUD       │  │ MinIO upload/ │
│ Resend  │  │ Status     │  │ download      │
│ Redis   │  │ Postgres   │  │ Postgres      │
└─────────┘  └──────┬─────┘  └───────────────┘
                    │ Event publishing
             ┌──────▼──────┐
             │    Kafka    │
             └──────┬──────┘
                    │ Consumer
             ┌──────▼──────────────┐
             │ Notification Service│
             │ Resend (email)      │
             │ WebSocket push      │
             └─────────────────────┘
```

The system is composed of five Spring Boot services:

| Service | Description | Key technologies |
|---------|-------------|------------------|
| **api-gateway** | Single entry point, routing and OAuth2 validation | Spring Cloud Gateway (WebFlux), OAuth2 Resource Server |
| **auth-service** | Authentication, user management, JWT issuance | Spring Security, JPA, Flyway, PostgreSQL, Spring AI (Redis vector store), Mail |
| **ticket-service** | Ticket lifecycle and business logic | Spring Data JPA, Flyway, PostgreSQL, Kafka, OAuth2 |
| **notification-service** | Notifications and real-time updates | Spring Data JPA, Kafka, WebSocket, PostgreSQL |
| **file-service** | File upload and storage | Spring Data JPA, Flyway, PostgreSQL, OAuth2 |

## Tech stack

- **Java** 25  
- **Spring Boot** 4.0.3  
- **Spring Cloud** 2025.1.0 (Gateway)  
- **Spring AI** 2.0.0-M2 (auth-service, Redis vector store)  
- **PostgreSQL** (persistence)  
- **Apache Kafka** (event-driven communication between ticket and notification services)  
- **MinIO** (S3-compatible object storage for file-service)  
- **Redis** (vector store for auth-service)  
- **OAuth2** (JWT-based security across gateway and services)

## Prerequisites

- JDK 25  
- Maven 3.x  
- Docker & Docker Compose (for Postgres, Redis, Kafka, MinIO)

## First-time setup (new developers)

To guarantee the project builds and runs with one flow:

```bash
# 1. Copy env template and keep defaults (matches docker-compose)
cp .env.example .env

# 2. Start infrastructure (Postgres, Redis, Kafka, MinIO, Kafka UI)
make docker-up
# or: docker compose up -d

# 3. (Optional) Create DBs and schema if they don't exist
#    Postgres runs docker/init-dbs.sql automatically on first start (empty volume).
#    If you reuse an existing volume or need to re-run the init script:
make init-dbs
# or: docker exec -i tms-postgres psql -U tms -d tms < docker/init-dbs.sql

# 4. Build all services in cascade (root Maven reactor)
make install
# or: mvn -f pom.xml clean install

# 5. (Optional) Install git hook so tests run before every commit (like Husky in Node)
make install-hooks
```

Optional: run a single service with env loaded from `.env`:

```bash
./scripts/run-with-env.sh auth-service spring-boot:run
# or: make run-auth
```

## Environment variables (Java vs Node .env)

Unlike Node.js, **Spring Boot does not load a `.env` file by default**. It does two things that work well with Docker and scripts:

1. **Environment variables override `application.properties`**  
   Any env var is mapped to a property using *relaxed binding*:  
   `SPRING_DATASOURCE_URL` → `spring.datasource.url`,  
   `SPRING_DATA_REDIS_HOST` → `spring.data.redis.host`, etc.

2. **Use a `.env` file by exporting it before running**  
   Copy `.env.example` to `.env`, then either:
   - **Source then run:** `set -a && source .env && set +a && ./mvnw spring-boot:run`
   - **Use the helper script:** `./scripts/run-with-env.sh auth-service spring-boot:run`
   - **Or use Make:** `make run-auth` (script sources `.env` automatically)

Credentials in `.env.example` match `docker-compose.yml` (user `tms`, password `tms`, DBs `tms_auth`, `tms_tickets`, `tms_files`, `tms_notifications`). Adjust `.env` for local overrides; never commit `.env` (it is gitignored).

## Running the services

**Option A — Run all backend services at once (recommended for local dev):**

```bash
make run-all
```

This starts the API Gateway, Auth, Ticket, Notification, and File services in the background (ports 8080–8084). Ensure `make docker-up` and `make init-dbs` have been run first. If you see **500** or **Connection refused** on `GET /ws/info`, the notification service (port 8084) is not running—start it with `make run-notification` or wait for all services from `make run-all` to finish starting.

**To stop all services:** Use `make stop-all`. Do not rely only on Ctrl+C—it often leaves Java processes running, which will cause "port already in use" when you run `make run-all` again.

```bash
make stop-all
```

**Option B — Run services individually (separate terminals; loads `.env`):**

```bash
make run-gateway
make run-auth
make run-ticket
make run-notification
make run-file
```

**Option C — Maven directly (set env or use `application.properties`):**

```bash
cd api-gateway && ./mvnw spring-boot:run
cd auth-service && ./mvnw spring-boot:run
# ... etc
```

**Option D — With env from `.env` (single service):**

```bash
./scripts/run-with-env.sh auth-service spring-boot:run
```

Configure each service via `src/main/resources/application.properties` or environment variables (and `.env` when using the script) for database, Kafka, Redis, MinIO, and OAuth2.

**Infrastructure URLs (after `docker compose up -d`):**

| Service     | URL / Endpoint              | Notes                    |
|------------|-----------------------------|--------------------------|
| PostgreSQL | `localhost:5432`            | Default DB: `tms` (empty). App tables live in: `tms_auth`, `tms_tickets`, `tms_files`, `tms_notifications` |
| Redis      | `localhost:6379`           |                          |
| Kafka      | `localhost:9092`            | Bootstrap for apps       |
| Kafka UI   | http://localhost:8090       | Web UI for topics/msgs   |
| MinIO API  | http://localhost:9000       | S3-compatible API        |
| MinIO Console | http://localhost:9001    | Web UI (user: `tms`, pass: `tms12345`) |

## Git hooks (run tests before commit)

To run `make test` automatically before each commit (similar to Husky in Node projects), install the pre-commit hook once:

```bash
make install-hooks
```

This copies `scripts/git-hooks/pre-commit` into `.git/hooks/pre-commit`. After that, every `git commit` will run the full test suite first; the commit is aborted if tests fail. No Python or Node required.

Alternatively, if you use the [pre-commit](https://pre-commit.com/) framework, run `pre-commit install` and it will use `.pre-commit-config.yaml` (which also runs `make test`).

## Build and update all projects (cascade)

From the repo root:

```bash
# Build everything (reactor order: api-gateway → auth → ticket → notification → file)
mvn -f pom.xml clean install

# Or use Make
make install
make test
make clean
```

## Project structure

```
ticket-management-system/
├── pom.xml                # Root Maven reactor (build all in cascade)
├── .env.example           # Env template (copy to .env; matches docker-compose)
├── Makefile               # build, docker-up, run-*, run-all, stop-all, install-hooks
├── scripts/
│   ├── run-with-env.sh   # Run a service with .env loaded
│   └── git-hooks/        # pre-commit hook (installed via make install-hooks)
├── api-gateway/           # Spring Cloud Gateway
├── auth-service/          # Authentication & user management
├── ticket-service/        # Ticket domain
├── notification-service/  # Notifications & WebSocket
├── file-service/          # File storage
├── docker-compose.yml     # Postgres, Redis, Kafka, MinIO, Kafka UI
├── docker/
│   └── init-dbs.sql       # DBs: tms_auth, tms_tickets, tms_files, tms_notifications
├── docs/
│   └── DEVELOPMENT_PLAN.md # Phased development todo list
├── LICENSE
└── README.md
```

## License

Copyright (c) 2026 Di2iT, Unipessoal Lda.  
This project is licensed under the [Business Source License 1.1](LICENSE).  
Change Date: 2029-01-01 → Change License: Apache 2.0.
