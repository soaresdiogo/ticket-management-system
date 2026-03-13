# TMS — One-command build, test, and run (for new developers)
# Requires: JDK 25, Maven 3.x, Docker & docker-compose

.PHONY: help build clean install test lint ci docker-up docker-down init-dbs run-gateway run-auth run-ticket run-notification run-file run-all stop-all env-check install-hooks flyway-repair-notification flyway-repair-file flyway-repair-ticket

help:
	@echo "TMS — Ticket Management System"
	@echo ""
	@echo "  make install      - Build all services in cascade (Maven reactor)"
	@echo "  make test        - Run tests for all services"
	@echo "  make ci          - Same as GitHub Actions: verify + tests + linters (simulate pipeline)"
	@echo "  make lint        - Run Checkstyle, PMD, and SpotBugs (no tests)"
	@echo "  make clean       - Clean all service targets"
	@echo "  make install-hooks - Install git hooks: pre-commit (make lint), pre-push (mvn verify)"
	@echo "  make docker-up   - Start Postgres, Redis, Kafka (docker-compose)"
	@echo "  make docker-down - Stop docker-compose"
	@echo "  make init-dbs    - Run docker/init-dbs.sql (create DBs + schema; use if not first start)"
	@echo "  make run-gateway - Run API Gateway (sources .env if present)"
	@echo "  make run-auth    - Run Auth Service"
	@echo "  make run-ticket  - Run Ticket Service"
	@echo "  make run-notification - Run Notification Service"
	@echo "  make run-file    - Run File Service"
	@echo "  make run-all     - Run all backend services in background (gateway, auth, ticket, notification, file)"
	@echo "  make stop-all    - Stop all backend services (kills processes on ports 8080-8084)"
	@echo "  make flyway-repair-notification - Fix Flyway checksum mismatch for notification-service (run if migration V2 fails validation)"
	@echo "  make flyway-repair-file        - Fix Flyway checksum mismatch for file-service (run if migration V2 fails validation)"
	@echo "  make flyway-repair-ticket      - Fix Flyway checksum mismatch for ticket-service (run if migration V2 fails validation)"
	@echo "  make env-check   - Check .env exists (copy .env.example to .env)"
	@echo ""
	@echo "First time: cp .env.example .env && make docker-up && make init-dbs && make install && make install-hooks"

# Use Maven wrapper so Maven does not need to be installed (./mvnw downloads it on first run)
MVN := $(shell command -v mvn 2>/dev/null || echo ./mvnw)

# Build all projects in cascade (order defined in root pom.xml)
build install:
	$(MVN) -f pom.xml clean install -DskipTests

test:
	$(MVN) -f pom.xml test

# Same command as GitHub Actions CI: verify (build + test + Checkstyle + PMD + SpotBugs).
# Run this before pushing to catch pipeline failures. Requires Docker for Testcontainers.
ci:
	$(MVN) -f pom.xml verify --no-transfer-progress -DskipTests=false

# Checkstyle + PMD + SpotBugs only (fast, for pre-commit). Full verify runs these + tests.
lint:
	$(MVN) -f pom.xml checkstyle:check pmd:check spotbugs:check

clean:
	$(MVN) -f pom.xml clean

# Install git hooks: pre-commit (lint) and pre-push (full verify before push).
install-hooks:
	@mkdir -p .git/hooks
	@cp scripts/git-hooks/pre-commit .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@cp scripts/git-hooks/pre-push .git/hooks/pre-push
	@chmod +x .git/hooks/pre-push
	@echo "Hooks installed: pre-commit → make lint; pre-push → ./mvnw verify"

docker-up:
	docker compose up -d
	@echo "Waiting for Postgres..."
	@until docker exec tms-postgres pg_isready -U tms -d tms 2>/dev/null; do sleep 2; done
	@echo "Infrastructure ready."

docker-down:
	docker compose down

# Run init-dbs.sql against Postgres (creates tms_auth, tms_tickets, tms_files, tms_notifications + tables).
# Postgres runs this automatically on first start; use this if reusing a volume or re-initing.
init-dbs:
	@docker exec -i tms-postgres psql -U tms -d tms < docker/init-dbs.sql
	@echo "Databases and schema updated."

env-check:
	@if [ ! -f .env ]; then echo "Create .env from .env.example: cp .env.example .env"; exit 1; fi
	@echo ".env found."

run-gateway:
	./scripts/run-with-env.sh api-gateway spring-boot:run

run-auth:
	./scripts/run-with-env.sh auth-service spring-boot:run

run-ticket:
	./scripts/run-with-env.sh ticket-service spring-boot:run

run-notification:
	./scripts/run-with-env.sh notification-service spring-boot:run

run-file:
	./scripts/run-with-env.sh file-service spring-boot:run

# Run all backend services in background. Ensure docker-up and init-dbs are done first.
# Use 'make stop-all' to stop them (Ctrl+C often leaves Java processes running).
# Runs Flyway repair for notification, file, and ticket services first so checksum mismatches don't block startup.
run-all: env-check flyway-repair-notification flyway-repair-file flyway-repair-ticket
	@echo "Starting all services in background..."
	@./scripts/run-with-env.sh api-gateway spring-boot:run & \
	./scripts/run-with-env.sh auth-service spring-boot:run & \
	./scripts/run-with-env.sh ticket-service spring-boot:run & \
	./scripts/run-with-env.sh notification-service spring-boot:run & \
	./scripts/run-with-env.sh file-service spring-boot:run & \
	wait

# Fix Flyway schema history when migration file was changed after it was applied (checksum mismatch).
# Uses same DB connection as app (sources .env). Requires Postgres with tms_notifications (make docker-up and make init-dbs).
flyway-repair-notification:
	@if [ -f .env ]; then set -a && . ./.env && set +a; fi; \
	REPAIR_URL="jdbc:postgresql://$${DB_HOST:-localhost}:$${DB_PORT:-5432}/$${DB_NAME:-tms_notifications}"; \
	$(MVN) -f pom.xml -pl notification-service flyway:repair -q \
		-Dflyway.url="$$REPAIR_URL" \
		-Dflyway.user="$${DB_USER:-tms}" \
		-Dflyway.password="$${DB_PASSWORD:-tms}"
	@echo "Flyway repair done. You can run make run-all again."

# Fix Flyway schema history for file-service (checksum mismatch for migration V2). Uses same DB as app (.env).
flyway-repair-file:
	@if [ -f .env ]; then set -a && . ./.env && set +a; fi; \
	REPAIR_URL="jdbc:postgresql://$${DB_HOST:-localhost}:$${DB_PORT:-5432}/$${DB_NAME:-tms_files}"; \
	$(MVN) -f pom.xml -pl file-service flyway:repair -q \
		-Dflyway.url="$$REPAIR_URL" \
		-Dflyway.user="$${DB_USER:-tms}" \
		-Dflyway.password="$${DB_PASSWORD:-tms}"
	@echo "Flyway repair done. You can run make run-all again."

# Fix Flyway schema history for ticket-service (checksum mismatch for migration V2). Uses same DB as app (.env).
flyway-repair-ticket:
	@if [ -f .env ]; then set -a && . ./.env && set +a; fi; \
	REPAIR_URL="jdbc:postgresql://$${DB_HOST:-localhost}:$${DB_PORT:-5432}/$${DB_NAME:-tms_tickets}"; \
	$(MVN) -f pom.xml -pl ticket-service flyway:repair -q \
		-Dflyway.url="$$REPAIR_URL" \
		-Dflyway.user="$${DB_USER:-tms}" \
		-Dflyway.password="$${DB_PASSWORD:-tms}"
	@echo "Flyway repair done. You can run make run-all again."

# Stop all TMS backend services by killing processes on ports 8080 (gateway), 8081 (auth), 8082 (ticket), 8083 (file), 8084 (notification).
# Use this after run-all when Ctrl+C did not stop everything.
stop-all:
	@for port in 8080 8081 8082 8083 8084; do \
		pid=$$(lsof -ti :$$port 2>/dev/null); \
		if [ -n "$$pid" ]; then \
			echo "Stopping process on port $$port (PID $$pid)..."; \
			kill -9 $$pid 2>/dev/null || true; \
		fi; \
	done
	@echo "Done. Ports 8080-8084 should be free."
