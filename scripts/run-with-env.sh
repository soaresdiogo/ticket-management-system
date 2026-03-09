#!/usr/bin/env bash
# Run a TMS service with env vars from .env (if present).
# Usage: ./scripts/run-with-env.sh <service-dir> [mvn-args...]
# Example: ./scripts/run-with-env.sh auth-service spring-boot:run

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [[ -z "$1" ]]; then
  echo "Usage: $0 <service-dir> [mvn-args...]"
  echo "  service-dir: api-gateway | auth-service | ticket-service | notification-service | file-service"
  echo "  Example: $0 auth-service spring-boot:run"
  exit 1
fi

SERVICE_DIR="$ROOT_DIR/$1"
if [[ ! -d "$SERVICE_DIR" ]]; then
  echo "Error: not a directory: $SERVICE_DIR"
  exit 1
fi

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "$ENV_FILE"
  set +a
  echo "Loaded env from $ENV_FILE"
else
  echo "No .env found at $ENV_FILE (copy .env.example to .env and configure)"
fi

shift || true
cd "$SERVICE_DIR"
exec ./mvnw "$@"
