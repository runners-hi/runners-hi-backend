# Repository Guidelines

## Project Structure & Module Organization
The main application lives in `src/main/kotlin/com/runnershi/`. Cross-cutting code sits in `common/` (config, security, exceptions, API responses). Business logic is grouped by domain in `domain/` (`user`, `auth`, `running`). Configuration and migrations are in `src/main/resources/` with `application*.yaml` and Flyway scripts under `db/migration`. Tests live in `src/test/kotlin`, with test config in `src/test/resources`. Root-level build and runtime entry points include `build.gradle.kts`, `settings.gradle.kts`, and `docker-compose.yml`.

## Build, Test, and Development Commands
- `docker-compose up -d`: start PostgreSQL for local development.
- `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`: run the API using `application-local.yaml`.
- `./gradlew test`: run JUnit 5 tests (H2 via the test profile).
- `./gradlew build`: compile and package the application.
When running locally, Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## Coding Style & Naming Conventions
Follow Kotlin standard style with 4-space indentation and no tabs. Use lowercase package names, PascalCase for classes/objects, and camelCase for functions and properties. Keep shared infrastructure in `common/*` and domain-specific code in `domain/*`.

## Testing Guidelines
Use the Spring Boot test starters on JUnit Platform (JUnit 5). Place tests under `src/test/kotlin` and follow the existing `*Tests` suffix. Use `application-test.yaml` for test-only settings and avoid relying on external services.

## Commit & Pull Request Guidelines
The repository has no commit history yet, so no convention is established. Prefer short, imperative commit subjects (e.g., "Add running record API"). Pull requests should include a clear summary, the tests run, and any Flyway migration or configuration changes.

## Security & Configuration Tips
Set `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` for local runs. Do not commit secrets; keep local overrides in `application-local.yaml`.
