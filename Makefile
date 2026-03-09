# TMS — One-command build, test, and run (for new developers)
# Requires: JDK 25, Maven 3.x, Docker & docker-compose

.PHONY: help build clean install test docker-up docker-down init-dbs run-gateway run-auth run-ticket run-notification run-file env-check

help:
	@echo "TMS — Ticket Management System"
	@echo ""
	@echo "  make install      - Build all services in cascade (Maven reactor)"
	@echo "  make test        - Run tests for all services"
	@echo "  make clean       - Clean all service targets"
	@echo "  make docker-up   - Start Postgres, Redis, Kafka (docker-compose)"
	@echo "  make docker-down - Stop docker-compose"
	@echo "  make init-dbs    - Run docker/init-dbs.sql (create DBs + schema; use if not first start)"
	@echo "  make run-gateway - Run API Gateway (sources .env if present)"
	@echo "  make run-auth    - Run Auth Service"
	@echo "  make run-ticket  - Run Ticket Service"
	@echo "  make run-notification - Run Notification Service"
	@echo "  make run-file    - Run File Service"
	@echo "  make env-check   - Check .env exists (copy .env.example to .env)"
	@echo ""
	@echo "First time: cp .env.example .env && make docker-up && make init-dbs && make install"

# Use Maven wrapper so Maven does not need to be installed (./mvnw downloads it on first run)
MVN := $(shell command -v mvn 2>/dev/null || echo ./mvnw)

# Build all projects in cascade (order defined in root pom.xml)
build install:
	$(MVN) -f pom.xml clean install -DskipTests

test:
	$(MVN) -f pom.xml test

clean:
	$(MVN) -f pom.xml clean

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
