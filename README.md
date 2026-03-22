# flux-bank

> A production-grade, cloud-native banking platform built with Java 21 and Spring Boot 3.3.

---

## Architecture

```
                           ┌───────────────────────────────────────────────┐
                           │              flux-bank platform                │
                           └───────────────────────────────────────────────┘

  Client (web / mobile)
        │  HTTPS
        ▼
  ┌─────────────┐          ┌─────────────────┐     ┌──────────────────┐
  │  api-gateway│◄────────►│ service-registry│     │  config-server   │
  │  :8080      │  Eureka  │    (Eureka)      │     │  :8888  (native) │
  └──────┬──────┘          │    :8761        │     └──────────────────┘
         │  lb://          └─────────────────┘
         │
  ┌──────┼────────────────────────────────────────────────────────────┐
  │      │           Downstream microservices (Phase 1+)              │
  │  ┌───▼──────┐  ┌──────────────┐  ┌────────────────┐              │
  │  │auth-svc  │  │account-svc   │  │transaction-svc │  ...         │
  │  │:8081     │  │:8082         │  │:8083           │              │
  │  └────┬─────┘  └──────┬───────┘  └───────┬────────┘              │
  └───────┼───────────────┼─────────────────┼───────────────────────┘
          │               │                 │
          └───────────────┴────────┬────────┘
                                   │  Kafka (events)
                            ┌──────▼──────────────┐
                            │ Apache Kafka :9092   │
                            │ Kafka UI     :8090   │
                            └─────────────────────┘
                                   │
          ┌────────────────────────┴────────────────────────┐
          │ Databases (PostgreSQL 16 — one per service)      │
          │  auth_db :5433  accounts_db :5434  …            │
          └─────────────────────────────────────────────────┘
                                   │
                            ┌──────▼──────┐
                            │ Redis :6379 │
                            │ (cache /    │
                            │  rate limit)│
                            └─────────────┘
```

---

## Tech Stack

| Layer            | Technology                        | Version   |
|------------------|-----------------------------------|-----------|
| Language         | Java                              | 21        |
| Framework        | Spring Boot                       | 3.3.5     |
| Service Mesh     | Spring Cloud                      | 2023.0.3  |
| API Gateway      | Spring Cloud Gateway (WebFlux)    | 4.1.x     |
| Service Registry | Netflix Eureka                    | 4.1.x     |
| Config           | Spring Cloud Config (native)      | 4.1.x     |
| Circuit Breaker  | Resilience4j                      | 2.2.x     |
| Messaging        | Apache Kafka                      | 3.7.x     |
| Cache            | Redis                             | 7.2       |
| Database         | PostgreSQL                        | 16        |
| Build            | Maven                             | 3.9.x     |
| Observability    | Micrometer + OpenTelemetry        | 1.42.x    |
| Containerisation | Docker / Docker Compose           | v3.9      |

---

## Repository Layout

```
flux-bank/
├── common-lib/                # Shared DTOs, exceptions, events, Kafka constants
├── service-registry/          # Eureka discovery server
├── config-server/             # Spring Cloud Config Server (native profile)
│   └── src/main/resources/
│       └── config/            # Per-service YAML config files
├── api-gateway/               # Spring Cloud Gateway + Resilience4j
├── infrastructure/
│   └── docker-compose.yml     # Full local dev stack
└── pom.xml                    # Root multi-module POM
```

---

## Services & Ports

| Service              | Port  | Description                            |
|----------------------|-------|----------------------------------------|
| api-gateway          | 8080  | Single ingress — routes all traffic    |
| service-registry     | 8761  | Eureka dashboard + REST API            |
| config-server        | 8888  | Centralised configuration              |
| auth-service         | 8081  | _(Phase 1)_ JWT authentication         |
| account-service      | 8082  | _(Phase 1)_ Account management         |
| transaction-service  | 8083  | _(Phase 1)_ Ledger transactions        |
| payment-service      | 8084  | _(Phase 1)_ Payment processing         |
| card-service         | 8085  | _(Phase 1)_ Card management            |
| kyc-service          | 8086  | _(Phase 1)_ KYC / onboarding           |
| notification-service | 8087  | _(Phase 1)_ Push / email notifications |
| Kafka                | 9092  | Event streaming                        |
| Kafka UI             | 8090  | Kafka web UI (Provectus)               |
| Redis                | 6379  | Cache / rate limiter                   |
| postgres-auth        | 5433  | PostgreSQL — auth_db                   |
| postgres-accounts    | 5434  | PostgreSQL — accounts_db               |
| postgres-transactions| 5435  | PostgreSQL — transactions_db           |
| postgres-payments    | 5436  | PostgreSQL — payments_db               |
| postgres-cards       | 5437  | PostgreSQL — cards_db                  |
| postgres-kyc         | 5438  | PostgreSQL — kyc_db                    |
| postgres-notifications| 5439 | PostgreSQL — notifications_db          |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker 24+ with Docker Compose v2

### 1 — Start the infrastructure

```bash
cd infrastructure
docker compose up -d
```

Wait ~30 s for all health checks to pass:

```bash
docker compose ps   # all services should show "healthy"
```

### 2 — Build the project

```bash
# From the repository root
mvn clean install -DskipTests
```

### 3 — Start services in order

```bash
# Terminal 1 — Service Registry (must start first)
cd service-registry
mvn spring-boot:run

# Terminal 2 — Config Server
cd config-server
mvn spring-boot:run

# Terminal 3 — API Gateway
cd api-gateway
mvn spring-boot:run
```

### 4 — Verify

| URL                                | Description               |
|------------------------------------|---------------------------|
| http://localhost:8761              | Eureka dashboard          |
| http://localhost:8888/actuator     | Config server actuator    |
| http://localhost:8080/actuator     | Gateway actuator          |
| http://localhost:8090              | Kafka UI                  |

---

## Actuator Endpoints

All services expose `/actuator/*` with full detail:

```bash
curl http://localhost:8080/actuator/health | jq
curl http://localhost:8761/actuator/health | jq
curl http://localhost:8888/actuator/health | jq
```

---

## Environment Variables

Sensitive values are **not** committed. Create a `.env` file at the repository root (see `.gitignore`) and override defaults as needed.

| Variable                | Default          | Used by             |
|-------------------------|------------------|---------------------|
| `REDIS_PASSWORD`        | `fluxbank_redis` | Redis, gateway      |
| `POSTGRES_PASSWORD`     | `fluxbank_pass`  | All Postgres nodes  |
| `EUREKA_URL`            | `http://localhost:8761/eureka/` | All services |
| `CONFIG_SERVER_URL`     | `http://localhost:8888/config` | All services |

---

## Contributing

1. Branch from `main` — branch name: `feature/<ticket-id>-short-description`
2. Run `mvn verify` before pushing
3. Open a PR — CI must be green before merge
