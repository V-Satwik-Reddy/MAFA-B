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
- New transaction and stock daily price filters: endpoints support optional query parameters for date range (`startDate`/`endDate`), `beforeDate`, and `afterDate` — any combination is allowed and all are optional.
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

## Endpoints In-Depth Documentation

Important: Only authentication endpoints are public. All other endpoints require a valid JWT access token passed in the header:
`Authorization: Bearer <token>`

### Authentication (AuthController) — Public

- **`POST /auth/signup`**
  - **Description:** Creates a new user account.
  - **Expects (JSON):**
    ```json
    {
      "email": "user@example.com",
      "password": "securePassword",
      "firstName": "John",
      "lastName": "Doe"
    }
    ```
  - **Returns (201 Created):**
    ```json
    {
      "success": true,
      "message": "User signed up successfully",
      "data": {
        "id": 1,
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe"
      }
    }
    ```

- **`POST /auth/login`**
  - **Description:** Authenticates a user and issues tokens. Sets a `refresh_token` HttpOnly cookie.
  - **Expects (JSON):**
    ```json
    {
      "email": "user@example.com",
      "password": "securePassword"
    }
    ```
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Login successful",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
        "user": {
          "id": 1,
          "email": "user@example.com"
        }
      }
    }
    ```

- **`POST /auth/refresh`**
  - **Description:** Exchanges a valid refresh cookie for a fresh access token.
  - **Expects:** A valid `refresh_token` HttpOnly cookie attached to the request.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Token refreshed successfully",
      "data": {
        "accessToken": "eyJhbGciOiJIUzI1...new"
      }
    }
    ```

- **`POST /auth/logout`**
  - **Description:** Revokes the refresh cookie to log out the user.
  - **Expects:** A valid `refresh_token` HttpOnly cookie.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Logged out successfully",
      "data": null
    }
    ```

### Profile (ProfileController) — Auth: Yes

- **`GET /profile/me`**
  - **Description:** Retrieves the authenticated user's profile details.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Profile retrieved",
      "data": {
        "id": 1,
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe"
      }
    }
    ```

- **`PUT /profile/me`**
  - **Description:** Updates the user's profile information.
  - **Expects (JSON):**
    ```json
    {
      "firstName": "Johnny",
      "lastName": "Doe"
    }
    ```
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Profile updated",
      "data": {
        "id": 1,
        "email": "user@example.com",
        "firstName": "Johnny",
        "lastName": "Doe"
      }
    }
    ```

- **`GET /profile/balance`**
  - **Description:** Retrieves the user's available cash balance.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Balance retrieved",
      "data": {
        "balance": 15000.50
      }
    }
    ```

- **`GET /profile/holdings`**
  - **Description:** Retrieves the user's current asset holdings (portfolio).
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Holdings retrieved",
      "data": [
        {
          "symbol": "AAPL",
          "quantity": 15,
          "averagePrice": 145.20
        }
      ]
    }
    ```

### Alerts (AlertController) — Auth: Yes

- **`GET /alert`**
  - **Description:** Gets all active price alerts for the user.
  - **Returns (200 OK):**
    ```json
    [
      {
        "id": 10,
        "symbol": "AAPL",
        "targetPrice": 160.00,
        "condition": "ABOVE",
        "active": true
      }
    ]
    ```

- **`POST /alert`**
  - **Description:** Creates a new price alert.
  - **Expects (JSON):**
    ```json
    {
      "symbol": "TSLA",
      "targetPrice": 200.00,
      "condition": "BELOW"
    }
    ```
  - **Returns (201 Created):**
    ```json
    {
      "id": 11,
      "symbol": "TSLA",
      "targetPrice": 200.00,
      "condition": "BELOW",
      "active": true
    }
    ```

- **`PUT /alert/{id}`**
  - **Description:** Updates an existing active price alert.
  - **Expects (JSON):**
    ```json
    {
      "targetPrice": 190.00,
      "condition": "BELOW",
      "active": true
    }
    ```
  - **Returns (200 OK):**
    ```json
    {
      "id": 11,
      "symbol": "TSLA",
      "targetPrice": 190.00,
      "condition": "BELOW",
      "active": true
    }
    ```

- **`DELETE /alert/{id}`**
  - **Description:** Deletes a specific alert by ID.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Alert deleted successfully"
    }
    ```

### Trading Strategies (StrategyController) — Auth: Yes

- **`GET /strategy`**
  - **Description:** Returns the user's active trading strategy details.
  - **Returns (200 OK):**
    ```json
    {
      "id": 5,
      "name": "Moving Average Crossover",
      "parameters": {
        "shortWindow": 10,
        "longWindow": 50
      },
      "active": true
    }
    ```

- **`GET /strategy/history`**
  - **Description:** Returns a history of past strategy executions and performance data.
  - **Returns (200 OK):**
    ```json
    [
      {
        "executionId": 1001,
        "strategyName": "Moving Average Crossover",
        "executedAt": "2026-04-20T10:30:00Z",
        "pnl": 120.50
      }
    ]
    ```

- **`POST /strategy`**
  - **Description:** Saves a new trading strategy configuration.
  - **Expects (JSON):**
    ```json
    {
      "name": "Momentum",
      "parameters": {
        "rsiThreshold": 70
      }
    }
    ```
  - **Returns (201 Created):**
    ```json
    {
      "id": 6,
      "name": "Momentum",
      "parameters": {
        "rsiThreshold": 70
      },
      "active": true
    }
    ```

- **`PUT /strategy/{id}`**
  - **Description:** Updates the parameters of an existing trading strategy.
  - **Expects (JSON):**
    ```json
    {
      "parameters": {
        "rsiThreshold": 75
      },
      "active": false
    }
    ```
  - **Returns (200 OK):**
    ```json
    {
      "id": 6,
      "name": "Momentum",
      "parameters": {
        "rsiThreshold": 75
      },
      "active": false
    }
    ```

### Portfolio Analytics (PortfolioController) — Auth: Yes

- **`GET /portfolio`**
  - **Description:** Returns high-level portfolio metrics.
  - **Returns (200 OK):**
    ```json
    {
      "totalValue": 250000.00,
      "totalInvested": 200000.00,
      "overallProfit": 50000.00,
      "percentageChange": 25.0
    }
    ```

### Master Data (MasterDataController) — Auth: Yes

- **`GET /master-data`**
  - **Description:** Fetches application metadata (like supported stocks) for frontend usage.
  - **Returns (200 OK):**
    ```json
    {
      "supportedStocks": ["AAPL", "GOOGL", "MSFT", "TSLA"],
      "tradingHours": "09:30-16:00 EST"
    }
    ```

### Dashboard & Transactions (DashboardController) — Auth: Yes

- **`GET /transactions`**
  - **Description:** Lists user transactions. Supports optional date filters.
  - **Query Params (Optional):** `startDate`, `endDate`, `beforeDate`, `afterDate` (Format: `YYYY-MM-DD`).
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "data": [
        {
          "id": 101,
          "symbol": "AAPL",
          "type": "BUY",
          "quantity": 5,
          "price": 150.00,
          "date": "2026-04-10T14:20:00Z"
        }
      ]
    }
    ```

- **`GET /dashboard`**
  - **Description:** Returns aggregated overview data for the user dashboard.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "data": {
        "userBalance": 10500.00,
        "recentActivity": [],
        "portfolioHealth": "GOOD"
      }
    }
    ```

### Market Data (PriceFetchController) — Auth: Yes

- **`GET /stockprice?symbol={SYMBOL}`**
  - **Description:** Fetches the real-time or latest available price for a stock.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "data": {
        "symbol": "AAPL",
        "currentPrice": 155.00,
        "timestamp": "2026-04-25T10:00:00Z"
      }
    }
    ```

- **`GET /stockdailyprices?symbol={SYMBOL}`**
  - **Description:** Fetches historical daily prices. Supports optional date filters.
  - **Query Params:** `symbol` (required). Optional: `startDate`, `endDate`, `beforeDate`, `afterDate` (Format: `YYYY-MM-DD`).
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "data": [
        {
          "date": "2026-04-24",
          "closePrice": 154.50,
          "volume": 1200000
        }
      ]
    }
    ```

- **`GET /stockchange?symbol={SYMBOL}`**
  - **Description:** Returns recent price variations for the specified stock.
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "data": {
        "symbol": "AAPL",
        "absoluteChange": 2.50,
        "percentChange": 1.63
      }
    }
    ```

### Trade Execution (ExecutionController) — Auth: Yes

- **`POST /execute/buy`**
  - **Description:** Executes a buy order, deducting balance and adding to holdings.
  - **Expects (JSON):**
    ```json
    {
      "symbol": "AAPL",
      "quantity": 10,
      "orderType": "MARKET"
    }
    ```
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Buy order executed successfully",
      "data": {
        "transactionId": 102,
        "symbol": "AAPL",
        "quantity": 10,
        "price": 155.00
      }
    }
    ```

- **`POST /execute/sell`**
  - **Description:** Executes a sell order, adding to balance and reducing holdings.
  - **Expects (JSON):**
    ```json
    {
      "symbol": "AAPL",
      "quantity": 5,
      "orderType": "MARKET"
    }
    ```
  - **Returns (200 OK):**
    ```json
    {
      "success": true,
      "message": "Sell order executed successfully",
      "data": {
        "transactionId": 103,
        "symbol": "AAPL",
        "quantity": 5,
        "price": 155.50
      }
    }
    ```

### Chat & LLM (ChatController) — Auth: Yes

- **`POST /general-chat`, `POST /ea-chat`, `POST /mra-chat`**
  - **Description:** Send queries to the multi-agent chat system (or Gemini LLM).
  - **Expects (JSON):**
    ```json
    {
      "query": "Should I buy more AAPL stock?"
    }
    ```
  - **Returns (200 OK - plain text):**
    ```text
    "Based on current market conditions and your portfolio, increasing your AAPL position could be beneficial but watch out for upcoming earnings reports."
    ```

- **`GET /chats`**
  - **Description:** Retrieves the chat history corresponding to the authenticated user.
  - **Returns (200 OK):**
    ```json
    [
      {
        "id": 50,
        "userId": 1,
        "query": "Should I buy more AAPL stock?",
        "response": "Based on current market conditions...",
        "timestamp": "2026-04-25T11:00:00Z"
      }
    ]
    ```

Errors and conventions:

- Most endpoints return `ApiResponse<T>` with `success`, `message`, and `data` except chat POST endpoints that return plain `String`.
- Standard HTTP codes used: 400 (Bad Request), 401 (Unauthorized), 403 (Forbidden), 404 (Not Found), 409 (Conflict), 500 (Internal Server Error).

---

## New date filters: transactions and stock daily prices

Both the transactions list and stock daily prices endpoints now accept three optional query params that can be used together or separately:

- `startDate` and `endDate` — define an inclusive date range. Use ISO-8601 date format: `YYYY-MM-DD`.
- `beforeDate` — returns records strictly before this date (exclusive). Format: `YYYY-MM-DD`.
- `afterDate` — returns records strictly after this date (exclusive). Format: `YYYY-MM-DD`.

Behavior notes:
- All parameters are optional. If none provided, the endpoint returns the default (usually recent or paginated) results for the authenticated user.
- If both `startDate`/`endDate` and `beforeDate`/`afterDate` are present, they are combined logically (the repository/service layer applies filters cumulatively).
- Examples:
  - Date range: `GET /transactions?startDate=2026-01-01&endDate=2026-01-31`
  - Before a date: `GET /transactions?beforeDate=2026-01-01`
  - After a date: `GET /transactions?afterDate=2026-02-01`
  - Combined: `GET /stockdailyprices?symbol=AAPL&afterDate=2026-01-01&beforeDate=2026-03-01`

Make sure to pass the access token header for these endpoints:

```
Authorization: Bearer <accessToken>
```

---

## Chat subsystem & Gemini LLM integration

Behavior summary:

- Request shape: All chat POST endpoints expect a compact JSON body: `{ "query": "..." }`.
- The authenticated user is inferred from the access token; the service saves the incoming user message in the chat history table with the `userId` and timestamp.
- `general-chat` behavior:
  - If `gemini.api.key` is present in configuration, ChatService will POST to the Gemini endpoint with a JSON body containing the user query and the `userId` (example body: `{ "query": "...", "userId": 123 }`) and an authorization header using the configured key. The exact header format depends on the Gemini provider; the default implementation uses `Authorization: Bearer <GEMINI_KEY>`.
  - The service expects Gemini's response to include assistant text; the helper extracts that text and returns it as a plain `String` to the caller and persists it in chat history.
  - If Gemini is not configured, ChatService forwards the request to `agents_endpoint + "general-agent"` and forwards the user's JWT so the agent can authorize/recognize the user.
  - If the agent/LLM is unreachable or returns an error, the service responds with a friendly fallback message such as: `Our chat assistant is under active development and temporarily unavailable — please try again shortly.`

Security and persistence:

- Chats are stored per `userId`, enabling the service to reference a user's prior messages for future context.
- The service only returns the assistant message as a String to simplify client usage; clients wanting more structure can retrieve chat history via `GET /chats`.

Example request (Windows cmd.exe curl style):

```
curl -X POST http://localhost:8080/general-chat -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"query\":\"Hello assistant\"}"
```

Example improved generic fallback response used by the application when LLM/agent is unavailable:

```
"Thanks for your message — our chat assistant is being actively improved and is currently in a limited-access phase. We appreciate your patience; please try again in a few moments or use the dashboard actions for critical requests."
```

Notes on Gemini integration:

- The code expects Gemini to return JSON with a field containing assistant text. Adjust parsing logic in `ChatService` if your Gemini endpoint returns a different schema.
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

Transactions with date filters examples:

```
REM Date range
curl -X GET "http://localhost:8080/transactions?startDate=2026-01-01&endDate=2026-01-31" -H "Authorization: Bearer <token>"

REM Before date
curl -X GET "http://localhost:8080/transactions?beforeDate=2026-01-01" -H "Authorization: Bearer <token>"

REM After date
curl -X GET "http://localhost:8080/transactions?afterDate=2026-02-01" -H "Authorization: Bearer <token>"
```

Stock daily prices with filters:

```
curl -X GET "http://localhost:8080/stockdailyprices?symbol=AAPL&startDate=2026-01-01&endDate=2026-01-31" -H "Authorization: Bearer <token>"
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

Last updated: 2026-04-24
