# Notifshot — Multi-Tenant Notification & Campaign Platform

A production-grade SaaS platform for sending large-scale notifications via Email, SMS, and Push. Built with Java Spring Boot, React TypeScript, Kafka, Redis, and PostgreSQL.

https://ibb.co/2Yn9n26h

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5 |
| Frontend | React 18, TypeScript |
| Database | PostgreSQL |
| Message Queue | Apache Kafka |
| Cache / Rate Limiting | Redis |
| DB Migrations | Flyway |
| Resilience | Resilience4j (Circuit Breaker, Retry) |

---

## Prerequisites

Make sure you have these installed before starting:

- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Node.js 18+](https://nodejs.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Git](https://git-scm.com/)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ilhamhmdn/notifshot.git
cd notifshot
```

---

### 2. Set up PostgreSQL

Open pgAdmin or psql and create the database:

```sql
CREATE DATABASE notifshot;
```

---

### 3. Start Kafka and Redis with Docker

From the project root (where `docker-compose.yml` is):

```bash
docker-compose up -d
```

Verify both containers are running:

```bash
docker ps
```

You should see `notifshot-kafka` and `notifshot-redis` both with status `Up`.

---

### 4. Configure the backend

Open `src/main/resources/application.yml` and update your PostgreSQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notifshot
    username: postgres
    password: (your password)
```

---

### 5. Run the backend

From the project root:

```bash
mvn spring-boot:run
```

Flyway will automatically create all database tables on first startup.

Verify the backend is running:

```bash
curl http://localhost:8080/actuator/health
```

You should see:
```json
{"status": "UP"}
```

---

### 6. Run the frontend

Open a new terminal:

```bash
cd notifshot-frontend
npm install
npm start
```

The dashboard will open automatically at `http://localhost:3000`.

---

## Using the Platform

### Create a Tenant (via Postman or curl)

```bash
curl -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corporation",
    "email": "admin@acme.com",
    "monthlyCampaignLimit": 100,
    "monthlyMessageLimit": 1000000,
    "campaignsUsed": 0,
    "messagesUsed": 0,
    "active": true
  }'
```

### Create a Campaign via Dashboard

1. Open `http://localhost:3000`
2. Click **Create Campaign**
3. Select your tenant from the dropdown
4. Fill in campaign name, channel (EMAIL / SMS / PUSH), and message template
5. Upload a CSV file with this format:

```csv
recipientId,email,phone
REC001,alice@example.com,+60123456781
REC002,bob@example.com,+60123456782
REC003,charlie@example.com,+60123456783
```

6. Click **Create Campaign**
7. You will be redirected to the Campaign Detail page where live delivery stats update in real time

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/tenants` | Create a tenant |
| GET | `/api/v1/tenants` | List all tenants |
| POST | `/api/v1/campaigns` | Create campaign + upload CSV |
| GET | `/api/v1/campaigns` | List campaigns (supports pagination) |
| GET | `/api/v1/campaigns/{id}` | Get campaign details and live stats |
| POST | `/api/v1/campaigns/{id}/retry-failures` | Retry all failed jobs |

---

## Architecture Overview

```
Frontend (React TypeScript) — localhost:3000
      ↓
REST API (Spring Boot 3.3.5) — localhost:8080
      ↓
PostgreSQL ← Flyway migrations (versioned schema)
      ↓
Kafka (async processing) — localhost:9092
      ↓
Notification Workers (3 concurrent consumers)
  → Rule Engine
      - Global Suppression Check
      - Quiet Hours (10pm–8am per recipient timezone)
      - Tenant Credit Check
      - Deduplication (5 min window via Redis)
  → Rate Limiter (100 req/min per channel — Bucket4j token bucket)
  → Simulated Provider (20% failure rate, 50–200ms latency)
  → Retry with exponential backoff (max 3 attempts)
  → Circuit Breaker per provider (Resilience4j)
```

---

## Observability

| Endpoint | Description |
|---|---|
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/actuator/metrics` | Application metrics |
| `http://localhost:8080/actuator/prometheus` | Prometheus metrics scrape endpoint |

---

## Key Features

- **Multi-tenancy** — full data isolation per tenant
- **Async processing** — API returns 202 immediately, Kafka handles delivery
- **Idempotency** — no duplicate sends even after crashes or retries
- **Rule Engine** — suppression, quiet hours, credit limits, deduplication
- **Rate limiting** — token bucket algorithm, 100 req/min per channel
- **Streaming CSV** — processes millions of rows without loading into memory
- **Circuit breakers** — auto-stops requests to failing providers
- **PII masking** — emails and phone numbers masked in all logs (GDPR/SOC2)
- **Live dashboard** — delivery stats poll every 5 seconds

---

## Stopping the Application

Stop the frontend: `Ctrl+C` in the frontend terminal

Stop the backend: `Ctrl+C` in the backend terminal

Stop Docker containers:
```bash
docker-compose down
```

---

## Troubleshooting

**PostgreSQL connection failed**
- Check your password in `application.yml`
- Make sure PostgreSQL is running on port 5432

**Kafka errors on startup**
- Make sure Docker Desktop is running before starting the backend
- Run `docker-compose up -d` first

**Frontend shows Network Error**
- Make sure the backend is running on port 8080
- Both backend and frontend must be running at the same time

**npm start fails**
- Run `npm install` first inside the `notifshot-frontend` folder
- Make sure Node.js 18+ is installed: `node --version`

**Campaign stays RUNNING**
- This resolves automatically once all Kafka jobs finish processing
- Refresh the campaign detail page after a few seconds
