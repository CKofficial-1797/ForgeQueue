# ForgeQueue 

### Distributed Job Processing Platform (Horizontally Scalable)

ForgeQueue is a production-grade distributed job processing platform
designed to demonstrate real-world backend architecture, fault
tolerance, and distributed coordination.

It is built with a focus on correctness, scalability, and operational
reliability --- not just feature completeness.

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

#  Architecture Overview

    Client
       ↓
    Spring Cloud Gateway (Rate Limiting + Routing)
       ↓
    ForgeQueue Core (Job Engine + Worker Execution)
       ↓
    PostgreSQL (Leasing + State)
    Redis (Concurrency + Fairness)

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

#  Conclusion

ForgeQueue is designed as a backend systems showcase --- demonstrating
distributed coordination, concurrency control, resilience engineering,
and production-grade architectural decisions.

It is not just a job queue implementation --- it is a correctness-first
distributed system.

------------------------------------------------------------------------

Built for learning, system design mastery, and real-world backend
engineering.

