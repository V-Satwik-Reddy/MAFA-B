# Multi Agent Financial Assistant (MAF)

A small Spring Boot backend that provides account management, portfolio/dashboard data, price fetching and a multi-agent chat integration for general, execution and market research assistants.

This README documents what the project does, configuration keys, how authentication works, and a complete list of HTTP endpoints (method, path, example request/response shape) and whether they require authentication.

---

## Table of contents

- Project overview
- Key features
- Configuration
- Authentication & tokens
- Endpoints (detailed)
- Database & persistence (notes)
- Running the app

---

## Project overview

MAF (Multi Agent Financial Assistant) is a backend service offering basic user authentication (JWT access + refresh cookie), portfolio and transaction data, stock price fetching, trade execution endpoints, and a small chat subsystem that forwards queries to agent services. Chats are persisted per user.

The project uses Spring Boot, Spring Security, JPA (Hibernate), and Java 17+ HTTP client for outward requests to agent services.

## Key features

- Signup / Login with JWT access tokens and refresh tokens (refresh token is stored in an HttpOnly cookie).
- Profile endpoints for fetching/updating user info and holdings.
- Dashboard endpoints returning user transactions and holdings.
- Stock price fetching and daily prices.
- Execution endpoints for buying/selling that create transactions and update holdings.
- Multi-agent chat endpoints that forward user queries to agent services and persist chat history.

## Configuration

Important properties (set in `application.properties` or environment variables):

- `jwt.secret` (required) - Base64-encoded secret for signing JWTs.
- `allowed_origins` - Comma-separated list of allowed origins for CORS.
- `agents_endpoint` - Base URL for agent services that will receive forwarded chat queries. Example: `http://localhost:8081/agents/` (ChatService appends endpoints like `general-agent`).

Optional (future/LLM integrations):

- `gemini.api.key` - (optional) API key for Gemini LLM if you add direct LLM calls inside chat service.
- `gemini.endpoint` - (optional) Gemini endpoint URL.

Notes:
- `jwt.secret` must be a base64 encoded key long enough for HS256 (at least 256 bits recommended).
- `agents_endpoint` should include trailing slash if service expects it (
  ChatService uses `url + agentEndpoint`).

## Authentication & Tokens

- Login (`POST /auth/login`) returns an `ApiResponse` whose `data` contains an `accessToken` and a `user` object. Use the `accessToken` in the `Authorization` header for authenticated requests:

  Authorization: Bearer <accessToken>

- A refresh token is set as an HttpOnly cookie named `refresh_token` when signing in. To obtain a new access token, POST to `/auth/refresh` (the cookie must be sent by the client).

- Security rules (from `SecurityConfig`):
  - Public (no auth required): `/`, `/auth/**`, `OPTIONS /**`, and paths under `/jobs/**`.
  - All other endpoints require a valid access token.

Response envelope used across many endpoints: `ApiResponse<T>` which usually contains `success`, `message`, and `data` fields (and an unused `token` field in the DTO definition).


## Endpoints (detailed)

Below is a list of controllers and endpoints discovered in code. For each endpoint: HTTP method, path, whether authentication is required, example request body, and example response.

1) Home
- GET /
  - Auth: No
  - Purpose: Health / welcome message
  - Response: ApiResponse<String>
    - Example: { success: true, message: "Hello", data: "Welcome to the Multi Agent Financial Assistant" }

2) Authentication (AuthController)
- POST /auth/signup
  - Auth: No
  - Request: SignUpRequest JSON (email, username, password, phone, balance)
  - Response: ApiResponse (contains created user details in `data`)

- POST /auth/login
  - Auth: No
  - Request: LoginRequest JSON (email, password)
  - Response: ApiResponse with `data` containing a map: { "accessToken": "...", "user": { username, email, phone, balance } }
  - Side-effects: sets `refresh_token` cookie (HttpOnly)

- POST /auth/refresh
  - Auth: No (but requires `refresh_token` cookie set)
  - Request: empty body, cookie `refresh_token` sent automatically by client
  - Response: 200 OK with JSON { "accessToken": "<newAccessToken>" } or 403 if invalid

- POST /auth/logout
  - Auth: No
  - Request: none
  - Response: 200 OK and clears `refresh_token` cookie (sets maxAge=0)

3) Profile (ProfileController)
- GET /profile/me
  - Auth: Yes
  - Request: none
  - Response: ApiResponse<UserDto> (user profile)

- PUT /profile/me
  - Auth: Yes
  - Request: UserDto JSON to update profile
  - Response: ApiResponse<UserDto> (updated user)

- GET /profile/balance
  - Auth: Yes
  - Response: ApiResponse<Double> (current balance)

- GET /profile/holdings
  - Auth: Yes
  - Response: ApiResponse<List<Share>> (user holdings)

4) Dashboard (DashboardController)
- GET /transactions
  - Auth: Yes
  - Response: List<TransactionDto> (user transaction history)

- GET /dashboard
  - Auth: Yes
  - Response: List<StockDto> (user holdings details for dashboard)

5) Price Fetching (PriceFetchController)
- GET /stockprice?symbol=SYMBOL
  - Auth: Yes
  - Response: Double (current stock price)

- GET /stockdailyprices?symbol=SYMBOL
  - Auth: Yes
  - Response: ApiResponse<List<StockPrice>> (historical daily prices)

- GET /stockchange?symbol=SYMBOL
  - Auth: Yes
  - Response: StockChange JSON (percentage/absolute change)

- POST /jobs/updateprices
  - Auth: No (explicitly permitted)
  - Purpose: Trigger job to add previous day prices; returns a simple message

6) Execution (ExecutionController)
- POST /execute/buy
  - Auth: Yes
  - Request: ExecuteRequest JSON (symbol, quantity, price, type, etc.)
  - Response: TransactionDto (transaction details)

- POST /execute/sell
  - Auth: Yes
  - Request: ExecuteRequest JSON
  - Response: TransactionDto

7) Chat (ChatController)
- POST /general-chat
  - Auth: Yes
  - Request body: ChatDto JSON with shape { "userQuery": "...", "agentResponse": "..." } (only userQuery is required when sending a request)
    - Note: ChatDto currently uses field `userQuery`. If you want the request to be { "query": "..." }, update DTO and controller accordingly.
  - Response: String (the agent response text)
  - Behavior: forwards the query to an agent service (configured by `agents_endpoint`) along with the userId and the user's JWT forwarded as `Authorization: Bearer <token>`. The returned agent `data` field is saved to DB and returned.

- POST /ea-chat
  - Auth: Yes
  - Request: ChatDto (userQuery)
  - Response: String (agent response)

- POST /mra-chat
  - Auth: Yes
  - Request: ChatDto (userQuery)
  - Response: String (agent response)

- GET /chats
  - Auth: Yes
  - Response: List<ChatDto> (user chat history: userQuery, agentResponse)

Notes about Chat service behavior:
- The `ChatService` builds a JSON body containing `query` and `userId` and POSTs it to `agents_endpoint + agentEndpoint`. It forwards the user's JWT in the Authorization header when calling the agent service.
- Chat responses are expected to be JSON with a `data` field; that `data` is persisted as `agentResponse` and returned to the caller.

---

## Database & Persistence

- The project uses JPA entities found in `src/main/java/majorproject/maf/model` (User, Chat, Stock, StockPrice, Transaction).
- Chats are stored in the `chats` table with columns for user_id, userQuery (TEXT), agentResponse (TEXT) and created_at.

## Running the application

Requirements:
- Java 17+
- Maven

Run locally (Windows cmd.exe):

mvnw.cmd spring-boot:run

Or build and run JAR:

mvnw.cmd package
java -jar target/MAFA-0.0.1-SNAPSHOT.jar

## Notes & Troubleshooting

- Authentication: Make sure `jwt.secret` is configured as a base64 string. Missing or invalid keys will cause the application to fail during startup.
- CORS: Configure `allowed_origins` to match your frontend origin (comma-separated list).
- Agents: `agents_endpoint` needs to point to a running agent service that expects POST bodies with `{ "query": "...", "userId": <id> }` and returns a JSON object containing `data` which will be saved and returned.
- If you prefer the chat POST body to be `{ "query": "hello" }` instead of the current `ChatDto` with `userQuery` field, update `ChatDto` and `ChatController` accordingly.

---

If you want, I can:
- Update the chat endpoints to accept `{ "query": "..." }` and map it to `ChatDto` (or a new request DTO).
- Integrate a direct LLM call (Gemini) in `ChatService` and demonstrate configuration; I can add properties and a minimal implementation that uses `gemini.api.key`.




