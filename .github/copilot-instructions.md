# Copilot coding agent instructions — `kolok`

## 1) What this repo is
- `kolok` is a **Spring Boot (4.0.1) + Kotlin** backend that manages planning/rotations/responsibilities and integrates with **Discord**.
  - Discord libs: `dev.kord:kord-core-jvm` and `net.dv8tion:JDA`.
- Build tool: **Gradle (Kotlin DSL)**.
- Target runtime is the JVM; Docker is first-class (Dockerfile + Compose). Data files used at runtime live under `/data`.

High-level size/layout: single service backend repo (no frontend), conventional `src/main` + `src/test` tree, plus Docker/Compose and GitHub Actions.

## 2) Build / validate (always prefer these, don’t rediscover)
### Required local setup (Windows PowerShell)
**Required**: a JDK in PATH and `JAVA_HOME` configured.
- If missing, builds fail with “JAVA_HOME … not defined correctly” / `java` not found.

Tool versions (from repo):
- Spring Boot: `4.0.1`
- Kotlin: `2.2.21`
- Java toolchain: `24` (see `build.gradle.kts`)

### Commands (PowerShell)
Run from repo root:
- Bootstrap + compile + tests (CI-equivalent):
  - `./gradlew.bat --no-daemon clean test bootJar`
- Unit tests only:
  - `./gradlew.bat --no-daemon test`
- Run app:
  - `./gradlew.bat --no-daemon bootRun`

Postconditions:
- Successful build produces `build/libs/*.jar`.

Lint/format:
- No dedicated linter/formatter config was found in the repo. Keep diffs minimal and follow existing Kotlin style.

## 3) CI/CD you must not break
GitHub Actions:
- `/.github/workflows/ci.yaml`: runs `./gradlew --no-daemon clean test bootJar` on Ubuntu with **Temurin JDK 25**.
- `/.github/workflows/docker-publish.yaml`: publishes Docker image to GHCR after `ci` succeeds (`workflow_run`).

Local replication: always run `./gradlew.bat --no-daemon clean test bootJar` before finishing.

## 4) Project layout / architecture (where to change things)
Main entry point:
- `src/main/kotlin/cat/ohmushi/kolok/planning/KolokApplication.kt` (`runApplication<KolokApplication>()`).

Architecture is “hexagonal/clean-ish” by package:
- `.../domain/`: domain model (keep **Spring-free**).
- `.../application/`: use-cases/services + ports.
- `.../adapters/in/`: inbound adapters (REST/Discord/scheduler).
- `.../adapters/infrastructure/`: infra implementations (JSON persistence, external clients).
- `.../bootstrap/Wiring.kt`: wiring/composition.

Testing:
- `src/test/kotlin/...`: unit/integration tests.

## 5) Docker / Compose (common pitfalls)
- `Dockerfile` uses Gradle build stage and runs on `eclipse-temurin:25-jre-alpine`, copies `/data` folder.
- `compose.yaml` runs service `kolok` + healthcheck via `/actuator/health`.
- `compose.db.yaml` adds a Postgres service under profile `db` and adds `depends_on` from `kolok` -> `db`.

**Profiles gotcha**: a service behind a profile is treated as “undefined” unless the profile is enabled. If `kolok` depends on `db`, you must enable the `db` profile (or use the override file).

Config/secrets:
- `src/main/resources/application.properties` uses environment variables for Discord token/channel/guild and JSON path. Keep secrets out of git; `.env` is gitignored.

## 6) Repo root inventory (high-signal)
- Build: `build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `gradlew.bat`, `gradle/`
- Docker: `Dockerfile`, `compose.yaml`, `compose.db.yaml`
- Docs: `HELP.md`, `COPILOT_CONTEXT.md`, `structure.txt`, `README.md`
- Data: `data/`
- Source: `src/`

## 7) Operating principle
Trust this file first. Only search the repo when instructions are missing or contradict the current codebase.
