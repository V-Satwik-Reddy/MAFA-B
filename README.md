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

```text
                        ┌─────────────────────────────────────────────────────────────────────────────────┐
                        │                          Frontend (React + JavaScript)                          │
                        │                    (MCP_Financial_analyst_frontend)                             │
                        └────────────────────────────────────┬────────────────────────────────────────────┘
                                                             │ HTTP/REST + JWT
                                                             ↓
                        ┌─────────────────────────────────────────────────────────────────────────────────┐
                        │                      MAFA-B (Spring Boot Backend)                               │
                        │                                                                                 │
                        │  ┌──────────────────────┐      ┌──────────────────────┐    ┌─────────────────┐  │
                        │  │  REST API Layer      │◄─────┤  Authentication &    │    │  Chat Service   │  │
                        │  │  • /auth, /portfolio,│      │  User Management     │    │  (Routes        │  │
                        │  │    /execute, /chats  │      └──────────────────────┘    │  queries)       │  │
                        │  └──────────┬───────────┘◄─────────────────────────────────┴────────┬────────┘  │
                        │             │      ▲                                                │           │
                        │             │      │ (Fetches Data via REST API)                    │           │
                        │             │      └─────────────────────────────────────────┐      │           │
                        │             ↓                                                │      │           │
                        │  ┌──────────────────────┐                                    │      │           │
                        │  │  Data Access Layer   │                                    │      │           │
                        │  │  (JPA/Hibernate)     │                                    │      │           │
                        │  └──────────┬───────────┘                                    │      │           │
                        │             │                                                │      │           │
                        └─────────────┼────────────────────────────────────────────────┼──────┼───────────┘
                                      │                                                │      │
                                      ↓                                                │      ↓
                             ┌─────────────────┐                       ┌───────────────┴──────┴──────────┐
                             │ MySQL Database  │                       │  MAFA-Agents (Python MCP)       │
                             │ (Users, Holdings│                       │                                 │
                             │  Chats, Alerts) │                       │  • Multi-Agent Orchestrator     │
                             └─────────────────┘                       │  • Event Bus (Redis)            │
                                                                       └───────────┬───────────┬─────────┘
                                                                                   │           │
                                                                                   ↓           ↓
                                                                      ┌──────────────┐   ┌───────────────┐
                                                                      │ Google / LLM │   │ Data Storage  │
                                                                      │ Services     │   │ (Vector DB)   │
                                                                      └──────────────┘   └───────────────┘
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

<!-- ### Data Storage: Stock Prices

- **Real-time prices**: Fetched from Alpha Vantage → stored in MAFA-B PostgreSQL (cache layer)
- **Historical prices**: Accumulated in MAFA-B over time → used by agents for LSTM training
- **MAFA-Agents** can query MAFA-B backend to fetch any stock's price data for analysis -->

---

## 🚀 Quick Start

### Prerequisites

| Requirement | Notes |
|---|---|
| **Java 17+** | [Download](https://www.oracle.com/java/technologies/downloads/) |
| **IntelliJ IDEA** (Recommended) | [Download](https://www.jetbrains.com/idea/download/). Makes dependency install and running the project a 2-button process |
| **MySQL 8+** | [Download MySQL Server](https://dev.mysql.com/downloads/mysql/). Create a database (e.g., `mafa_db`) |
| **Docker Desktop** | [Download](https://www.docker.com/products/docker-desktop/). Used to run the Redis container image — start/stop via the Docker Desktop UI, no command-line needed |
| **Redis Docker Image** | Pull via Docker Desktop or `docker pull redis:7`. Used for caching and event bus |
| **Prometheus** *(optional)* | [Download](https://prometheus.io/download/). Manually downloaded and configured for metrics scraping |
| **Grafana** *(optional)* | [Download](https://grafana.com/grafana/download). Manually downloaded and configured for dashboards |

### Setup Steps

#### 1. Clone & Open in IntelliJ

```bash
git clone https://github.com/V-Satwik-Reddy/MAFA-B.git
```

Open the cloned `MAFA-B` folder in **IntelliJ IDEA**.

#### 2. Configure Application Properties

Create a new file at `src/main/resources/application.properties` and copy the contents of [`application-prod.properties`](src/main/resources/application-prod.properties) into it.

Then fill in every value that has `${}` placeholders. Here's the full template with guidance:

```properties
spring.application.name=MAF

# ── Database (MySQL) ──────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/mafa_db          # ← your MySQL URL
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_mysql_password                     # ← your MySQL password

spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.minimum-idle=10
spring.jpa.properties.hibernate.jdbc.batch_size=100

spring.jpa.hibernate.ddl-auto=update                               # ← use "update" for development
management.endpoints.web.exposure.include=health
#spring.jpa.show-sql=true

# ── Security ──────────────────────────────────────────────────────
jwt.secret=your-base64-encoded-secret-key                          # ← any strong base64 string

# ── MAFA-Agents ───────────────────────────────────────────────────
agents_endpoint=http://localhost:5000/                              # ← URL where MAFA-Agents is running

# ── CORS ──────────────────────────────────────────────────────────
allowed_origins=http://localhost:3000,http://localhost:5000         # ← frontend & agents URLs

# ── Docker Compose ────────────────────────────────────────────────
spring.docker.compose.enabled=false

# ── Redis ─────────────────────────────────────────────────────────
spring.cache.type=redis
spring.data.redis.host=localhost                                   # ← Redis host
spring.data.redis.port=6379                                        # ← Redis port (check Docker Desktop)

# ── Email (SMTP via Gmail) ────────────────────────────────────────
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com                          # ← your Gmail address
spring.mail.password=your_app_password                             # ← Gmail App Password (not regular password)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

> **Note:** The `application.properties` file is gitignored — it stays on your local machine only. Never commit secrets to version control.

#### 3. Install Dependencies

In IntelliJ, open `pom.xml` → click the **Maven reload button** (🔄 icon in the top-right of the editor or in the Maven tool window) to download and install all dependencies.

#### 4. Start Required Services

1. **MySQL**: Ensure your MySQL server is running and the database (e.g., `mafa_db`) exists
2. **Redis**: Open **Docker Desktop** → start the Redis container image. If you haven't pulled it yet:
   ```bash
   docker pull redis:7
   docker run -d --name redis -p 6379:6379 redis:7
   ```
   After the first run, you can start/stop it directly from the Docker Desktop UI.

3. Verify the `spring.data.redis.port` and `spring.datasource.url` port in your `application.properties` match the actual running services.

#### 5. Run the Application

Click the **Run/Start** button (▶️) in IntelliJ IDEA — it will build and start the Spring Boot application.

#### 6. Verify

Open your browser and visit **http://localhost:8080/**. If the page loads without errors, the backend is running successfully.

---

### Quick Reference: Setup Flow

```
Clone repo → Open in IntelliJ → Copy application-prod.properties to application.properties
→ Fill ${} values → Maven reload (pom.xml) → Start Redis (Docker Desktop)
→ Confirm ports in properties → Click Run ▶️ → Visit http://localhost:8080/
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

## 🔧 Technical Details

### JWT Authentication
- **Signing**: HS256 with `jwt.secret` from `application.properties`
- **Access Token**: Short-lived JWT (~15 minutes) returned in response body
- **Refresh Token**: Long-lived token (~7 days) stored as HttpOnly cookie (XSS-safe)
- **Flow**: Login → receive `accessToken` + `refresh_token` cookie → use `accessToken` for all requests → call `/auth/refresh` when expired

### Database
- **Engine**: MySQL 8+
- **Driver**: `com.mysql.cj.jdbc.Driver`
- **Auto-Migrations**: Hibernate (set `spring.jpa.hibernate.ddl-auto=update` for development)
- **Core Tables**: User, Chat, Transaction, Holdings, Alert, Strategy, Stock, StockPrice
- **Connection Pool**: HikariCP (max 30 connections, min 10 idle)

### Multi-Agent Communication
- **Protocol**: HTTP REST + JWT token forwarding
- **Agents Endpoint**: Configured via `agents_endpoint` in `application.properties`
- **Supported Agents**: General (market research), Execution, Market Research
- **Event Bus**: Redis Pub/Sub (used by MAFA-Agents for inter-agent communication)
- **Data Flow**: Chat Service forwards queries to agents → Agents call back to MAFA-B REST API for user data, holdings, and trade execution

---

## 📊 Observability (Optional)

Prometheus and Grafana are **manually downloaded and configured** — they are not run via Docker.

### Prometheus Setup
1. [Download Prometheus](https://prometheus.io/download/) and extract it
2. Create/edit `prometheus.yml` in the Prometheus directory:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'mafa-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

3. Run Prometheus and access it at **http://localhost:9090**

### Grafana Setup
1. [Download Grafana](https://grafana.com/grafana/download) and install it
2. Access at **http://localhost:3000** (default login: admin/admin)
3. Add Prometheus as a data source: `http://localhost:9090`
4. Create dashboards to monitor request latency, error rates, and JVM metrics

### Key Metrics
- `http_requests_seconds` — Request latency percentiles (p50/p95/p99)
- `http_requests_total` — Request count by endpoint
- `jvm_memory_used_bytes` — JVM memory consumption
- `jvm_gc_pause_seconds` — Garbage collection pause times

---

## 🔐 Security
- **JWT Signing**: HS256 with `jwt.secret` property
- **CORS**: Configurable via `allowed_origins` (comma-separated frontend and agent URLs)
- **Refresh Token**: HttpOnly cookie prevents JavaScript access (XSS-safe)
- **HTTPS**: Configure in production (enable Spring Security SSL)
- **Secrets Management**: All secrets live in `application.properties` (gitignored) — never commit them

---

## 📁 Project Structure
```
src/main/java/majorproject/maf/
├── controller/        # REST endpoint handlers
│   ├── AuthController           (signup, login, refresh, logout)
│   ├── ChatController           (chat routing to agents)
│   ├── ExecutionController      (buy/sell orders)
│   ├── PriceFetchController     (stock prices)
│   ├── ProfileController        (user info)
│   ├── PortfolioController      (portfolio metrics)
│   ├── DashboardController      (dashboard data)
│   ├── AlertController          (price alerts)
│   └── StrategyController       (trading strategies)
├── service/           # Business logic
│   ├── AuthService              (JWT, user auth)
│   ├── ChatService              (agent routing)
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

---

## 💬 API Usage Examples

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

Returns a natural language response from MAFA-Agents.

---

## 🐛 Troubleshooting
| Issue | Solution |
|---|---|
| **401 Unauthorized** | JWT token expired. Call `/auth/refresh` with the refresh_token cookie to get a new one |
| **500 Internal Server Error** | Check server logs in IntelliJ console. Verify MySQL is running and accessible |
| **Chat returning 503** | Verify `agents_endpoint` is reachable and MAFA-Agents is running |
| **Database connection fails** | Ensure MySQL server is running, credentials are correct, and `spring.datasource.url` is set properly in `application.properties` |
| **Redis connection refused** | Start the Redis container in Docker Desktop. Verify port matches `spring.data.redis.port` |
| **Prometheus not scraping** | Check `prometheus.yml` target address matches `localhost:8080` |
| **Stock prices not updating** | Verify frontend or agents are calling `/stockprice` endpoint to fetch and cache new data |
| **Email not sending** | Verify Gmail App Password (not regular password) in `spring.mail.password`. Enable "Less secure apps" or use App Passwords |

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
**Database**: MySQL 8+
