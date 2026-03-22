# ForgeQueue 

### Distributed Job Processing Platform (Horizontally Scalable by Design)

ForgeQueue is a distributed job processing system that allows users to submit long-running tasks—such as report generation, file processing, or data analysis—without waiting for immediate results. When a task is submitted, the system returns a job ID and processes it asynchronously in the background.

Users can query job status at any time (pending, processing, completed, or failed). The system includes automatic retry with backoff for transient failures, dead-letter handling for persistent failures, rate limiting to prevent abuse, and idempotent submission to avoid duplicate processing.

ForgeQueue provides reliable, trackable, and horizontally scalable background execution through safe multi-worker coordination.

------------------------------------------------------------------------
#  Architecture Overview
<img width="1330" height="845" alt="image" src="https://github.com/user-attachments/assets/252cc83f-2599-495a-8c01-de15a363e199" />


------------------------------------------------------------------------

##  Tech Stack

## Backend

-   Java 17
-   Spring Boot 3.5
-   Spring Cloud Gateway
-   Hibernate / JPA
-   PostgreSQL 15
-   Redis 7

## Reliability & Resilience

-   Atomic row-level leasing (`SELECT FOR UPDATE SKIP LOCKED`)
-   30s visibility timeout
-   Exponential backoff (5--300s)
-   ±20% retry jitter
-   Dead-letter handling
-   Resilience4j (Circuit Breaker + Short-Term Retry)

## Infrastructure & DevOps

-   Docker (multi-stage builds)
-   Docker Compose
-   GitHub Actions (CI/CD)
-   Docker Hub (image registry)
-   Testcontainers (integration testing)

## API Documentation

-   OpenAPI (Swagger), aggregated via Gateway

------------------------------------------------------------------------



  

### Key Design Principles

-   Horizontal scalability
-   Strong consistency for job leasing
-   Idempotent submission
-   At-least-once processing semantics
-   Multi-tenant fairness
-   Failure isolation at execution layer
-   Infrastructure validated through integration tests

------------------------------------------------------------------------

#  Core Features

## 1️⃣ Idempotent Job Submission

-   Composite unique constraint (`user_id`, `idempotency_key`)
-   Transactional deduplication
-   Safe under concurrent submissions

Guarantees duplicate-safe job creation.

------------------------------------------------------------------------

## 2️⃣ Atomic Job Leasing

Uses:

``` sql
SELECT ... FOR UPDATE SKIP LOCKED
```

-   Prevents duplicate leasing
-   Safe for multiple worker instances
-   30-second visibility timeout
-   Lease expiration allows crash recovery

------------------------------------------------------------------------

## 3️⃣ Crash Recovery

If a worker crashes: - Lease expires - Job becomes eligible again -
`attempt_count` increments - Retry scheduling applied

Validated via integration tests.

------------------------------------------------------------------------

## 4️⃣ Retry Strategy

### Short-Term Retry (Resilience4j)

-   Millisecond-level retries
-   Circuit breaker protection
-   Protects downstream systems

### Long-Term Retry (Queue Logic)

-   Exponential backoff (5--300 seconds)
-   ±20% jitter to prevent retry storms
-   Dead-letter transition after max attempts

------------------------------------------------------------------------

## 5️⃣ Multi-Tenant Fairness

### Gateway-Level Rate Limiting

-   Redis-backed `RedisRateLimiter`
-   Header-based user identification

### Worker-Level Concurrency Throttling

-   Redis TTL-based counters
-   Prevents single user from saturating workers

------------------------------------------------------------------------

## 6️⃣ Distributed Correctness Validation

Validated via:

-   Testcontainers (PostgreSQL + Redis)
-   Concurrency simulation tests
-   Crash recovery tests
-   Idempotency race tests
-   Executed automatically in CI
## 7️⃣ Observability & Execution Insights

On success:

- Stores result_payload

- Stores completed_at

On failure:

- Stores last_error_message

- Stores last_error_stacktrace

- Stores failed_at on dead-letter transition

Provides traceable execution history and failure diagnostics.
## 8️⃣ Performance & Polling Optimization

- Indexed polling on (status, next_run_at, priority)

- Indexed lease expiry lookup

- Short polling interval with bounded batch size

- Optimized for high-concurrency worker coordination

Reduces lock contention and prevents sequential scan degradation.

------------------------------------------------------------------------

#  Project Structure

    ForgeQueue/
    │
    ├── forgequeue-core/
    │   ├── controller/
    │   ├── domain/
    │   ├── repository/
    │   ├── service/
    │   ├── worker/
    │   ├── execution/
    │   └── config/
    │
    ├── forgequeue-gateway/
    │   ├── config/
    │   └── routing/
    │
    ├── docker-compose.yml
    └── .github/workflows/ci.yml

------------------------------------------------------------------------

#  Containerization

-   Multi-stage Docker builds
-   Separate images for Core and Gateway
-   Docker Compose orchestration
-   Healthchecks enabled

Images are automatically published to Docker Hub on merge to main.

------------------------------------------------------------------------

# 🔁 CI/CD Pipeline

## Continuous Integration (CI)

On every push and pull request: - Builds multi-module Maven project -
Runs integration tests (Testcontainers) - Validates distributed
behavior - Builds Docker images

## Continuous Delivery (CD)

On merge to `main` branch: - Logs into Docker Hub via GitHub Secrets -
Builds production images - Tags images as `latest` - Pushes images
automatically to Docker Hub

 - docker.io/`<username>`{=html}/forgequeue-core:latest\
 - docker.io/`<username>`{=html}/forgequeue-gateway:latest
------------------------------------------------------------------------

#  Distributed Guarantees

ForgeQueue provides:

-   Idempotent job submission
-   Duplicate-safe leasing
-   Crash-safe processing
-   Controlled retries
-   Dead-letter handling
-   Per-user fairness
-   Horizontal scaling support
-   Execution-layer resilience

------------------------------------------------------------------------


------------------------------------------------------------------------

#  Running Locally

### 1️⃣ Build Project

    mvn clean install

### 2️⃣ Start Services

    docker compose up --build

### 3️⃣ Access APIs

Core:

    http://localhost:8080

Gateway (Public API + Swagger):

    http://localhost:8081/swagger-ui.html
------------------------------------------------------------------------



## Future Improvements

- **Tier-Based Scheduling** – Priority handling for paid users with fairness control  
- **Job Notifications** – Email/webhook callbacks with retries  
- **Audit Logging** – Track job state transitions  
- **Metrics & Monitoring** – Throughput, failures, alerting (e.g., Prometheus)  
- **Adaptive Polling** – Adjust polling based on load  
- **Queue Sharding** – Partition jobs for scalability  
- **Worker Heartbeat** – Extend leases for long-running jobs  
- **Exactly-Once Execution** – Idempotency + transactional outbox

------------------------------------------------------------------------
## Load Testing

Load testing was performed using Apache JMeter on the `POST /api/jobs` endpoint.

- Handled **~200 RPS** consistently in local environment
- Rate limiting (**HTTP 429**) validated under burst traffic
- Stable connection pool usage (HikariCP)
- No crashes or deadlocks observed

> Testing was conducted on a single-machine setup. Higher-scale validation requires distributed load generation.

#  Conclusion

ForgeQueue is designed as a backend systems showcase --- demonstrating
distributed coordination, concurrency control, resilience engineering,
and production-grade architectural decisions.

It is not just a job queue implementation --- it is a correctness-first
distributed system.

------------------------------------------------------------------------

Built for learning, system design mastery, and real-world backend
engineering.


