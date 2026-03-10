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
