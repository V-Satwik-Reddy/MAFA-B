# Multi Agent Financial Assistant (MAF)

MAF (Multi Agent Financial Assistant) is a Spring Boot backend that provides account management, portfolio/dashboard data, stock price fetching, trade execution endpoints, and a multi-agent chat subsystem with optional direct LLM integration.

This README documents the project's purpose, configuration, authentication, all HTTP endpoints (method, path, auth requirement), error behaviors, observability (Prometheus/Grafana), performance testing guidance (JMeter + performance_data.xlsx), and step-by-step run instructions for Windows (cmd.exe) including Redis and optional services.

---

## Table of contents

- Project overview
- Key features (recent changes)
- Configuration (properties & environment variables)
- Authentication & token lifecycle
- Endpoints (detailed) + error behaviors
- Chat subsystem & LLM (Gemini) integration
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
  - Chat queries are persisted per user and forwarded to agent services (or an LLM when configured)

The project uses Spring Boot (Java 17+), Spring Security, Spring Data JPA / Hibernate, Micrometer for metrics, and the Java HTTP client for outward requests.

---

## Key features (recent changes)

- Chat endpoints now accept a compact request body: `{ "query": "..." }` (instead of older `userQuery` property). Controllers and DTOs were updated accordingly.
- Chat `general-chat` will call Gemini LLM if `gemini.api.key` is present and configured; otherwise forwarded to an agents microservice configured by `agents_endpoint`.
- Chat traceability: the `userId` is sent to the agent/LLM so user-specific context can be preserved and chats persisted.
- Chat endpoint responses are returned as plain `String` containing the assistant response; they are also saved to the DB as chat history.
- Added observability: Micrometer instrumentation with prometheus exposition via Spring Boot Actuator (`/actuator/prometheus`).
- Redis support documented (used for caching or Spring Session when enabled).
- Performance testing artifacts: `performance_data.xlsx` shipped at repository root with prior benchmark results.

---

## Configuration

Place properties in `src/main/resources/application.properties` or provide them as environment variables.

Minimum / important properties

- `jwt.secret` (required) - Base64-encoded secret used to sign access tokens (HS256); must be long enough (256-bit recommended).
- `agents_endpoint` - Base URL for agent services (forwards chat queries). Example: `http://localhost:8081/agents/`.
- `allowed_origins` - Comma-separated list for CORS (e.g., `http://localhost:3000`).

Optional / integrations

- `gemini.api.key` - API key used to call the Gemini LLM (if you want chat to hit Gemini directly).
- `gemini.endpoint` - Gemini API endpoint (if not using the default configured in code).
- `spring.redis.host`, `spring.redis.port`, `spring.redis.password` - Redis config (optional).
- `management.endpoints.web.exposure.include` - include `prometheus` and `health` for the actuator (recommended: `health,info,prometheus`).
- `management.endpoint.prometheus.enabled=true` - Expose prometheus endpoint.

Example snippet to add to `application.properties`:

```
# Security
jwt.secret=YOUR_BASE64_SECRET_HERE
allowed_origins=http://localhost:3000

# Agents & LLM
agents_endpoint=http://localhost:8081/agents/
# Optional Gemini LLM
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.endpoint=https://api.gemini.example/v1/complete

# Redis (optional)
spring.redis.host=localhost
spring.redis.port=6379
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

Security rules (from `SecurityConfig`) - typical default mapping in this project:

- Public (no auth required): `/`, `/auth/**`, `OPTIONS /**`, and `/jobs/**`.
- All other endpoints require a valid access token.

Token details:

- Access token: short-lived JWT in response; send in Authorization header.
- Refresh token: long-lived, stored as HttpOnly cookie with name `refresh_token`.

---

## Response & Error conventions

Most endpoints return an `ApiResponse<T>` JSON wrapper with fields: `success` (boolean), `message` (string), and `data` (payload). Chat endpoints return a plain `String` in the response body (the agent/LLM message) in the current implementation.

Common HTTP error cases and status codes used by the API:

- 400 Bad Request — invalid request body or missing required fields.
- 401 Unauthorized — missing or invalid access token.
- 403 Forbidden — invalid refresh token, or access to resource denied.
- 404 Not Found — requested resource does not exist.
- 409 Conflict — for resource creation conflicts (e.g., duplicate email) where applicable.
- 422 Unprocessable Entity — validation errors (when used).
- 500 Internal Server Error — unexpected exceptions; service should return helpful `message` within `ApiResponse`.

Controller advice (`GlobalExceptionHandler`) intercepts exceptions and returns structured `ApiResponse` with `success=false` and a user-friendly message. Inspect `exception` package to see which exceptions are handled explicitly.

---

## Endpoints (complete, with details)

Note: Unless marked "Public" these endpoints require a valid `Authorization: Bearer <token>` header.

1) Home
- GET /
  - Auth: No
  - Purpose: Health / welcome
  - Response: ApiResponse<String>

2) Authentication (AuthController)
- POST /auth/signup
  - Auth: No
  - Request: SignUpRequest: { email, username, password, phone, balance }
  - Response: ApiResponse<UserDto>
  - Errors: 400 invalid data, 409 if email/username already exists

- POST /auth/login
  - Auth: No
  - Request: LoginRequest: { email, password }
  - Response: ApiResponse with `data` map: { accessToken, user }
  - Side effect: sets `refresh_token` cookie (HttpOnly)
  - Errors: 401 invalid credentials

- POST /auth/refresh
  - Auth: No (requires `refresh_token` cookie)
  - Request: empty body; cookie must be sent
  - Response: 200 { accessToken: "<newAccessToken>" }
  - Errors: 403 invalid/expired refresh token

- POST /auth/logout
  - Auth: No
  - Response: 200 and clears the refresh cookie

3) Profile (ProfileController)
- GET /profile/me
  - Auth: Yes
  - Response: ApiResponse<UserDto>

- PUT /profile/me
  - Auth: Yes
  - Request: UserDto
  - Response: ApiResponse<UserDto>
  - Errors: 400 validation errors

- GET /profile/balance
  - Auth: Yes
  - Response: ApiResponse<Double>

- GET /profile/holdings
  - Auth: Yes
  - Response: ApiResponse<List<ShareDto>>

4) Dashboard (DashboardController)
- GET /transactions
  - Auth: Yes
  - Response: ApiResponse<List<TransactionDto>>

- GET /dashboard
  - Auth: Yes
  - Response: ApiResponse<DashboardDto> (holdings / aggregated view)

5) Price Fetching (PriceFetchController)
- GET /stockprice?symbol=SYMBOL
  - Auth: Yes
  - Response: ApiResponse<Double>
  - Errors: 400 missing symbol, 404 symbol unavailable

- GET /stockdailyprices?symbol=SYMBOL
  - Auth: Yes
  - Response: ApiResponse<List<StockPrice>>

- GET /stockchange?symbol=SYMBOL
  - Auth: Yes
  - Response: ApiResponse<StockChange>

- POST /jobs/updateprices
  - Auth: No (allowed publicly for scheduled job triggers)
  - Response: ApiResponse<String>

6) Execution (ExecutionController)
- POST /execute/buy
  - Auth: Yes
  - Request: ExecuteRequest { symbol, quantity, price, type }
  - Response: ApiResponse<TransactionDto>
  - Errors: 400 invalid request, 402 insufficient funds (domain-specific), 404 stock not found

- POST /execute/sell
  - Auth: Yes
  - Request: ExecuteRequest
  - Response: ApiResponse<TransactionDto>
  - Errors: 400 invalid request, 409 attempt to sell more than holding

7) Chat (ChatController) — updated

- POST /general-chat
  - Auth: Yes
  - Request body: JSON { "query": "..." }
  - Response: String (assistant response text)
  - Behavior:
    - The server persists the user message with the authenticated `userId`.
    - If `gemini.api.key` is configured, the service will call Gemini LLM with a payload such as { "query": "...", "userId": <id> } and return the LLM's text response.
    - Otherwise, the request is forwarded to `agents_endpoint + "general-agent"` with the same payload; the user's JWT is forwarded to the agent in the Authorization header so the agent can authorize/recognize the user.
    - The returned `data` string from agent/LLM is stored as `agentResponse` and returned. If the agent/LLM is unreachable, a friendly fallback string is returned.
  - Errors: 400 missing query, 401 unauthorized token, 503 agent unavailable -> fallback message

- POST /ea-chat
  - Auth: Yes
  - Request: { "query": "..." }
  - Behavior: forwarded to execution-assistant endpoint on agents service
  - Response: String

- POST /mra-chat
  - Auth: Yes
  - Request: { "query": "..." }
  - Behavior: forwarded to market-research-assistant endpoint on agents service
  - Response: String

- GET /chats
  - Auth: Yes
  - Response: ApiResponse<List<ChatDto>> (chat history for the authenticated user)

Notes about Chat DTOs and backward compatibility:
- Clients must send `{ "query": "..." }` in the request body for chat endpoints; the server maps it into the internal DTO and persists `userQuery` and `agentResponse` fields in DB.

Fallback message (human-friendly):
- If no agent/LLM is available, the server will respond with a helpful fallback such as: `The chat assistant is currently being improved; please try again later.` (see code for exact text).

---

## Chat service: Gemini LLM integration details

When `gemini.api.key` (and optionally `gemini.endpoint`) are configured, the `ChatService` will POST to the Gemini endpoint with a JSON body similar to:

```
{
  "query": "<user query>",
  "userId": <userId>
}
```

The request should include the `Authorization: Bearer <GEMINI_KEY>` header or another authentication mechanism as required by your Gemini provider.

The chat service expects the LLM to return JSON with a `data` field containing the assistant's response text. That text is saved in the `chats` table and returned to the caller as a plain `String` body.

Security note: Do not commit `gemini.api.key` to source control. Provide via environment variables or a secure secret store.

---

## Observability: Prometheus & Grafana (metrics)

This project includes Micrometer + Spring Boot Actuator support to expose runtime metrics that Prometheus can scrape. Typical setup steps:

1) Enable prometheus endpoint in `application.properties` (see Configuration section):
```
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
```

2) Run Prometheus with a minimal config that scrapes the application (example `prometheus.yml`):

```
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: 'maf'
    static_configs:
      - targets: ['host.docker.internal:8080'] # change host/port as appropriate
```

3) Run Grafana and add Prometheus as a data source. Use prebuilt dashboards or create panels showing:
- JVM memory, GC pauses
- CPU usage (container/host)
- HTTP request count, errors (4xx, 5xx), latency (p50, p95, p99)
- Custom app metrics (e.g., chat request counts, agent latency)

Docker example (Windows cmd.exe):

```
REM Start Prometheus (uses default prometheus.yml on cwd)
docker run -p 9090:9090 -v %cd%\prometheus.yml:/etc/prometheus/prometheus.yml --name prometheus -d prom/prometheus

REM Start Grafana
docker run -d -p 3000:3000 --name grafana grafana/grafana
```

Prometheus will scrape `/actuator/prometheus` if the JVM app is reachable by the Prometheus container or host.

Suggested metrics to track during performance testing:
- request rate (requests/sec) globally and per-endpoint
- latency percentiles (p50, p95, p99)
- error rate (4xx, 5xx)
- JVM memory and GC metrics
- DB connection pool usage
- Redis hit rate and latency (if used)

---

## Performance testing & `performance_data.xlsx`

The repository includes `performance_data.xlsx` at the project root. It contains previous benchmark results and per-endpoint latency/throughput breakdowns. Use it as a baseline for comparison.

How to reproduce tests and update the spreadsheet:

1) Prepare environment: run the application, Redis (if used), and ensure Prometheus is scraping metrics.
2) Use a load test tool such as JMeter (GUI or non-GUI), `wrk`, or `hey` to generate load.
3) Capture results (latency percentiles, throughput, error counts) and paste them into `performance_data.xlsx` sheets.

JMeter quick-start (recommended when you need more realistic load tests):
- Download JMeter and open the GUI.
- Create a Thread Group:
  - Number of Threads (users): e.g., 100
  - Ramp-Up: e.g., 60
  - Loop Count: e.g., 100 (or forever with duration)
- Add an HTTP Request sampler for your endpoint (e.g., POST /general-chat).
  - Set `Content-Type: application/json`
  - Add an HTTP Header Manager with `Authorization: Bearer <token>`.
  - Use a JSON body: { "query": "load test message" }
- Add Listeners (View Results in Table, Summary Report, Aggregate Report) and save results to CSV.
- For non-GUI execution (recommended for large tests):

```
REM Run JMeter non-GUI on Windows cmd.exe
jmeter -n -t test-plan.jmx -l results.jtl -e -o report-folder
```

Interpreting results:
- Focus on p95/p99 latencies and error rate. Use Prometheus + Grafana dashboards to correlate CPU/memory spikes with latency.
- Update `performance_data.xlsx` with the captured numbers for traceability.

---

## Running the app (Windows cmd.exe)

Prereqs:
- Java 17+
- Maven (or use the included `mvnw.cmd`)
- Docker (recommended for Redis/Prometheus/Grafana)

1) Start Redis (recommended using Docker):

```
REM Start a Redis container
docker run -p 6379:6379 --name redis -d redis:7
```

If you use WSL2 you can `sudo apt install redis-server` inside WSL and start with `sudo service redis-server start`.

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

4) (Optional) Start Prometheus & Grafana via Docker (see Observability section).

5) Visit health and metrics endpoints:
- Health: http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8080/actuator/prometheus

---

## Example requests (curl / Postman)

Login and receive tokens (public):

```
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"user@example.com","password":"pass"}'
```

Call a protected endpoint (Windows cmd.exe PowerShell/curl example) after receiving an access token:

```
curl -X POST http://localhost:8080/general-chat -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"query\":\"Hello assistant\"}"
```

If token is missing or expired you'll receive 401 Unauthorized. If refresh token is expired, `POST /auth/refresh` will return 403.

---

## Troubleshooting

- Authentication failing: ensure `jwt.secret` is set and the token is signed using that secret. If tokens are rejected, check system time and token expiry.
- Chat requests failing silently: enable logs at `DEBUG` for `majorproject.maf.service.ChatService` and `controller.ChatController` to see outgoing HTTP requests and responses.
- Prometheus not scraping: check target address in `prometheus.yml` and firewall/networking between Prometheus and the app host.
- Redis connection: test with `redis-cli -h <host> -p <port> ping` (returns `PONG`).
- If agent forwarding fails (503), the fallback response will be returned and an error logged; ensure `agents_endpoint` is correct.

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
- The workbook contains sheets summarizing previous benchmark runs: latency percentiles, throughput, error counts per endpoint, and resource usage (CPU, memory) across different concurrency levels.
- Use it as a baseline and paste new results from JMeter or other tools into the sheets for comparisons.
- Recommended workflow:
  1. Start the application and prerequisite services (Redis/DB/Prometheus).
  2. Run a JMeter test or `wrk` scenario against a chosen endpoint.
  3. Export JMeter summary to CSV and copy the metrics (throughput, avg latency, p95, p99, error %) into the workbook.
  4. Correlate with Prometheus metrics (JVM/GC/CPU) in Grafana dashboards.

---

If you'd like, I can also:
- Add an example `prometheus.yml` and a minimal Grafana dashboard JSON to the repository.
- Create a sample JMeter test plan (`.jmx`) for the `/general-chat` endpoint and add a short script to run non-GUI tests and export results to CSV.

If you want me to make any of those additions, tell me which one and I'll add it to the repo now.

---

Last updated: 2026-01-12
