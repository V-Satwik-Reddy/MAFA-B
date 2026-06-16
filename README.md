# MAFA-B: Multi-Agent Financial Assistant Backend

## 🎯 What is MAFA?

**MAFA (Multi-Agent Financial Assistant)** is an intelligent financial platform designed to democratize stock market investing for non-expert users. It combines AI agents, real-time market data, and natural language conversation to make investment decisions simple and accessible.

### Key Capabilities

- **Natural Language Interface**: Ask questions like "Should I buy AAPL?" or "What tech stocks are trending?" in plain English—no financial jargon required
- **AI-Powered Research**: Autonomous agents analyze market trends, news sentiment, and company fundamentals in seconds
- **Intelligent Trading**: Execute buy/sell orders with AI-recommended quantities and optimal timing suggestions
- **Risk Management**: Set price alerts, portfolio limits, and automated trading strategies based on your risk tolerance
- **Portfolio Analytics**: Real-time dashboard showing holdings, gains/losses, diversification, and performance metrics
- **Goal-Based Investing**: Define investment goals (e.g., "Save $50k for a house in 2 years") and receive personalized strategies
- **Chat History**: Maintain conversation context so the AI learns your preferences and investment style over time

**Who It's For**: Beginners, casual investors, and anyone who wants Wall Street-level analysis without years of training.

---

## 📚 What is MAFA-B?

**MAFA-B** is the **Spring Boot backend service** that powers the entire MAFA ecosystem. It's the central hub responsible for:

- **User Authentication & Accounts**: Secure signup/login, profile management, and session handling via JWT tokens
- **Data Persistence**: Stores user portfolios, transactions, chat history, alerts, and trading strategies in PostgreSQL
- **Market Data Gateway**: Fetches real-time stock prices and maintains a database of historical price data
- **Trade Execution Engine**: Validates buy/sell orders, updates balances and holdings atomically, and records transactions
- **Chat Orchestration**: Routes user questions to the appropriate AI agent (market research, portfolio analysis, trade execution) or directly to Google Gemini LLM
- **REST API**: Provides the single source of truth for all frontend requests and agent-to-backend communication
- **Observability**: Exposes metrics (via Prometheus) for monitoring performance, error rates, and system health

Without MAFA-B, the frontend and agents would have nowhere to persist data and no way to communicate. It's the glue that binds the entire system together.

---

## 🏗️ Project Overview

MAFA-B is a production-grade Spring Boot 3.x REST API built with enterprise-standard patterns:

### Core Responsibilities

1. **User Management**
   - Secure JWT-based authentication (signup, login, token refresh, logout)
   - User profile endpoints for name, email, and preferences
   - HttpOnly refresh token cookies to prevent XSS attacks

2. **Portfolio & Balance Tracking**
   - Real-time cash balance and equity holdings
   - Portfolio metrics (total value, invested amount, profit/loss, diversification)
   - Historical transaction records with date filtering and search

3. **Market Data Management**
   - Fetch and cache current stock prices
   - Store historical daily OHLCV (Open, High, Low, Close, Volume) data
   - Price change calculations (absolute and percentage)
   - Support for date range queries on historical data

4. **Trade Execution**
   - Atomic buy/sell order processing
   - Automatic balance deduction and holdings updates
   - Transaction logging for audit trails
   - Order validation (sufficient funds, valid symbols)

5. **Smart Alerts & Strategies**
   - Price alerts: Notify users when a stock crosses a target price (above/below)
   - Trading strategies: Save and manage rule-based trading patterns (e.g., "buy when moving average crosses")
   - Strategy performance history: Track P&L across strategy executions

6. **Multi-Agent Chat System**
   - Route user queries to specialized AI agents (market research, portfolio analysis, execution)
   - LLM fallback: If agents aren't available, send queries to Google Gemini for general financial advice
   - Chat history persistence per user for context-aware conversations
   - Integration with MAFA-agents Python system via REST API calls

7. **System Observability**
   - Prometheus metrics endpoint for request latency, throughput, and error rates
   - JVM memory and garbage collection metrics
   - Custom metrics for chat request counts and agent latency

---

## 🔗 Related Repositories & Data Flow

MAFA is a distributed multi-service architecture. MAFA-B is the central orchestrator:

| Component | Language | Role |
|-----------|----------|------|
| **[s0meshnalla/MAFA-agents](https://github.com/s0meshnalla/MAFA-agents)** | Python | Multi-agent orchestration system with MCP (Model Context Protocol) servers. Includes market research, portfolio analysis, trade execution, and strategy servers |
| **[s0meshnalla/MCP_Financial_analyst_frontend](https://github.com/s0meshnalla/MCP_Financial_analyst_frontend)** | JavaScript/React | Web UI for MAFA. Users interact here; frontend talks to MAFA-B via REST API |
| **MAFA-B (this repo)** | Java/Spring Boot | Central REST API backend. Routes requests, manages data, orchestrates agents |

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          Frontend (React + JavaScript)                           │
│                    (MCP_Financial_analyst_frontend)                              │
│                                                                                   │
│  User Actions: Login → Chat → Buy/Sell → View Portfolio → Set Alerts            │
└────────────────────────────────────┬────────────────────────────────────────────┘
                                      │
                                      │ HTTP/REST + JWT
                                      ↓
┌──────────────────────────────────────────────────────────────────────────────────┐
│                      MAFA-B (Spring Boot Backend)                                 │
│                                                                                    │
│  ┌──────────────────────┐      ┌──────────────────────┐    ┌─────────────────┐  │
│  │  REST API Layer      │      │  Authentication &    │    │  Chat Service   │  │
│  │                      │      │  User Management     │    │                 │  │
│  │  • /auth/*           │      │  (JWT, Sessions)     │    │  Routes queries │  │
│  │  • /portfolio        │      │                      │    │  to agents or   │  │
│  │  • /transactions     │      │                      │    │  Gemini LLM     │  │
│  │  • /execute/buy|sell │      │                      │    │                 │  │
│  │  • /general-chat     │      ���                      │    └────────┬────────┘  │
│  │  • /stockprice       │      └──────────────────────┘             │           │
│  │  • /alerts           │                                            │           │
│  └──────────┬───────────┘      ┌──────────────────────┐             │           │
│             │                  │  Data Access Layer   │             │           │
│             │                  │  (JPA/Hibernate)     │             │           │
│             │                  │                      │             │           │
│             │                  │  • User Repository   │             │           │
│             │                  │  • Transaction Repo  │             │           │
│             │                  │  • Holdings Repo     │             │           │
│             │                  │  • Alert Repository  │             │           │
│             │                  │  • Chat Repository   │             │           │
│             │                  │  • Stock Price Repo  │             │           │
│             │                  └──────────┬───────────┘             │           │
│             │                             │                        │           │
│             └─────────────────────────────┼────────────────────────┘           │
│                                           │                                    │
└───────────────────────────────────────────┼────────────────────────────────────┘
                                            │
                                            ↓
                           ┌─────────────────────────────────┐
                           │   PostgreSQL Database           │
                           │   (User, Holdings, Alerts,     │
                           │    Transactions, Chats)        │
                           └─────────────────────────────────┘


                           ┌─────────────────────────────────────────────────┐
                           │  MAFA-Agents (Python MCP System)                │
                           │                                                  │
                           │  Market Research Agent                          │
                           │  ├─→ Alpha Vantage API (Stock Fundamentals)    │
                           │  ├─→ NewsAPI (Market News & Sentiment)         │
                           │  ├─→ LSTM Models (Price Forecasting)           │
                           │  └─→ Fetches Stock Data from MAFA-B Backend   │
                           │                                                  │
                           │  Portfolio Analysis Agent                       │
                           │  ├─→ Risk Calculations                          │
                           │  ├─→ Diversification Analysis                   │
                           │  └─→ Uses Holdings Data from MAFA-B Backend    │
                           │                                                  │
                           │  Trade Execution Agent                          │
                           │  ├─→ Validates Orders                           │
                           │  ├─→ Recommends Quantities & Prices            │
                           │  └─→ Executes via MAFA-B Backend               │
                           │                                                  │
                           │  Strategy Agent                                 │
                           │  ├─→ Generates Trading Strategies              │
                           │  └─→ Stores/Updates via MAFA-B Backend         │
                           │                                                  │
                           │  MCP Orchestrator                               │
                           │  └─→ Coordinates All Agents                    │
                           │                                                  │
                           │  Event Bus (Redis Pub/Sub)                     │
                           │  └─→ Real-time Agent Communication             │
                           └─────────────────────────────────────────────────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
                    ↓                      ↓                      ↓
        ┌─────────────────────┐  ┌─────────────────────┐  ┌──────────────────┐
        │  External Services  │  │  Google Services    │  │  Data Storage    │
        │                     │  │                     │  │                  │
        │ • Alpha Vantage     │  │ • Google Gemini LLM │  │ • Supabase       │
        │   (Stock Data)      │  │   (LLM Responses)   │  │   (Vector DB)    │
        │                     │  │ • Google Custom     │  │                  │
        │ • NewsAPI           │  │   Search (News)     │  │ • Redis          │
        │   (Market News)     │  │                     │  │   (Cache/Events) │
        └─────────────────────┘  └─────────────────────┘  └──────────────────┘
```

### Data Flow Example: "Should I buy AAPL?"

1. **User** types in frontend chat: "Should I buy AAPL?"
2. **Frontend** sends POST to `MAFA-B:/general-chat` with JWT token
3. **MAFA-B ChatService** receives request, extracts user ID from token
4. **MAFA-B** checks if Gemini API key is configured:
   - **Yes**: Calls Google Gemini directly, returns response, saves to chat history
   - **No**: Forwards request to `MAFA-Agents:/general-agent` with user's JWT
5. **MAFA-Agents Market Research Agent** receives request:
   - Fetches AAPL fundamentals from Alpha Vantage API
   - Fetches latest news from NewsAPI
   - Runs LSTM model for price forecast
   - **Queries MAFA-B backend** for user's current AAPL holdings and balance
   - Generates personalized recommendation
6. **MAFA-B** receives agent response, saves to chat history in PostgreSQL
7. **Frontend** displays recommendation to user

### Data Storage: Stock Prices

- **Real-time prices**: Fetched from Alpha Vantage → stored in MAFA-B PostgreSQL (cache layer)
- **Historical prices**: Accumulated in MAFA-B over time → used by agents for LSTM training
- **MAFA-Agents** can query MAFA-B backend to fetch any stock's price data for analysis

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+** ([download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** (or use included `mvnw.cmd` / `mvnw`)
- **Docker** (for PostgreSQL, Redis, Prometheus, Grafana)
- **PostgreSQL 15+** (can run in Docker)
- **API Keys**:
  - Google Gemini API key (for LLM integration)
  - MAFA-agents base URL (e.g., `http://localhost:8081`)

### 1. Clone & Navigate

```bash
git clone https://github.com/V-Satwik-Reddy/MAFA-B.git
cd MAFA-B
```

### 2. Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Security
JWT_SECRET=your-base64-encoded-secret-key-here

# Database (PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mafa_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# CORS (for frontend)
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5000

# LLM Integration (optional, but recommended)
GEMINI_API_KEY=your_google_gemini_api_key

# Multi-Agent System (required for full functionality)
AGENTS_ENDPOINT=http://localhost:8081/

# Redis (optional, caching disabled by default)
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Observability
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus
```

Or add to `src/main/resources/application.properties`:

```properties
# Security
jwt.secret=${JWT_SECRET:fallback-secret}
allowed_origins=${ALLOWED_ORIGINS:http://localhost:3000}

# Database
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/mafa_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update

# LLM & Agents
gemini.api.key=${GEMINI_API_KEY:}
agents_endpoint=${AGENTS_ENDPOINT:http://localhost:8081/}

# Actuator
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
```

### 3. Start Docker Services (Required)

```bash
# PostgreSQL (required)
docker run -d \
  --name postgres \
  -e POSTGRES_DB=mafa_db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Redis (optional, for caching and event bus)
docker run -d --name redis -p 6379:6379 redis:7

# Prometheus (optional, for metrics)
docker run -d \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  --name prometheus \
  prom/prometheus

# Grafana (optional, for dashboards)
docker run -d \
  -p 3000:3000 \
  --name grafana \
  grafana/grafana
```

### 4. Install Dependencies

```bash
# Using Maven wrapper (Windows)
mvnw.cmd clean install

# Using Maven wrapper (Linux/Mac)
./mvnw clean install

# Or use system Maven
mvn clean install
```

### 5. Run the Application

```bash
# Using Maven wrapper (Windows)
mvnw.cmd spring-boot:run

# Using Maven wrapper (Linux/Mac)
./mvnw spring-boot:run

# Or build and run JAR
mvnw.cmd package
java -jar target/MAFA-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**

### 6. Verify It's Running

```bash
# Health check
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 📋 API Overview

### Public Endpoints (No Auth Required)

- `POST /auth/signup` — Create account
- `POST /auth/login` — Login and get JWT token
- `POST /auth/refresh` — Refresh access token
- `POST /auth/logout` — Logout

### Protected Endpoints (JWT Token Required)

All other endpoints require `Authorization: Bearer <accessToken>` header:

**Profile & Balance:**
- `GET /profile/me` — User details
- `GET /profile/balance` — Cash balance
- `GET /profile/holdings` — Portfolio holdings

**Market Data:**
- `GET /stockprice?symbol=AAPL` — Current stock price
- `GET /stockdailyprices?symbol=AAPL&startDate=2026-01-01&endDate=2026-01-31` — Historical prices
- `GET /stockchange?symbol=AAPL` — Price changes

**Trading:**
- `POST /execute/buy` — Buy stocks
- `POST /execute/sell` — Sell stocks
- `GET /transactions` — Transaction history with date filtering

**Portfolio:**
- `GET /portfolio` — Portfolio metrics (value, profit/loss, diversification)
- `GET /dashboard` — Dashboard overview data

**Alerts:**
- `GET /alert` — List all price alerts
- `POST /alert` — Create price alert
- `PUT /alert/{id}` — Update alert
- `DELETE /alert/{id}` — Delete alert

**Chat & AI:**
- `POST /general-chat` — Chat with general financial assistant
- `POST /ea-chat` — Execution assistant (trade-focused)
- `POST /mra-chat` — Market research assistant (research-focused)
- `GET /chats` — Get chat history for the user

**Strategy:**
- `GET /strategy` — Get user's active strategy
- `POST /strategy` — Create new strategy
- `PUT /strategy/{id}` — Update strategy
- `GET /strategy/history` — Strategy performance history

---

## 🔧 Configuration Details

### JWT Authentication

- **Secret**: Configure `jwt.secret` in properties (use a strong base64-encoded key for production)
- **Access Token**: Short-lived JWT (~15 minutes) in response body
- **Refresh Token**: Long-lived token (~7 days) stored as HttpOnly cookie
- **Flow**: Login → receive accessToken + set refresh_token cookie → use accessToken for all requests → call `/auth/refresh` when expired

### Database

- **Engine**: PostgreSQL 15+
- **Auto-Migrations**: Hibernate (set `spring.jpa.hibernate.ddl-auto=update` for development)
- **Core Tables**: User, Chat, Transaction, Holdings, Alert, Strategy, Stock, StockPrice
- **Indexes**: Optimized for fast lookups on (userId, timestamp) and (symbol, date)

### LLM Integration

**If `gemini.api.key` is configured:**
- `/general-chat` calls Google Gemini API directly
- Responses are immediately returned and persisted to chat history
- No dependency on MAFA-agents

**If `gemini.api.key` is NOT configured:**
- `/general-chat` forwards requests to `agents_endpoint/general-agent`
- The MAFA-agents system handles AI responses using its own models
- JWT token is forwarded for user context

### Multi-Agent Communication

- **Protocol**: HTTP REST + JWT token forwarding
- **Agents Supported**: General (market research), Execution, Market Research
- **Event Bus**: Redis Pub/Sub (used by MAFA-agents for inter-agent communication)
- **Timeout**: Configure connection timeout in `application.properties`

---

## 📊 Observability: Prometheus & Grafana

### Prometheus Setup

1. Create `prometheus.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'mafa-backend'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

2. Run Prometheus:

```bash
docker run -d \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  --name prometheus \
  prom/prometheus
```

3. Access: **http://localhost:9090**

### Grafana Setup

1. Run Grafana:

```bash
docker run -d -p 3000:3000 --name grafana grafana/grafana
```

2. Access: **http://localhost:3000** (default: admin/admin)
3. Add Prometheus as data source: `http://host.docker.internal:9090`
4. Create dashboards to monitor request latency, error rates, and JVM metrics

### Key Metrics

- `http_requests_seconds` — Request latency percentiles (p50/p95/p99)
- `http_requests_total` — Request count by endpoint
- `jvm_memory_used_bytes` — JVM memory consumption
- `jvm_gc_pause_seconds` — Garbage collection pause times

---

## 🛠️ Development

### Project Structure

```
src/main/java/majorproject/maf/
├── controller/        # REST endpoint handlers
│   ├── AuthController           (signup, login, refresh, logout)
│   ├── ChatController           (chat routing to agents/Gemini)
│   ├── ExecutionController      (buy/sell orders)
│   ├── PriceFetchController     (stock prices)
│   ├── ProfileController        (user info)
│   ├── PortfolioController      (portfolio metrics)
│   ├── DashboardController      (dashboard data)
│   ├── AlertController          (price alerts)
│   └── StrategyController       (trading strategies)
├── service/           # Business logic
│   ├── AuthService              (JWT, user auth)
│   ├── ChatService              (agent routing, LLM calls)
│   ├── ExecutionService         (trade logic)
│   ├── PriceFetchService        (market data)
│   ├── UserService              (profile management)
│   └── ...
├── model/             # JPA entities
│   ├── User           (user accounts)
│   ├── Chat           (chat history)
│   ├── Transaction    (buy/sell records)
│   ├── Holdings       (portfolio)
│   ├── Alert          (price alerts)
│   ├── Strategy       (trading strategies)
│   ├── Stock          (stock master data)
│   └── StockPrice     (historical prices)
├── repository/        # Data access layer
│   ├── UserRepository
│   ├── ChatRepository
│   ├── TransactionRepository
│   └── ...
├── exception/         # Custom exceptions
│   ├── AuthenticationException
│   ├── InsufficientFundsException
│   └── ...
├── config/            # Spring configuration
│   ├── SecurityConfig           (JWT, CORS)
│   ├── WebConfig                (beans, interceptors)
│   └── ...
└── util/              # Utility classes
```

### Building

```bash
# Build JAR
mvnw.cmd package

# Run JAR
java -jar target/MAFA-0.0.1-SNAPSHOT.jar

# Run with custom properties
java -Djwt.secret=my-secret -jar target/MAFA-0.0.1-SNAPSHOT.jar
```

---

## 🔐 Security

- **JWT Signing**: HS256 with `jwt.secret` environment variable
- **CORS**: Configurable via `allowed_origins` (e.g., frontend URL)
- **HTTPS**: Configure in production (enable Spring Security SSL)
- **Secrets Management**: Use environment variables, never hardcode sensitive data
- **OWASP**: Standard Spring Security defaults applied (CSRF protection, XSS prevention, etc.)
- **Refresh Token**: HttpOnly cookie prevents JavaScript access (XSS-safe)

---

## 📌 Environment Variables Summary

| Variable | Required | Default | Description |
|---|---|---|---|
| `JWT_SECRET` | ✅ | None | JWT signing secret (base64-encoded) |
| `SPRING_DATASOURCE_URL` | ✅ | None | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | ✅ | None | Database username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | None | Database password |
| `ALLOWED_ORIGINS` | ❌ | http://localhost:3000 | CORS origins (comma-separated) |
| `GEMINI_API_KEY` | ❌ | None | Google Gemini API key (if using LLM directly) |
| `AGENTS_ENDPOINT` | ❌ | http://localhost:8081/ | MAFA-agents base URL |
| `SPRING_REDIS_HOST` | ❌ | localhost | Redis host (for event bus) |
| `SPRING_REDIS_PORT` | ❌ | 6379 | Redis port |

---

## 📖 Example Requests

### Signup

```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "securePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "securePass123"
  }'
```

Response includes `accessToken` and sets `refresh_token` cookie.

### Get Balance (Protected)

```bash
curl -X GET http://localhost:8080/profile/balance \
  -H "Authorization: Bearer <accessToken>"
```

### Buy Stock

```bash
curl -X POST http://localhost:8080/execute/buy \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "quantity": 5,
    "orderType": "MARKET"
  }'
```

### General Chat (AI Assistant)

```bash
curl -X POST http://localhost:8080/general-chat \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"query": "Should I buy TSLA? I have $2000."}'
```

Returns a natural language response from either Google Gemini or MAFA-agents.

---

## 🐛 Troubleshooting

| Issue | Solution |
|---|---|
| **401 Unauthorized** | JWT token expired. Call `/auth/refresh` with the refresh_token cookie to get a new one |
| **500 Internal Server Error** | Check server logs. Verify PostgreSQL is running and accessible |
| **Chat returning 503** | Verify `agents_endpoint` is reachable, or ensure `GEMINI_API_KEY` is set as a fallback |
| **Prometheus not scraping** | Check `prometheus.yml` target address and verify network connectivity to the backend |
| **Database connection fails** | Ensure PostgreSQL container is running, credentials are correct, and `SPRING_DATASOURCE_URL` is set |
| **Stock prices not updating** | Verify frontend or agents are calling `/stockprice` endpoint to fetch and cache new data |

---

## 📞 Support & Documentation

- **Chat Integration**: See `majorproject.maf.service.ChatService` for agent routing logic
- **Trade Execution**: See `majorproject.maf.service.ExecutionService` for order validation and processing
- **Database Schema**: Auto-generated from JPA entities in `majorproject.maf.model` package
- **Security Configuration**: See `majorproject.maf.config.SecurityConfig` for JWT and CORS setup

---

## 📄 License

[Add your license here]

---

## 🙋 Contributing

[Add contribution guidelines here]

---

**Last Updated**: 2026-06-16  
**Backend Version**: Spring Boot 3.x  
**Java Version**: 17+  
**Database**: PostgreSQL 15+
