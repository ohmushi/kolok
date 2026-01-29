# Copilot coding agent instructions — `kolok`

## 1) What this repo is
- `kolok` is a **Spring Boot (4.0.1) + Kotlin** backend that manages planning/rotations/responsibilities and integrates with **Discord** (library: `dev.kord:kord-core-jvm`).
- Build tool: **Maven** (wrapper: `mvnw`, `mvnw.cmd`).
- Target runtime is the JVM; Docker is first-class (Dockerfile + Compose). Data files used at runtime live under `/data`.

High-level size/layout: single service backend repo (no frontend), conventional `src/main` + `src/test` tree, plus Docker/Compose and GitHub Actions.

## 2) Build / validate (always prefer these, don’t rediscover)
### Required local setup (Windows PowerShell)
**Required**: a JDK in PATH and `JAVA_HOME` configured.
- Observed failure when missing:
  - `./mvnw.cmd -B -ntp clean verify` -> `The JAVA_HOME environment variable is not defined correctly`
  - `java -version` / `mvn -v` not found when Java/Maven aren’t installed.

Recommended tool versions (from repo):
- `pom.xml`: `java.version=24`, `kotlin.version=2.2.21`.

### Commands (PowerShell)
Run from repo root:
- Bootstrap dependencies + compile + tests (CI-equivalent):
  - `./mvnw.cmd -B -ntp clean verify`
- Unit tests only:
  - `./mvnw.cmd -B -ntp test`
- Package without tests (local only; avoid for PR validation):
  - `./mvnw.cmd -B -ntp -DskipTests package`
- Run app:
  - `./mvnw.cmd -B -ntp spring-boot:run`

Postconditions:
- Successful build produces `target/*.jar`.

Lint/format:
- No dedicated linter/formatter config was found in the repo. Keep diffs minimal and follow existing Kotlin style.

## 3) CI/CD you must not break
GitHub Actions:
- `/.github/workflows/ci.yaml`: runs `mvn -B -ntp clean verify` on Ubuntu with **Temurin JDK 24**.
- `/.github/workflows/docker-publish.yaml`: publishes Docker image to GHCR after `ci` succeeds (`workflow_run`).

Local replication: run `./mvnw.cmd -B -ntp clean verify` before finishing.

## 4) Project layout / architecture (where to change things)
Main entry point:
- `src/main/kotlin/cat/ohmushi/kolok/planning/KolokApplication.kt` (`runApplication<KolokApplication>()`).

Architecture is “hexagonal/clean-ish” by package:
- `.../domain/`: domain model (keep **Spring-free**). Exceptions under `domain/exceptions`.
- `.../application/`: use-cases/services + ports.
- `.../adapters/in/`: inbound adapters (REST/Discord/scheduler).
- `.../adapters/infrastructure/`: infra implementations (DB, repositories, external clients).
- `.../bootstrap/Wiring.kt`: wiring/composition.

Testing:
- `src/test/kotlin/...`: unit/integration tests (Spring context test exists).
- Note: there is a `TODO("Not yet implemented")` in `PlanningServiceTest` in a fake repository method; avoid calling that path or complete it if you touch it.

## 5) Docker / Compose (common pitfalls)
- `Dockerfile` (multi-stage): builds jar with Maven, runs on `eclipse-temurin:25-jre-alpine`, copies `/data` folder.
- `compose.yaml` runs service `kolok` + healthcheck via `/actuator/health`.
- `compose.db.yaml` adds a Postgres service under profile `db` and adds `depends_on` from `kolok` -> `db`.

**Profiles gotcha**: a service behind a profile is treated as “undefined” unless the profile is enabled. If `kolok` depends on `db`, you must enable the `db` profile (or use the override file).

Config/secrets:
- `src/main/resources/application.properties` uses environment variables for Discord token/channel and JSON path. Keep secrets out of git; `.env` is gitignored.

## 6) Repo root inventory (high-signal)
- Build: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`
- Docker: `Dockerfile`, `compose.yaml`, `compose.db.yaml`
- Docs: `HELP.md`, `COPILOT_CONTEXT.md`, `structure.txt`
- Data: `data/`
- Source: `src/`

## 7) Operating principle
Trust this file first. Only search the repo when instructions are missing or contradict the current codebase.
