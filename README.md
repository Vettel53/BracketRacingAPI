# Bracket Racing API

A REST API for tracking and analyzing 1/8th mile bracket racing runs, built with Spring Boot. Originally a Vaadin full-stack app; rebuilt as a standalone backend API with self-hosted JWT authentication.

## Overview

This API lets racers record, track, and analyze their drag racing performance data, including automatic weather correlation for each run. It's designed to be consumed by any client (web, mobile, CLI) - the backend has no opinion about UI.

## Features

- **Auth**: Register/login endpoints issuing self-hosted JWTs (HMAC-signed, no Spring Security dependency)
- **Run Tracking**: Create, update, delete, and list your own bracket racing runs, with per-user ownership enforcement
- **Automatic Weather Integration**: Weather conditions are fetched and attached to a run automatically based on track, date, and time
- **Statistics**: Breakout percentage, over percentage, and average reaction time across your runs
- **Fake Run Generation**: Optional dev-only endpoint (`POST /api/runs/fake`) for quickly seeding demo data

## Technology Stack

- **Backend**: Spring Boot 3.4, Spring Data JPA/Hibernate
- **Auth**: Self-hosted JWT (jjwt) + BCrypt (spring-security-crypto only, no Spring Security filter chain)
- **Database**: H2 (dev) / MySQL (prod), configurable via Spring profiles
- **Weather Data**: Custom microservice API [TrackWeatherAPI](https://github.com/Vettel53/TrackWeatherAPI) - shoutout [Air Density Online](http://airdensityonline.com/)

## API

All endpoints except `/api/auth/register` and `/api/auth/login` require an `Authorization: Bearer <token>` header.

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account, returns a JWT |
| POST | `/api/auth/login` | Log in, returns a JWT |
| GET | `/api/runs` | List your runs |
| POST | `/api/runs` | Create a run (weather is fetched automatically) |
| POST | `/api/runs/fake` | Generate a randomized demo run (dev only) |
| PUT | `/api/runs/{id}` | Update a run you own |
| DELETE | `/api/runs/{id}` | Delete a run you own |
| GET | `/api/statistics` | Breakout %, over %, average reaction time |

## Architecture

- **Controller layer**: REST endpoints, request/response DTOs at the boundary
- **Service layer**: Business logic (`RunService`, `WeatherService`, `AuthService`, `StatisticsService`)
- **Repository layer**: Spring Data JPA repositories
- **Security**: `JwtService` (sign/verify), `JwtAuthFilter` (plain servlet filter, not Spring Security) - resolves the caller's user id onto the request, controllers read it directly
- **Shared**: `GlobalExceptionHandler` + `BusinessException` for consistent error responses

## Running locally

```
./mvnw spring-boot:run
```

Defaults to the `h2` profile (in-memory DB, fake weather generation enabled). Set `jwt.secret` via `application.properties` or the `JWT_SECRET` env var for the mysql/railway profiles.

## Planned

- Vehicle management (multi-car support) - entity/service stub exists, not yet wired up
- Run prediction based on historical data and conditions

## License

MIT - see the LICENSE file for details.
