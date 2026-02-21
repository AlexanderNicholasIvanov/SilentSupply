# SilentSupply — Design Document

**Date:** 2026-02-21
**Status:** Approved

## Overview

SilentSupply is a B2B supply chain marketplace where suppliers list inventory with pricing and terms, and buyers place orders or submit RFQs. Negotiations happen through an automated rules engine — no unstructured communication.

## Order Model: Hybrid (Catalog + RFQ)

- **Catalog orders:** Buyers purchase directly from product listings at the listed price. Instant, no negotiation.
- **RFQ flow:** Buyers submit structured requests for bulk/custom orders. A rules engine auto-negotiates against supplier-defined parameters (price floors, volume discounts, delivery constraints). Human intervention only when automation can't reach agreement.

## User Roles

- **Supplier:** Lists products, configures negotiation rules, fulfills orders.
- **Buyer:** Browses products, places catalog orders, submits RFQs.
- Distinct roles — a company is one or the other, not both.

## Core Domain Model

### Company
Registered business. Role: `SUPPLIER` or `BUYER`. Holds business name, contact info, verification status.

### Product
Listed by a supplier. Name, description, category, SKU, unit of measure, base price, available quantity. Status: `ACTIVE`, `OUT_OF_STOCK`, `DISCONTINUED`.

### CatalogOrder
Direct purchase at listed price. Status flow: `PLACED → CONFIRMED → SHIPPED → DELIVERED` (or `CANCELLED`).

### RFQ (Request for Quote)
Buyer request for bulk/custom orders. Contains product reference, desired quantity, target price, delivery requirements, expiration date. Status flow: `SUBMITTED → UNDER_REVIEW → COUNTERED → ACCEPTED → REJECTED → EXPIRED`.

### Proposal
Individual offer within an RFQ. Contains price, quantity, delivery terms, validity period. System auto-generates counter-proposals based on supplier rules.

### NegotiationRule
Supplier-defined per product. Price floor, volume discount tiers, max delivery window, auto-accept threshold. Max negotiation rounds (default: 3).

## Automated Negotiation Flow

1. Buyer submits RFQ with desired price, quantity, delivery date.
2. System evaluates against supplier's rules.
3. If all rules satisfied → auto-accept, create order.
4. If within negotiable range → auto-counter with best terms the rules allow.
5. If outside all acceptable ranges → auto-reject with reason code.
6. Buyer can accept counter, revise proposal, or withdraw.
7. Max rounds enforced — RFQ expires if no agreement.

Deterministic rule evaluation. No ML. Predictable and auditable.

## API Structure

```
com.silentsupply
├── config/          # Security, CORS, app config
├── company/         # Company registration & management
├── product/         # Product catalog CRUD
├── order/           # Catalog orders (direct purchase)
├── rfq/             # RFQ submission & lifecycle
├── proposal/        # Proposal creation & negotiation
├── negotiation/     # Rules engine & auto-negotiation logic
└── common/          # Shared DTOs, exceptions, utilities
```

### Key Endpoints

| Area | Endpoints |
|------|-----------|
| Companies | `POST /api/companies`, `GET /api/companies/{id}` |
| Products | CRUD at `/api/products`, filtered search |
| Orders | `POST /api/orders`, `GET /api/orders/{id}`, status updates |
| RFQs | `POST /api/rfqs`, `GET /api/rfqs/{id}`, lifecycle transitions |
| Proposals | `POST /api/rfqs/{id}/proposals`, triggers auto-negotiation |
| Rules | CRUD at `/api/suppliers/{id}/negotiation-rules` |

### Auth
Spring Security + JWT. Role-based access control per endpoint.

## Tech Stack

- Java 21, Spring Boot 3.4, Spring Data JPA, PostgreSQL 16
- Spring Security + JWT (jjwt)
- Maven, Flyway, SpringDoc OpenAPI, Lombok, MapStruct
- JUnit 5, Mockito, TestContainers, REST Assured

## MVP Scope

**In:** Company registration, product catalog, catalog orders, RFQs, proposals, negotiation engine, JWT auth, Swagger docs.

**Out (post-MVP):** Frontend UI, payment processing, real-time notifications, file attachments, multi-currency, analytics dashboards.
