# SilentSupply

B2B supply chain marketplace with automated negotiation.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose (for local PostgreSQL)

## Quick Start

1. Start PostgreSQL:

```bash
docker compose up -d
```

2. Run the application:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

3. Open Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Running Tests

Tests use TestContainers (requires Docker running):

```bash
./mvnw test
```

## API Overview

| Area | Endpoints |
|------|-----------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Companies | `POST /api/companies`, `GET /api/companies/{id}`, `GET /api/companies` |
| Products | CRUD at `/api/products`, search with filters |
| Orders | `POST /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/status` |
| RFQs | `POST /api/rfqs`, `GET /api/rfqs/{id}`, `GET /api/rfqs` |
| Proposals | `POST /api/rfqs/{id}/proposals`, `GET /api/rfqs/{id}/proposals` |
| Rules | CRUD at `/api/suppliers/{id}/negotiation-rules` |

## Architecture

Spring Boot layered architecture with domain-driven packages:

```
com.silentsupply
├── config/        # Security, JWT, OpenAPI
├── common/        # Base entity, exceptions, error handling
├── company/       # Company registration & auth
├── product/       # Product catalog
├── order/         # Catalog orders (direct purchase)
├── rfq/           # RFQ lifecycle
├── proposal/      # Proposals within RFQ negotiations
└── negotiation/   # Rules engine & auto-negotiation
```
