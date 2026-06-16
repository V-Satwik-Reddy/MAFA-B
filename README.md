# MAFA-B: Backend for Multi-Agent Financial Assistant (MAFA)

MAFA-B is the **Spring Boot backend** for the **Multi-Agent Financial Assistant (MAFA)** system. It provides REST API endpoints for user account management, portfolio analytics, stock market data, trade execution, and multi-agent chat integration.

The backend orchestrates communication between the frontend, multiple AI agents, and external market data services to deliver real-time financial advisory and trading capabilities.

---

## 📊 Project Overview

MAFA-B is a production-ready Spring Boot 3.x service that:

- **User Management**: Signup/login with JWT authentication, profile management
- **Portfolio Tracking**: Real-time balance, holdings, portfolio analytics, and risk metrics
- **Market Data**: Live stock prices, historical data, price alerts
- **Trade Execution**: Buy/sell orders with balance and holdings updates
- **Multi-Agent Chat**: Integration with MAFA-agents for AI-driven financial advice
- **Observability**: Prometheus metrics and Grafana dashboards for monitoring

---

## 🔗 Related Repositories

MAFA is a distributed system. MAFA-B depends on these companion repositories:

| Repository | Language | Purpose |
|---|---|---|
| **[s0meshnalla/MAFA-agents](https://github.com/s0meshnalla/MAFA-agents)** | Python | Multi-agent orchestration system (market research, portfolio analysis, strategy recommendations, trade execution) with MCP servers |
| **[s0meshnalla/MCP_Financial_analyst_frontend](https://github.com/s0meshnalla/MCP_Financial_analyst_frontend)** | JavaScript/React | Web frontend for MAFA system |
| **MAFA-B** (this repo) | Java/Spring Boot | REST backend and user management layer |

### Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│  Frontend (React)                                        │
│  (MCP_Financial_analyst_frontend)                        │
└─────────────────────────────────────────────────────────┘
                         ↓
         ┌───────────────────────────────────┐
         │  MAFA-B (Spring Boot Backend)     │
         │  REST API, JWT Auth, DB Layer     │
         └───────────────────────────────────┘
                         ↓
     ┌──────────────────────────────────────────┐
     │  MAFA-agents (Python MCP System)         │
     │  • Market Research Server (LSTM, News)   │
     │  • Execution Server (Trade Orders)       │
     │  • Portfolio Server (Risk Analysis)      │
     │  • Strategy Server (Recommendations)     │
     │  • MCP Orchestrator (Agent Coordination) │
     │  • Event Bus (Redis Pub/Sub)             │
     └──────────────────────────────────────────┘
                         ↓
     ┌──────────────────────────────────────────┐
     │  External Services                       │
     │  • Alpha Vantage (Stock Data)            │
     │  • News APIs (Market News)               │
     │  • Google Gemini (LLM)                   │
     │  • Supabase (Vector Memory)              │
     └──────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+** ([download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** (or use included `mvnw.cmd` / `mvnw`)
- **Docker** (for Redis, Prometheus, Grafana)
- **Database**: PostgreSQL (configured in `application.properties`)
- **API Keys**:
  - Google Gemini API key (for LLM integration)
  - MAFA-agents endpoint URL

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

# CORS
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5000

# LLM Integration
GEMINI_API_KEY=your_google_gemini_api_key

# Multi-Agent System
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

### 3. Install Dependencies

```bash
# Using Maven wrapper (Windows)
mvnw.cmd clean install

# Using Maven wrapper (Linux/Mac)
./mvnw clean install

# Or use system Maven
mvn clean install
```

### 4. Start Docker Services (Optional)

```bash
# PostgreSQL
docker run -d \
  --name postgres \
  -e POSTGRES_DB=mafa_db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Redis (if you want to experiment with caching)
docker run -d --name redis -p 6379:6379 redis:7

# Prometheus (for metrics)
docker run -d \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  --name prometheus \
  prom/prometheus

# Grafana (for dashboards)
docker run -d \
  -p 3000:3000 \
  --name grafana \
  grafana/grafana
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
- `GET /transactions` — Transaction history

**Portfolio:**
- `GET /portfolio` — Portfolio metrics
- `GET /dashboard` — Dashboard data

**Alerts:**
- `GET /alert` — List price alerts
- `POST /alert` — Create price alert
- `PUT /alert/{id}` — Update alert
- `DELETE /alert/{id}` — Delete alert

**Chat & AI:**
- `POST /general-chat` — Chat with general assistant
- `POST /ea-chat` — Execution assistant
- `POST /mra-chat` — Market research assistant
- `GET /chats` — Chat history

**Strategy:**
- `GET /strategy` — Get strategy
- `POST /strategy` — Create strategy
- `PUT /strategy/{id}` — Update strategy
- `GET /strategy/history` — Strategy performance

---

## 🔧 Configuration Details

### JWT Authentication

- **Secret**: Configure `jwt.secret` in properties (use a strong base64-encoded key)
- **Access Token**: Short-lived JWT in response
- **Refresh Token**: Long-lived HttpOnly cookie
- **Flow**: Login → receive accessToken + refresh_token cookie → use accessToken for all requests → refresh when expired

### Database

- **Default**: PostgreSQL 15+
- **Migrations**: Hibernate auto-DDL (set `spring.jpa.hibernate.ddl-auto=update`)
- **Entities**: `User`, `Chat`, `Transaction`, `Holdings`, `Alert`, `Strategy`, `Stock`, `StockPrice`

### LLM Integration

If `gemini.api.key` is set:
- `/general-chat` calls Google Gemini directly
- Responses are persisted to chat history

Otherwise:
- Chat requests are forwarded to `agents_endpoint/general-agent`
- The MAFA-agents system handles AI responses

### Multi-Agent Communication

- **Endpoint**: `agents_endpoint` (e.g., `http://localhost:8081/`)
- **Protocol**: HTTP + JWT forwarding
- **Agents**: Market Research, Portfolio Analysis, Strategy, Execution
- **Event Bus**: Redis Pub/Sub (MAFA-agents uses MCPTopics)

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
4. Import dashboards or create custom ones

### Key Metrics

- `http_requests_seconds` — Request latency (p50/p95/p99)
- `http_requests_total` — Request count by endpoint
- `jvm_memory_used_bytes` — JVM memory usage
- `jvm_gc_pause_seconds` — Garbage collection pauses
- Custom: Chat request counts, agent latency

---

## 🧪 Performance Testing

### Using JMeter

1. Install [JMeter](https://jmeter.apache.org/)
2. Create test plan with HTTP sampler targeting `/general-chat`
3. Add HTTP Header Manager: `Authorization: Bearer <token>`
4. Run:

```bash
jmeter -n -t test-plan.jmx -l results.jtl -e -o report-folder
```

5. Results are saved to `performance_data.xlsx` for comparison

---

## 🛠️ Development

### Project Structure

```
src/main/java/majorproject/maf/
├── controller/        # REST endpoints
│   ├── AuthController
│   ├── ChatController
│   ├── ExecutionController
│   ├── PriceFetchController
│   ├── PortfolioController
│   └── ...
├── service/           # Business logic
│   ├── AuthService
│   ├── ChatService
│   ├── ExecutionService
│   └── ...
├── model/             # JPA entities
│   ├── User
│   ├── Chat
│   ├── Transaction
│   └── ...
├── repository/        # Database access
├── exception/         # Custom exceptions
└── config/            # Security, CORS, etc.
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

- **JWT**: HS256 signing with `jwt.secret`
- **CORS**: Configurable via `allowed_origins`
- **HTTPS**: Configure in production (use Spring Security with SSL)
- **Secrets**: Use environment variables, not in code
- **OWASP**: Standard Spring Security defaults applied

---

## 📌 Environment Variables Summary

| Variable | Required | Default | Description |
|---|---|---|---|
| `JWT_SECRET` | ✅ | None | JWT signing secret (base64) |
| `SPRING_DATASOURCE_URL` | ✅ | None | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | ✅ | None | DB username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | None | DB password |
| `ALLOWED_ORIGINS` | ❌ | http://localhost:3000 | CORS origins |
| `GEMINI_API_KEY` | ❌ | None | Google Gemini API key |
| `AGENTS_ENDPOINT` | ❌ | http://localhost:8081/ | MAFA-agents base URL |
| `SPRING_REDIS_HOST` | ❌ | localhost | Redis host |
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

### General Chat

```bash
curl -X POST http://localhost:8080/general-chat \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"query": "Should I buy TSLA?"}'
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|---|---|
| 401 Unauthorized | Check JWT token expiry; refresh with `/auth/refresh` |
| 500 Internal Server Error | Check server logs; verify DB and Gemini API connectivity |
| Chat returning 503 | Verify `agents_endpoint` is reachable or `GEMINI_API_KEY` is set |
| Prometheus not scraping | Check `prometheus.yml` target; verify network connectivity |
| DB connection fails | Ensure PostgreSQL is running; verify credentials in `.env` |

---

## 📞 Support & Documentation

- **Chat Subsystem**: See in-code documentation for `ChatService` and `ChatController`
- **API Details**: Refer to individual controller classes for endpoint specifics
- **Agent Integration**: Check `agents_endpoint` configuration and MAFA-agents repository

---

## 📄 License

[Add your license here]

---

## 🙋 Contributing

[Add contribution guidelines here]

---

**Last Updated**: 2026-06-16
