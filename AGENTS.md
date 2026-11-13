# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/lsb/listProjectBackend/` contains the Spring Boot backend. Packages separate REST `controller`, business `service`, JPA `entity`/`repository`, transfer objects in `domain`, MapStruct `mapper`, and shared `config`/`utils` code.
- Organize features under existing subpackages such as `connection`, `dataset`, and `spider`.
- `src/main/resources/` holds application YAML and SQL schemas, including SQLite and PostgreSQL variants. Tests live in `src/test/java/`.
- `docs/` contains feature and API documentation; `spec/` contains specifications. `public/` holds supplementary assets. Generated build output belongs in `build/`.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root with a Java 21+ JDK. Gradle is the active build system; the Maven wrappers and README references are legacy.

- `.\gradlew.bat build` — compile, run tests, and package artifacts.
- `.\gradlew.bat test` — run the JUnit suite.
- `.\gradlew.bat test --tests '*ApiRequestConfigContractTest'` — run a focused test class.
- `.\gradlew.bat bootRun` — start locally on port `8081`, under `/list-project-backend`.
- `.\gradlew.bat bootWar` — create `build/libs/list-project-backend.war`.
- `.\gradlew.bat installPlaywrightChromium` — install Playwright's bundled Chromium.

On Unix, substitute `./gradlew` for `.\gradlew.bat`.

## Coding Style & Naming Conventions
Use UTF-8, four-space indentation, `PascalCase` class names, and `camelCase` methods and fields. Follow nearby formatting. Use established suffixes: `Controller`, `Service`/`ServiceImpl`, `Repository`, `Mapper`, and `TO`. Prefer constructor injection, matching existing Lombok `@RequiredArgsConstructor` usage. Keep persistence mapping in MapStruct mappers. No formatter or lint task is configured.

## Testing Guidelines
Tests use JUnit Jupiter, Spring Boot Test, and Mockito. Name classes `*Test` and methods after observable behavior. Cover changed behavior, mapping contracts, and relevant edge cases; no coverage threshold is configured. Keep tests independent of developer databases and external services. Browser smoke tests require installed Google Chrome and PowerShell `$env:PLAYWRIGHT_SMOKE_TEST='true'` before running tests.

## Commit & Pull Request Guidelines
History uses prefixes such as `feat:`, `refactor:`, `test:`, and `chore:`, often with Traditional Chinese descriptions. Keep commits focused. PRs should explain behavior changes, link relevant issues, record validation commands/results, and document configuration or schema changes. Update `docs/` when API contracts change.

## Configuration & Data Safety
Override the machine-specific datasource path in `application.yml` for local development. Never commit credentials, browser profiles, or local database contents. Keep SQLite/PostgreSQL schema variants aligned when persistence changes affect both.
