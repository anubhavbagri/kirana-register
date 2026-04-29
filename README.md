# Kirana Store - Transaction Register Backend

The objective is to develop a backend service aiding Kirana stores in managing their transaction registers. Responsibility of this service is to track daily credit and debit transactions, ensuring effective financial management.

## Functional Requirements

### Transactional API:
An API which will record transactions in different currencies (INR & USD). Use the reference API for currency conversion (currency conversion rates are volatile).
> Reference API: https://api.fxratesapi.com/latest

### Reporting API:
API for generating weekly, monthly, and yearly financial reports with insights like total credits, debits, and net flow. Implemented using Kafka and should be implemented using an async flow.

### User Authentication and Authorization:
Secure access with a clear distinction between authentication and authorization.

### API Rate Limiting:
Implement configurable rate limiting mechanism for both APIs to prevent abuse, exploring various algorithms and tools. For example, transaction record API can have 10 requests/minute limit.

### Caching:
Cache currency conversion API response to avoid hitting API limit.

### Implement DAO Layer:
Read and understand the importance of a DAO layer and how to use it for cache validation and invalidation.

### Metrics (Prometheus & Grafana):
Implement Prometheus and Grafana to monitor the application for performance and degradation. Demonstrate latency and throughput of the application on the dashboard.

### Logging:
Implement logging using ELK stack. Use logback XML with logstash to route the logs to your ELK destination.

## Add ons

### Handle Products / Items Purchased in Each Transaction
In each transaction, information about the products/items purchased needs to be recorded. The system must associate multiple products with a transaction. Store relevant details such as quantity, price, category, etc. in product details.

#### Steps
1. Design database schemas to capture product details, including attributes like product name, category, price, and quantity.
2. Develop APIs for creating, updating, and retrieving product information.
3. Implement logic to associate products with each transaction and store detailed purchase information within the transaction record.

### Handle Refunds for Transactions
When a customer requests a refund, the system should reverse the transaction by adding/updating the records. The transaction entries should reflect the refund, and the reporting APIs should adjust sales totals and inventory levels accordingly.

#### Steps
1. Implement logic to add/update transaction entries when a refund is processed.
2. Adjust inventory and financial records to account for the refunded products.
3. Update the reporting APIs to reflect changes in total credits, debits, net_flow, and inventory after refunds are processed.

---

## Project Guide (Implementation-Aware)

This section is additive and documents what the current codebase does today, plus practical guidance to run and extend it.

### 1) What This Service Actually Does

This backend helps a Kirana store:

- Register users and authenticate with JWT access tokens and refresh tokens.
- Manage product catalog in MongoDB.
- Record purchase bills and refunds.
- Convert currency using FxRates API and cache conversion rates in Redis.
- Persist transaction ledger in PostgreSQL for reporting.
- Generate weekly/monthly/yearly reports through Kafka-triggered async consumer logic.
- Expose Prometheus metrics (`/actuator/prometheus`) for monitoring.

### 2) Architecture At a Glance

```mermaid
flowchart LR
    C[Client / Frontend / Postman]
    API[Spring Boot API]
    SEC[Security + JWT + AOP Rate Limits]
    SVC[Service Layer]
    DAO[DAO Layer]
    PG[(PostgreSQL - transactions)]
    MG[(MongoDB - users/products/bills/refresh_tokens)]
    RD[(Redis - cache)]
    KF[(Kafka - report trigger)]
    FX[FxRates API]
    OBS[Prometheus + Grafana]

    C --> API --> SEC --> SVC --> DAO
    DAO --> PG
    DAO --> MG
    SVC --> RD
    SVC --> FX
    KF --> SVC
    API --> OBS
```

### 3) Technology Stack (With Why + Docs)

| Area | Tech | Why here | Official docs |
|---|---|---|---|
| Runtime | Java 21 | Modern LTS JVM features and performance | https://docs.oracle.com/en/java/javase/21/ |
| Framework | Spring Boot 3 | Fast setup for REST, DI, security, data access | https://docs.spring.io/spring-boot/docs/current/reference/html/ |
| Build | Gradle Wrapper | Reproducible builds across machines | https://docs.gradle.org/current/userguide/gradle_wrapper.html |
| SQL store | PostgreSQL + Spring Data JPA | Durable transactional ledger for transaction history/reporting | https://www.postgresql.org/docs/ and https://docs.spring.io/spring-data/jpa/reference/ |
| Document store | MongoDB + Spring Data MongoDB | Flexible schema for products/users/bills/refresh tokens | https://www.mongodb.com/docs/ and https://docs.spring.io/spring-data/mongodb/reference/ |
| Cache | Redis | Fast short-lived cache for FX conversion rates | https://redis.io/docs/latest/ |
| Auth | Spring Security + JWT | Stateless authentication with role-based authorization | https://docs.spring.io/spring-security/reference/ and https://jwt.io/introduction |
| Messaging | Apache Kafka + Spring Kafka | Async report trigger consumer flow | https://kafka.apache.org/documentation/ and https://docs.spring.io/spring-kafka/reference/ |
| Rate limiting | Bucket4j | Token-bucket based request throttling | https://bucket4j.com/ |
| Monitoring | Actuator + Micrometer + Prometheus + Grafana | Metrics scraping and dashboards | https://docs.spring.io/spring-boot/reference/actuator/ and https://micrometer.io/docs and https://prometheus.io/docs/introduction/overview/ and https://grafana.com/docs/ |
| Logging | Logback + Logstash encoder | Structured logs for ELK pipelines | https://logback.qos.ch/documentation.html and https://github.com/logfellow/logstash-logback-encoder |

### 4) Quick Start (Docker-First)

This is the fastest path when you do not want local Java/Postgres/Mongo/Redis/Kafka installs.

#### 4.1 Prerequisites

- Docker Desktop (with Compose v2)

#### 4.2 Build JAR using Dockerized Gradle (no local JDK needed)

```bash
docker run --rm -v "${PWD}:/workspace" -w /workspace gradle:8.10.2-jdk21 gradle clean bootJar --no-daemon
```

#### 4.3 Start core dependencies + app

```bash
docker compose up -d postgres mongodb redis kafka spring-boot-app
```

Application base URL: `http://localhost:8080`

#### 4.4 Optional observability stack

```bash
docker compose up -d prometheus grafana
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin password in compose file)

#### 4.5 Important compose caveats

- `spring-boot-app` depends on `build/libs/*.jar`; run the build step first.
- ELK services in `docker-compose.yaml` reference `logstash/...` files not present in this repository. Start ELK only after adding those files.
- The `spring-boot-app` volume path is Mac-specific (`/Users/...`) and may need adjustment in your environment.

### 5) Local Development (Without Editing Existing Files)

If you run app outside Docker but keep infra in Docker:

1. Start infra:
   ```bash
   docker compose up -d postgres mongodb redis kafka
   ```
2. Run app with property overrides so localhost works:
   ```bash
   ./gradlew bootRun --args="--spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase --spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/mydatabase?authSource=admin --spring.data.redis.host=localhost --spring.kafka.bootstrap-servers=localhost:9092"
   ```

### 6) API Reference (Current Contracts)

All responses are wrapped in `ApiResponse`:

```json
{
  "success": true,
  "data": {},
  "status": "OK",
  "error": null,
  "errorMessage": null,
  "errorCode": null
}
```

#### 6.1 Auth & User APIs

1. `POST /register`
   - Body:
     ```json
     {
       "username": "admin",
       "password": "admin123",
       "roles": ["ADMIN"]
     }
     ```
   - Returns created user payload (`password` currently included in DTO response).

2. `POST /login`
   - Body:
     ```json
     {
       "username": "admin",
       "password": "admin123"
     }
     ```
   - Response data (`AuthResponse`):
     ```json
     {
       "userId": "USER_ID",
       "accessToken": "JWT",
       "refreshToken": "TOKEN_HASH_STRING"
     }
     ```

3. `GET /generate-token`
   - Headers:
     - `Authorization: Bearer <access_token>`
     - `Refresh-Token: <refresh_token>`
   - Returns new `AuthResponse`.

#### 6.2 Product APIs

Base: `/v1/api/products`

1. `POST /v1/api/products/add` (ADMIN role)
   - Body:
     ```json
     {
       "name": "Rice",
       "type": "GROCERY",
       "price": 58.5
     }
     ```

2. `GET /v1/api/products?page=0&size=10`

3. `GET /v1/api/products/type?category=GROCERY&page=0&size=10`

#### 6.3 Transaction APIs

Current code uses `@RestController("/v1/api")` (bean name), not `@RequestMapping("/v1/api")`.
So endpoints exposed by current implementation are:

1. `POST /purchase`
   - Header: `Authorization: Bearer <access_token>`
   - Body:
     ```json
     {
       "currencyCode": "USD",
       "billItems": [
         { "itemName": "Rice", "quantity": 2 },
         { "itemName": "Oil", "quantity": 1 }
       ]
     }
     ```
   - Response data:
     ```json
     {
       "userName": "ADMIN",
       "billId": "BILL_ID",
       "amount": 4.7,
       "billItems": [
         { "itemName": "Rice", "quantity": 2 },
         { "itemName": "Oil", "quantity": 1 }
       ],
       "transactionType": null
     }
     ```

2. `POST /refund`
   - Header: `Authorization: Bearer <access_token>`
   - Body:
     ```json
     { "billId": "BILL_ID" }
     ```
   - Response data: `"Refund successful"`

### 7) Project Structure

```text
.
|-- src/main/java/com/jarapplication/kiranastore
|   |-- config/                      # Spring Security config
|   |-- filters/                     # JWT and global rate limiting filters
|   |-- AOP/                         # Annotation-driven cross-cutting aspects
|   |-- feature_users/               # user/authn domain (MVC + DAO + repository)
|   |-- feature_products/            # product catalog domain
|   |-- feature_transactions/        # purchase/refund + conversion + billing
|   |-- feature_reports/             # report retrieval from transaction ledger
|   |-- auth/                        # refresh token domain
|   |-- cache/, redis/               # redis abstraction
|   |-- kakfa/                       # kafka consumer for report messages
|   |-- response/, constants/, utils/
|-- src/main/resources/application.properties
|-- docker-compose.yaml
|-- Dockerfile
|-- build.gradle
|-- prometheus.yml
```

### 8) Architecture Deep Dive

For HLD, LLD, design principles, schema details, scalability, testing strategy, and production-grade tradeoffs, read:

- `architecture.md`
