# Auth Service

## JWT RSA keys (RS256)

**Best practice:** pass key content via `.env` (env vars), not file paths, so secrets stay out of the filesystem and work well with secret managers.

Keys can be provided in three ways (in order of precedence):

1. **Environment variables** (recommended): set in `.env` or your environment:
   - `AUTH_JWT_PRIVATE_KEY`: full PEM or **Base64-encoded** PEM (e.g. `LS0tLS1CRUdJTiBQUklWQVRF...`)
   - `AUTH_JWT_PUBLIC_KEY`: full PEM or **Base64-encoded** PEM  
   Base64 is useful in `.env` to avoid multiline; the service decodes automatically. Raw PEM is also accepted.

2. **File paths** in `application.properties` (fallback):
   - `auth.jwt.private-key-path=keys/private.pem`
   - `auth.jwt.public-key-path=keys/public.pem`
   - Paths are relative to the process working directory or absolute.

3. **Auto-generation**: If neither env nor file keys are set, the service generates a 2048-bit RSA key pair at startup and writes it to the directory configured by `auth.jwt.key-dir` (default: `keys/`). **Do not commit** the `keys/` folder (it is in `.gitignore`).

To generate keys manually (e.g. for production):

```bash
./scripts/generate-jwt-keys.sh [key-dir]
# default key-dir is "keys"
```

Then set in config:

```properties
auth.jwt.private-key-path=keys/private.pem
auth.jwt.public-key-path=keys/public.pem
```

## User registration

**POST** `/auth/tenants/{tenantId}/users`

Register a new user for a tenant (e.g. created by office/tenant admin).

Request body:

```json
{
  "email": "user@example.com",
  "password": "secret123",
  "fullName": "Jane Doe",
  "role": "CLIENT"
}
```

- `email`: required, unique across all users.
- `password`: required, 8–100 characters; must include at least 1 uppercase, 1 lowercase, 1 number and 1 special character (stored hashed with BCrypt).
- `fullName`: required.
- `role`: required (e.g. `CLIENT`, `ACCOUNTANT`, `ADMIN`).

Responses:

- **201 Created**: returns `{ "id", "tenantId", "email", "fullName", "role", "active", "firstAccess" }`.
- **404 Not Found**: tenant does not exist.
- **409 Conflict**: a user with that email already exists.

The endpoint is currently unauthenticated; it can be restricted later (e.g. to office/JWT holders).

---

## How to test

### 1. Prerequisites

- PostgreSQL running with database `tms_auth` (e.g. from project `docker-compose` or `init-dbs.sql`).
- `.env` in project root with at least:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tms_auth`
  - `AUTH_JWT_PRIVATE_KEY` and `AUTH_JWT_PUBLIC_KEY` (Base64 or raw PEM), or leave them unset to auto-generate keys.

### 2. Start the auth-service

From project root with env loaded:

```bash
./scripts/run-with-env.sh auth-service spring-boot:run
```

Or from `auth-service` with env already in your shell:

```bash
cd auth-service && ./mvnw spring-boot:run
```

Default port is **8080**. Check logs for `Loading JWT keys from environment` or `generating and storing in keys/`.

### 3. Create a tenant (required for registration)

Registration is per-tenant. Insert one tenant (once) via SQL:

```bash
psql -h localhost -U tms -d tms_auth -c "
INSERT INTO tenants (id, name, email, active, created_at, updated_at)
VALUES (gen_random_uuid(), 'Acme Corp', 'acme@example.com', true, NOW(), NOW())
RETURNING id, name, email;
"
```

Copy the returned `id` (UUID) for the next step.

### 4. Test user registration

**You must replace the tenant UUID in the URL** with the real `id` from step 3 (e.g. `a1b2c3d4-e5f6-7890-abcd-ef1234567890`). Do not use the literal text `TENANT_UUID`.

**Success (201):**

```bash
# Replace YOUR_TENANT_ID with the UUID from step 3, e.g. a1b2c3d4-e5f6-7890-abcd-ef1234567890
curl -s -X POST http://localhost:8080/auth/tenants/YOUR_TENANT_ID/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "Secret1!",
    "fullName": "Jane Doe",
    "role": "CLIENT"
  }'
```

One-liner using the first tenant in the DB (run after step 3):

```bash
TENANT_ID=$(psql -h localhost -U tms -d tms_auth -t -A -c "SELECT id FROM tenants LIMIT 1")
curl -s -X POST "http://localhost:8080/auth/tenants/${TENANT_ID}/users" \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"Secret1!","fullName":"Jane Doe","role":"CLIENT"}'
```

Expected: JSON with `id`, `tenantId`, `email`, `fullName`, `role`, `active`, `firstAccess`.

**Validation error (400)** – e.g. weak password (use a real tenant UUID):

```bash
curl -s -X POST http://localhost:8080/auth/tenants/YOUR_TENANT_ID/users \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"short","fullName":"Jane","role":"CLIENT"}'
```

**Tenant not found (404)** – use a non-existent UUID:

```bash
curl -s -X POST http://localhost:8080/auth/tenants/00000000-0000-0000-0000-000000000000/users \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"Secret1!","fullName":"Jane","role":"CLIENT"}'
```

**Duplicate email (409)** – run the success request twice with the same email.

### 5. Optional: automated test

From project root:

```bash
cd auth-service && ./mvnw test
```
