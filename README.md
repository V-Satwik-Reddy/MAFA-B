# Multi Agent Financial Assistant (MAF)

MAF (Multi Agent Financial Assistant) is a Spring Boot backend that provides account management, portfolio/dashboard data, stock price fetching, trade execution endpoints, and a multi-agent chat subsystem with optional direct LLM integration.

This README documents the project's purpose, configuration, authentication, all HTTP endpoints (only authentication endpoints are documented as public), error behaviors, observability (Prometheus/Grafana), performance testing guidance (JMeter + performance_data.xlsx), and step-by-step run instructions for Windows (cmd.exe) including optional services.

---

## Table of contents

- Project overview
- Key features (recent changes)
- Configuration (properties & environment variables)
- Authentication & token lifecycle
- Endpoints (overview; only auth endpoints are public)
- Chat subsystem & Gemini LLM integration
- Observability: Prometheus, Grafana, metrics
- Performance testing: JMeter and `performance_data.xlsx`
- Running the app (Windows cmd.exe) and Docker services (Redis, Prometheus, Grafana)
- Troubleshooting & notes

---

## Project overview

MAF is a backend service offering:

- Signup / Login (JWT access token + refresh token in HttpOnly cookie)
- Profile endpoints for user info, balance and holdings
- Dashboard and transaction endpoints
- Stock price fetching and daily prices
- Execution endpoints to buy / sell -> creates Transaction records and updates holdings
- Multi-agent chat endpoints: general, execution-assistant (EA), market-research-assistant (MRA)
  - Chat queries are persisted per user and can be forwarded to agent services or an LLM when configured

The project uses Spring Boot (Java 17+), Spring Security, Spring Data JPA / Hibernate, Micrometer for metrics, and the Java HTTP client for outward requests.

---

## Key features (recent changes)

- Chat endpoints now accept a compact request body: `{ "query": "..." }`.
- `general-chat` can call Gemini LLM when `gemini.api.key` is present; otherwise requests are forwarded to configured agent endpoints.
- Chat requests are persisted with the authenticated `userId` so the service can remember users and maintain chat history.
- Chat endpoint responses are returned as plain `String` and are also saved as chat history.
- Redis cache option has been disabled in this codebase (the cache wiring was commented out). Redis is no longer required for normal operation; configuration and Docker snippets remain for optional setups but are not necessary.
- Observability: Micrometer instrumentation with Prometheus exposition via Spring Boot Actuator (`/actuator/prometheus`) is included.
- Performance testing artifacts: `performance_data.xlsx` at repo root contains prior benchmark results and guidance.

---

## Configuration

Place properties in `src/main/resources/application.properties` or provide them as environment variables.

Important properties:

- `jwt.secret` (required) - secret used to sign access tokens (HS256); must be sufficiently strong.
- `allowed_origins` - Comma-separated list for CORS (e.g., `http://localhost:3000`).

Optional / integrations

- `gemini.api.key` - API key used to call the Gemini LLM (if you want chat to hit Gemini directly).
- `gemini.endpoint` - Gemini API endpoint (optional; a sensible default is used in code when absent).
- `agents_endpoint` - Base URL for agent services (forwards chat queries when Gemini is not used).
- Redis config (deprecated/disabled by default in the repo): `spring.redis.host`, `spring.redis.port`, `spring.redis.password` — note the project currently comments out caching code and does not rely on Redis.
- `management.endpoints.web.exposure.include` - include `prometheus` and `health` for the actuator (recommended: `health,info,prometheus`).

Example snippet to add to `application.properties`:

```
# Security
jwt.secret=YOUR_BASE64_SECRET_HERE
allowed_origins=http://localhost:3000

# Agents & LLM
agents_endpoint=http://localhost:8081/agents/
# Optional Gemini LLM
gemini.api.key=YOUR_GEMINI_API_KEY
# gemini.endpoint=https://api.gemini.example/v1/complete

# Redis (deprecated/disabled) - not required for normal operation
# spring.redis.host=localhost
# spring.redis.port=6379
# spring.redis.password=

# Actuator / Prometheus
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
```

Secrets such as `gemini.api.key` should be provided via environment variables on production or CI.

---

## Authentication & token lifecycle

Auth flow summary:

- `POST /auth/signup` — create account, public
- `POST /auth/login` — returns `ApiResponse` with `data` containing `accessToken` and `user` fields; also sets `refresh_token` cookie (HttpOnly)
- Protected endpoints require header: `Authorization: Bearer <accessToken>`
- `POST /auth/refresh` — uses the `refresh_token` cookie to issue a new access token
- `POST /auth/logout` — clears the refresh cookie

Security rules (from `SecurityConfig`) - typical mapping in this project:

- Public (no auth required): `/auth/**`, `/` (home/health), and `OPTIONS /**`.
- All other endpoints require a valid access token.

Token details:

- Access token: short-lived JWT in response; send in Authorization header.
- Refresh token: long-lived, stored as HttpOnly cookie with name `refresh_token`.

---

## Endpoints (overview)

Important: Only authentication endpoints are public and fully documented below. All other endpoints in the API require authentication (send `Authorization: Bearer <token>`) — brief descriptions are provided but they are not documented as public.

Authentication (AuthController) — Public

- POST /auth/signup
  - Public
  - Purpose: Create a user account. Accepts signup details and returns created user info inside an ApiResponse.

- POST /auth/login
  - Public
  - Purpose: Authenticate a user. Returns an access token (in the response body) and sets a `refresh_token` cookie (HttpOnly).

- POST /auth/refresh
  - Public (requires `refresh_token` cookie)
  - Purpose: Exchange a valid refresh cookie for a fresh access token.

- POST /auth/logout
  - Public
  - Purpose: Revoke/clear refresh cookie and log out user.

Other controllers (Auth required)

- ProfileController
  - GET /profile/me — Auth: Yes — returns user profile
  - PUT /profile/me — Auth: Yes — update profile
  - GET /profile/balance — Auth: Yes — returns account balance
  - GET /profile/holdings — Auth: Yes — returns holdings

- DashboardController
  - GET /transactions — Auth: Yes — list user transactions
  - GET /dashboard — Auth: Yes — aggregated holdings / dashboard data

- PriceFetchController
  - GET /stockprice?symbol=SYMBOL — Auth: Yes
  - GET /stockdailyprices?symbol=SYMBOL — Auth: Yes
  - GET /stockchange?symbol=SYMBOL — Auth: Yes
  - (Background job trigger endpoints, if present, may be less restricted in internal deployments — check controller annotations)

- ExecutionController
  - POST /execute/buy — Auth: Yes
  - POST /execute/sell — Auth: Yes

- ChatController (chat endpoints) — Auth: Yes
  - POST /general-chat — Auth: Yes — accepts `{ "query": "..." }` and returns a plain String (assistant response)
  - POST /ea-chat — Auth: Yes — execution-assistant
  - POST /mra-chat — Auth: Yes — market-research-assistant
  - GET /chats — Auth: Yes — get chat history for authenticated user

Errors and conventions:

- Most endpoints return `ApiResponse<T>` with `success`, `message`, and `data` except chat endpoints that return a plain `String` (the assistant response).
- Common HTTP codes used: 400 (bad request), 401 (unauthorized), 403 (forbidden), 404 (not found), 409 (conflict), 500 (server error).
- The application uses a global exception handler to return structured errors.

---

## Chat subsystem & Gemini LLM integration

Behavior summary:

- Request shape: All chat POST endpoints expect a compact JSON body: `{ "query": "..." }`.
- The authenticated user is inferred from the access token; the service saves the incoming user message in the chat history table with the `userId` and timestamp.
- general-chat behavior:
  - If `gemini.api.key` is present in configuration, ChatService will POST to the Gemini endpoint with a body similar to `{ "query": "<user query>", "userId": <id> }` and `Authorization: Bearer <GEMINI_KEY>` (or other header required by your Gemini provider), then extract the assistant text reply.
  - If Gemini is not configured, ChatService forwards the request to `agents_endpoint + "general-agent"` and forwards the user's JWT so the agent can authorize/recognize the user.
  - The chosen response (from Gemini or agent) is stored as the `agentResponse` in the chat record and returned to the caller as a plain `String`.
  - If agent/LLM is unreachable the service returns a friendly fallback message such as: `The chat assistant is currently being improved; please try again later.`

Security and persistence:

- Chats are stored per `userId`, enabling the service to reference a user's prior messages for future context.
- The service only returns the assistant message as a String to simplify client usage; clients wanting more structure can retrieve chat history via `GET /chats`.

Notes on Gemini integration:

- The code expects Gemini to return JSON with a data field containing assistant text. Adjust parsing logic if your Gemini endpoint returns a different schema.
- Keep `gemini.api.key` secret — provide via environment variables in production.

---

## Observability: Prometheus & Grafana (metrics)

The project includes Micrometer + Spring Boot Actuator support to expose runtime metrics that Prometheus can scrape. Typical setup steps:

1) Enable prometheus endpoint in `application.properties` (see Configuration section):
```
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
```

2) Minimal Prometheus config (`prometheus.yml`):

```
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: 'maf'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

3) Run Grafana and add Prometheus as a data source. Useful dashboards:
- JVM memory and GC
- HTTP request count, errors, latency percentiles (p50/p95/p99)
- Custom app metrics (chat request counts, agent latency)

Docker example (Windows cmd.exe):

```
REM Start Prometheus (uses default prometheus.yml on cwd)
docker run -p 9090:9090 -v %cd%\prometheus.yml:/etc/prometheus/prometheus.yml --name prometheus -d prom/prometheus

REM Start Grafana
docker run -d -p 3000:3000 --name grafana grafana/grafana
```

Prometheus will scrape `/actuator/prometheus` if the JVM app is reachable by the Prometheus container or host.

Suggested metrics during performance testing:
- request rate per-endpoint
- latency percentiles (p50/p95/p99)
- error rate
- JVM memory and GC metrics
- DB connection pool usage

---

## Performance testing & `performance_data.xlsx`

The repository includes `performance_data.xlsx` at the project root. It contains previous benchmark results and per-endpoint latency/throughput breakdowns. Use it as a baseline for comparison.

How to reproduce tests and update the spreadsheet:

1) Start the application and any required services (database). Redis is not required by default because caching was disabled in the codebase.
2) Use JMeter (recommended), `wrk`, or `hey` to generate load and collect latency/throughput.
3) Export JMeter summary and copy p50/p95/p99, throughput, and error counts into `performance_data.xlsx`.

JMeter quick-start (Windows cmd.exe):

```
REM Run JMeter non-GUI on Windows cmd.exe
jmeter -n -t test-plan.jmx -l results.jtl -e -o report-folder
```

JMeter test plan notes:
- Add an HTTP Header Manager with `Authorization: Bearer <token>` for protected endpoints.
- Use `{ "query": "load test message" }` as the JSON body when testing chat endpoints.

---

## Running the app (Windows cmd.exe)

Prereqs:
- Java 17+
- Maven (or use the included `mvnw.cmd`)
- Docker (recommended for optional services like Prometheus/Grafana; Redis is optional and not required by default)

1) (Optional) Start Redis if you still want it for experimentation (the project's cache layer is commented out; Redis is not required):

```
REM Start a Redis container (optional)
docker run -p 6379:6379 --name redis -d redis:7
```

2) Set environment variables (cmd.exe example):

```
set JWT_SECRET=your-base64-secret
set GEMINI_API_KEY=your_gemini_key
set AGENTS_ENDPOINT=http://localhost:8081/agents/
```

3) Run the app with Maven wrapper in Windows cmd.exe:

```
mvnw.cmd spring-boot:run
```

Or build and run the JAR:

```
mvnw.cmd package
java -jar target/MAFA-0.0.1-SNAPSHOT.jar
```

4) Visit health and metrics endpoints:
- Health: http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8080/actuator/prometheus

---

## Example requests (Postman / curl)

Login and receive tokens (public):

```
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"user@example.com","password":"pass"}'
```

Call a protected chat endpoint after receiving an access token (Windows cmd.exe curl example):

```
curl -X POST http://localhost:8080/general-chat -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"query\":\"Hello assistant\"}"
```

If token is missing or expired you'll receive 401 Unauthorized. If refresh token is expired, `POST /auth/refresh` will return 403.

---

## Troubleshooting

- Authentication failing: ensure `jwt.secret` is set and the token is signed using that secret. If tokens are rejected, check system time and token expiry.
- Chat requests failing: increase logging for `majorproject.maf.service.ChatService` and `majorproject.maf.controller.ChatController` to see outgoing HTTP requests and responses.
- Prometheus not scraping: check `prometheus.yml` target address and network connectivity.
- If agent forwarding fails (503), the fallback response will be returned; check `agents_endpoint` and network connectivity.

---

## Where to look in the code

- `majorproject.maf.controller` — controllers (AuthController, ChatController, DashboardController, PriceFetchController, ExecutionController, ProfileController)
- `majorproject.maf.service` — business logic (AuthService, ChatService, ExecutionService, PriceFetch)
- `majorproject.maf.model` — JPA entities (User, Chat, Transaction, Stock, StockPrice)
- `majorproject.maf.exception` — global exception handling and custom exceptions
- `src/main/resources/application.properties` — example properties referenced above

---

## performance_data.xlsx

File location: `performance_data.xlsx` at the project root.

Contents and how to use:
- The workbook contains sheets summarizing previous benchmark runs: latency percentiles, throughput, error counts per endpoint, and resource usage across concurrency levels.
- Use it as a baseline and paste new results from JMeter or other tools into the sheets for comparisons.

---

If you want, I can also:
- Add an example `prometheus.yml` and a minimal Grafana dashboard JSON to the repository.
- Create a sample JMeter test plan (`.jmx`) for the `/general-chat` endpoint and add a short script to run non-GUI tests and export results to CSV.

If you want any of those additions, tell me which and I'll add them to the repo.

---

Last updated: 2026-01-31
