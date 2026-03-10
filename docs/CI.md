# CI / Code quality

The project uses **GitHub Actions** for automated builds, tests, and **local static analysis**. All checks run **fully local**—no server, port, or token required.

## What runs automatically

- **On every push and pull request** to `main` or `master`:
  1. **Build** – `./mvnw clean verify`
  2. **Tests** – all unit/integration tests (via `verify`)
  3. **Lint** – **Checkstyle** (style/conventions), **PMD** (code smells, design), **SpotBugs** (bugs: nulls, leaks, concurrency). All bound to `verify`; build fails on violations.

## Local checks

Before pushing, run the same checks locally:

```bash
# Full build + test + all linters (same as CI)
./mvnw clean verify

# Linters only (fast, for pre-commit)
./mvnw checkstyle:check pmd:check spotbugs:check
# or
make lint
```

## Simulate the pipeline before pushing

You can run the **exact same steps** as GitHub Actions locally so you catch failures before pushing.

### Option 1: Same Maven command as CI (recommended)

CI runs `verify` with tests enabled. Use the same command (Docker must be running for Testcontainers in `ticket-service` and `auth-service`):

```bash
./mvnw verify --no-transfer-progress -DskipTests=false
```

To mirror CI even closer (clean + same command):

```bash
./mvnw clean verify --no-transfer-progress -DskipTests=false
```

If this passes locally, the pipeline will usually pass on GitHub (same JDK 25, Maven, and tests).

### Option 2: Run the workflow in a container with act

[act](https://github.com/nektos/act) runs your `.github/workflows` in Docker, simulating the real runner.

1. Install act (e.g. Homebrew on macOS: `brew install act`).
2. From the repo root:

```bash
# List workflows and events
act -l

# Simulate push to main (runs the CI job)
act push

# Simulate a pull_request (same trigger as opening a PR)
act pull_request
```

The first run will pull the GitHub Actions runner image and may take a few minutes. Ensure Docker is running and has enough memory (e.g. 4GB+); Testcontainers will start Postgres/Kafka/Redis inside the same run.

### Manual linter commands

| Tool       | Command                    | Config |
|-----------|----------------------------|--------|
| Checkstyle| `./mvnw checkstyle:check` | `config/checkstyle.xml` |
| PMD       | `./mvnw pmd:check`        | `config/pmd.xml` |
| SpotBugs  | `./mvnw spotbugs:check`   | `config/spotbugs-exclude.xml` |

Fix reported files and line numbers to clear violations. The build fails if any rule is violated.

## Pre-commit hook

To run **Checkstyle, PMD, and SpotBugs** before each commit (no tests, for speed):

```bash
make install-hooks
# or
./scripts/setup-hooks.sh
```

Then every commit will run `make lint`. If there are violations, the commit is blocked.

## Configuration

- **Checkstyle** – `config/checkstyle.xml` (line length, imports, braces, etc.)
- **PMD** – `config/pmd.xml` (best practices, code style, design, error-prone, multithreading, performance)
- **SpotBugs** – `config/spotbugs-exclude.xml` (optional exclusions; add `<Match>` elements as needed)

All tools run in the Maven reactor for every module. No external service or authentication is required.
