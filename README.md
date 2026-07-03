# Star Wars API

**Repository:** https://github.com/server23/starwars-api

Java REST API that wraps [SWAPI](https://swapi.dev) and exposes paginated people data, character details, and a mocked authentication flow.

## Requirements

- Java 21+
- Maven (or use the included Maven Wrapper)

## Quickstart

```bash
# Windows
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

## Tests

### Automated

```bash
.\mvnw.cmd test
```

23 automated tests cover the same scenarios as the manual Postman checklist below (without calling the live SWAPI in tests — `SwapiClient` is mocked).

| Test class | What it covers |
|------------|----------------|
| `PeopleApiTest` | `GET /people` pagination, validation, caching; `GET /people/{id}` format, 404, Jabba mass |
| `PeopleControllerTest` | JSON contract for character details |
| `PersonServiceTest` | SWAPI mapping, mass with comma, unknown values, cache |
| `AuthFlowTest` | Login, favourites, refresh, logout, invalid credentials/tokens |
| `AuthenticatedApiClientTest` | 401 → refresh → retry on `/favourites` |
| `StarwarsApiApplicationTests` | Spring context loads |

### Manual (Postman)

Tested on `http://localhost:8080` with **Postman Desktop** (server started via `.\mvnw.cmd spring-boot:run`).

#### 1. `GET /people` — paginated list

| Request | Expected | Result |
|---------|----------|--------|
| `GET /people?page=1` | 200, `count: 82`, 10 results (`id` + `name`, ids 1–10) | Pass |
| `GET /people?page=2` | 200, starts with Anakin (id 11), includes Jabba (id 16) | Pass |
| `GET /people?page=9` | 200, last page, `next: null`, 2 results (ids 82–83) | Pass |
| `GET /people?page=0` | 400, `"Page must be >= 1"` | Pass |
| `GET /people?page=-1` | 400 | Pass |
| `GET /people` (no page param) | 200, defaults to page 1 | Pass |

#### 2. `GET /people/{id}` — character details

| Request | Expected | Result |
|---------|----------|--------|
| `GET /people/1` (Luke) | 200, `height: 1.72`, `mass: 77.0`, `birth_year`, `number_of_films: 4`, `date_added: "09-12-2014"` | Pass |
| `GET /people/11` (Anakin) | 200, `height: 1.88`, `mass: 84.0` | Pass |
| `GET /people/16` (Jabba) | 200, `mass: 1358.0` (not 500) | Pass |
| `GET /people/12` (Tarkin) | 200, `mass: null` (SWAPI `"unknown"`) | Pass |
| `GET /people/999` | 404 | Pass |
| `GET /people/17` | 404 (missing SWAPI id) | Pass |
| `GET /people/0`, `/people/-1` | 400, `"Id must be >= 1"` | Pass |

#### 3. Authentication

| Request | Expected | Result |
|---------|----------|--------|
| `POST /auth/login` (`user` / `password`) | 200, `accessToken`, `refreshToken`, `user` | Pass |
| `POST /auth/login` (wrong credentials) | 401 | Pass |
| `GET /favourites` (no token) | 401 | Pass |
| `GET /favourites` (valid Bearer token) | 200, Luke, Darth Vader, Obi-Wan | Pass |
| `GET /favourites` (invalid token) | 401 | Pass |
| `POST /auth/refresh` (valid `refreshToken`) | 200, new `accessToken` | Pass |
| `POST /auth/refresh` (invalid token) | 401 | Pass |
| Favourites with old access token after refresh | 401 | Pass |
| Favourites with new access token after refresh | 200 | Pass |
| `POST /auth/logout` (valid `refreshToken`) | 204 | Pass |
| `GET /favourites` after logout | 401 | Pass |

Full auth flow (login → favourites → refresh → logout) exercised end-to-end in one session.

#### 4. Caching

Repeated `GET /people?page=1` three times: only the first call logged `Fetching SWAPI people page 1` in the server console; subsequent requests returned 200 from in-memory cache (0–4 ms, no extra SWAPI fetch).

## Code formatting

```bash
.\mvnw.cmd spotless:apply   # format code
.\mvnw.cmd spotless:check   # verify formatting (runs on build)
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/people?page=1` | Paginated list of characters from SWAPI |
| GET | `/people/{id}` | Character details in a custom format |
| POST | `/auth/login` | Mock login (`user` / `password`) |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Clear session (204) |
| GET | `/favourites` | Static favourites list (Bearer token required) |

### Example: login and favourites

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'

curl http://localhost:8080/favourites \
  -H "Authorization: Bearer <accessToken>"
```

## Authentication flow

Login accepts fixed credentials (`user` / `password`) and returns `accessToken`, `refreshToken`, and `user`. Tokens are stored in memory on the server. Protected routes require `Authorization: Bearer <accessToken>`. When an access token is invalid, clients can call `/auth/refresh` with the refresh token to obtain a new access token. Logout removes both tokens for the session.

`AuthenticatedApiClient` keeps tokens in memory and, on a 401 from `/favourites`, performs a single refresh attempt and replays the request. If refresh fails, it returns 401. This retry behaviour is covered by `AuthenticatedApiClientTest`.

## Logging

- **Request logging:** every HTTP request is logged with method, path, status code, and duration (`RequestLoggingFilter`).
- **SWAPI calls:** info on fetch, warn on 404, error with stack trace when SWAPI is unavailable (`SwapiClient`).
- **Errors:** warn for 400/401/404, error for 503 (`GlobalExceptionHandler`).

Log level for the application is `INFO` (see `application.properties`).

## Design decisions

- **Spring Boot + RestClient** for a simple HTTP client to SWAPI without extra dependencies.
- **In-memory caching** (`ConcurrentHashMap`) for SWAPI pages and person details to avoid redundant external calls.
- **Separate DTOs** for SWAPI responses and public API responses, so `/people/{id}` can expose the required format (`height` in meters, `date_added` as `dd-MM-yyyy`, etc.).
- **Mock auth with UUID tokens** instead of JWT to keep the assessment focused on flow and error handling.
- **Global exception handler** maps domain exceptions to HTTP 400, 401, 404, and 503.
- **Spotless** enforces consistent Java formatting.
