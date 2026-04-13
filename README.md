# Notifshot — Multi-Tenant Notification & Campaign Platform

A production-grade SaaS platform for sending large-scale notifications via Email, SMS, and Push. Built with Java Spring Boot, React TypeScript, Kafka, Redis, and PostgreSQL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3 |
| Frontend | React 18, TypeScript |
| Database | PostgreSQL 18 |
| Message Queue | Apache Kafka |
| Cache / Rate Limiting | Redis |
| DB Migrations | Flyway |
| Resilience | Resilience4j (Circuit Breaker, Retry) |

---

## Prerequisites

Make sure you have these installed before starting:

- [Java 21](https://adoptium.net/)
- [Maven](https://maven.apache.org/download.cgi)
- [Node.js 18+](https://nodejs.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [PostgreSQL 18](https://www.postgresql.org/download/)
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
    password: YOUR_PASSWORD   # ← change this
```

---

### 5. Run the backend

```bash
cd notifshot
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

### Create a Tenant

```
POST http://localhost:8080/api/v1/tenants
Content-Type: application/json

{
    "name": "Acme Corporation",
    "email": "admin@acme.com",
    "monthlyCampaignLimit": 100,
    "monthlyMessageLimit": 1000000,
    "campaignsUsed": 0,
    "messagesUsed": 0,
    "active": true
}
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
```

6. Click **Create Campaign**
7. You will be redirected to the Campaign Detail page where you can watch live delivery stats update in real time

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/tenants` | Create a tenant |
| GET | `/api/v1/tenants` | List all tenants |
| POST | `/api/v1/campaigns` | Create a campaign + upload CSV |
| GET | `/api/v1/campaigns` | List campaigns (supports pagination) |
| GET | `/api/v1/campaigns/{id}` | Get campaign details and live stats |
| POST | `/api/v1/campaigns/{id}/retry-failures` | Retry all failed jobs |

---

## Observability

| Endpoint | Description |
|---|---|
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/actuator/metrics` | Application metrics |
| `http://localhost:8080/actuator/prometheus` | Prometheus metrics |

---

## Architecture Overview

```
Frontend (React)
      ↓
REST API (Spring Boot)
      ↓
PostgreSQL ← Flyway migrations
      ↓
Kafka (async processing)
      ↓
Notification Workers
  → Rule Engine (suppression, quiet hours, credit check, deduplication)
  → Rate Limiter (100 req/min per channel via Bucket4j)
  → Simulated Provider (20% failure rate, 50–200ms latency)
  → Retry with exponential backoff (max 3 attempts)
  → Circuit Breaker (Resilience4j)
```

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
- Make sure Docker Desktop is running
- Run `docker-compose up -d` before starting the backend

**Frontend shows Network Error**
- Make sure the backend is running on port 8080
- Check that CORS is configured (it is by default)

**npm start fails**
- Run `npm install` first
- Make sure Node.js 18+ is installed: `node --version`
