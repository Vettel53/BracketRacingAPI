# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a REST API for tracking 1/8th-mile bracket racing runs. It was originally a Vaadin full-stack app (Java-rendered UI + backend in one process) and was just migrated to a backend-only REST API with self-hosted JWT auth. There is no frontend in this repo anymore, and none is planned - a REST client (Postman, curl, or a separate frontend project) is how it's meant to be consumed.

`_DELETE_ME/` at the repo root holds the old Vaadin views, the frontend build tooling (package.json, vite config, generated Vaadin assets), and other files made obsolete by the migration. They were moved there instead of deleted because the environment that did the migration couldn't unlink files on the mounted drive. Delete that whole folder once you've confirmed nothing in it is still needed - nothing in `src/main/java` references it.

`src/test` does not exist yet - there are no tests in this repo currently.

## Migration to a new project (do this first)

This folder (`BracketRacingFrontend`) is the user's original repo and needs to stay as-is for their records. The Vaadin-to-REST-API rewrite described in the rest of this file only exists as **uncommitted working-tree changes** on top of the last commit - nothing has been committed here. Before doing anything else described below, migrate the rewrite out to its own project:

1. Create `C:\Users\Javier\Intelij Projects\BracketRacingAPI` (a new sibling folder directly under `Intelij Projects`).
2. Copy the current working tree - the rewritten REST API code - into that new folder. Copy everything except `.git/`, `target/`, `.idea/`, `.vscode/`, and `_DELETE_ME/` (that folder holds the old Vaadin files being restored in step 5, not part of the new project). `ARCHITECTURE_REVIEW.md` is worth copying over too as background on why some of the changes were made.
3. In the new folder, run `git init` and make the first commit there. This is a fresh repo - it should not inherit `BracketRacingFrontend`'s Vaadin-era commit history.
4. Verify the new project builds: `./mvnw clean compile` inside `BracketRacingAPI`.
5. Back in `BracketRacingFrontend`, discard the rewrite and restore the original Vaadin app:
   - `git reset --hard HEAD` - restores all tracked files (Vaadin views, old Spring Security config, etc.) to their last committed state.
   - Delete the now-redundant `_DELETE_ME/` folder - its contents are already restored to their original tracked locations by the reset above.
   - Delete this `CLAUDE.md` and `ARCHITECTURE_REVIEW.md` from this folder (assuming they were copied over in step 2) - they describe `BracketRacingAPI`'s architecture, not what's left here after the revert.
6. Confirm `BracketRacingFrontend` is back to a clean original Vaadin state (`git status` shows nothing to commit) before considering this done.

Everything below this section - Commands, Architecture - describes `BracketRacingAPI`, not this folder, once the migration is complete.

## Commands

Build/run (Maven wrapper, no local Maven install needed):
```
./mvnw clean compile
./mvnw spring-boot:run
```

Runs on the `h2` profile by default (`spring.profiles.active=h2` in `application.properties`) - in-memory DB, fake weather generation, fake-run endpoint all enabled. To run against MySQL instead:
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Tests (once they exist):
```
./mvnw test                                    # full suite
./mvnw test -Dtest=RunServiceTest              # single class
./mvnw test -Dtest=RunServiceTest#createsRun   # single method
```

Integration tests run under the `it` Maven profile (starts the app via `spring-boot-maven-plugin` before running `*IT` classes via failsafe):
```
./mvnw verify -Pit
```

Package for deployment:
```
./mvnw clean package -Pit    # or just `clean package` for a plain jar
```
The `Dockerfile` builds via `mvn clean package -Pproduction` - that profile no longer exists post-migration (it was Vaadin's frontend-build profile) and should be updated or removed before the Docker build is used again.

## Architecture

**Auth is not Spring Security.** There is no `SecurityFilterChain`, no `UserDetailsService`, no `@AuthenticationPrincipal`. Authentication is a self-hosted JWT scheme:
- `security/JwtService` signs and verifies HMAC tokens (`jwt.secret` property, read from the `JWT_SECRET` env var for every profile - there is no committed default, so it must be exported before running locally; `jwt.expiration-ms` similarly reads `JWT_EXPIRATION_MS`, defaulting to 3600000 if unset).
- `security/JwtAuthFilter` is a plain `OncePerRequestFilter` (auto-registered by Spring Boot because it's a `Filter` bean, not because of any security config). It validates the `Authorization: Bearer <token>` header and, on success, sets `userId` and `username` as **request attributes** - it does not populate a `SecurityContext`.
- Controllers read the caller by pulling `request.getAttribute("userId")` directly (see `RunController.currentUser()` / `StatisticsController.currentUser()`) and look up the `AppUser` via `UserService`. Any new controller needing the current user should follow this same pattern rather than introducing Spring Security.
- `PasswordEncoderConfig` provides the only `PasswordEncoder` bean (BCrypt) - the project depends on `spring-security-crypto` alone, not the full security starter, specifically to get BCrypt without pulling in a filter chain.
- Public (unauthenticated) routes are hardcoded in `JwtAuthFilter.PUBLIC_PATHS` (`/api/auth/register`, `/api/auth/login`). New public endpoints must be added there or they'll get rejected by the filter.

**Layering and DTO boundary.** Each feature package (`account`, `run`, `weather`, `statistics`) follows controller → service → repository → entity. Entities (`AppUser`, `Run`, `Weather`) are never returned directly from controllers - each feature has a `dto/` subpackage plus a static `*Mapper` class (`RunMapper`, `WeatherMapper`) that converts between entities and request/response DTOs. Follow this pattern for any new entity-backed feature rather than serializing entities directly.

**Run/Weather relationship.** `Weather` is the owning side of a one-to-one with `Run` (holds the `run_id` FK via `@JoinColumn`), while `Run.weather` is the inverse side with `cascade = CascadeType.REMOVE` - deleting a `Run` cascades to delete its `Weather` row. When editing a run's weather in place, update the existing `Weather` entity's fields rather than creating a new one, or you'll end up with orphaned rows (see `WeatherService.updateWeather`).

**Weather fetch flow.** Creating a run (`RunService.createRun`) automatically fetches weather for the run's track/date/time via `WeatherService`, unless the `weatherapi.fake-weather` property is true, in which case it uses `WeatherService.getFakeWeather()` (fixed placeholder values, no HTTP call). This flag is on by default in the `h2` profile. The real weather call goes to the sibling project [TrackWeatherAPI](https://github.com/Vettel53/TrackWeatherAPI) - see `WeatherService.getAPIURL()` for local vs. deployed URL selection (`weatherapi.local` property).

**Ownership checks.** `RunController` resolves and authorizes a run via `ownedRunOrThrow()`, which 404/400s if the run doesn't belong to the caller's `userId`. This check did not exist before the migration (the old Vaadin dashboard only scoped by session, with no per-request ownership check on edit/delete) - any new mutating endpoint on a per-user resource should include an equivalent check rather than trusting the path parameter alone.

**Error handling.** `shared/exception/ApiException` + `shared/exception/GlobalExceptionHandler` (`@RestControllerAdvice`) are the only error path. `ApiException(HttpStatus status, String code, String message)` carries its own response status and a stable machine-readable `code` (e.g. `RUN_NOT_FOUND`, `INVALID_CREDENTIALS`) - throw it directly with the right status for the failure rather than always defaulting to 400. The handler serializes it to `{timestamp, status, code, message}`. `MethodArgumentNotValidException` (bean validation) maps to 400 with code `VALIDATION_FAILED`; anything unhandled falls through to 500 with code `INTERNAL_ERROR`. Any new failure mode should get its own `ApiException(status, code, message)` call site rather than a stringly-typed message.

**Config profiles.** `spring.profiles.active` (set in `application.properties`, default `h2`) selects `application-{h2,mysql,railway}.properties`. `JWT_SECRET` is required for every profile, including `h2` - it's read once in the base `application.properties` (`jwt.secret=${JWT_SECRET}`), not per-profile. `mysql`/`railway` additionally expect DB credentials from environment variables - don't hardcode secrets into those two files.

**Incomplete/stub areas** (flagged so you don't assume they're finished):
- `vehicles/Vehicle` + `vehicles/VehicleService` are stubs with no repository, controller, or ownership relationship to `AppUser` yet.
- `WeatherService.updateWeather()` exists but isn't called from anywhere - editing a run's track/date/time via `RunController.updateRun` does not currently refresh the attached weather.
