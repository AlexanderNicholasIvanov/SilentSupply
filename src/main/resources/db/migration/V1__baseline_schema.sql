-- V1__baseline_schema.sql
-- Baseline schema for SilentSupply B2B marketplace

-- ============================================================
-- Companies
-- ============================================================
CREATE TABLE companies (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('SUPPLIER', 'BUYER')),
    contact_phone   VARCHAR(50),
    address         VARCHAR(500),
    verified        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_companies_email ON companies (email);
CREATE INDEX idx_companies_role ON companies (role);

-- ============================================================
-- Products
-- ============================================================
CREATE TABLE products (
    id                  BIGSERIAL       PRIMARY KEY,
    supplier_id         BIGINT          NOT NULL REFERENCES companies(id),
    name                VARCHAR(255)    NOT NULL,
    description         TEXT,
    category            VARCHAR(100)    NOT NULL,
    sku                 VARCHAR(100)    NOT NULL,
    unit_of_measure     VARCHAR(50)     NOT NULL,
    base_price          NUMERIC(15,2)   NOT NULL CHECK (base_price > 0),
    available_quantity  INTEGER         NOT NULL CHECK (available_quantity >= 0),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED')),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (supplier_id, sku)
);

CREATE INDEX idx_products_supplier ON products (supplier_id);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_name ON products (name);

-- ============================================================
-- Catalog Orders (direct purchase at listed price)
-- ============================================================
CREATE TABLE catalog_orders (
    id              BIGSERIAL       PRIMARY KEY,
    buyer_id        BIGINT          NOT NULL REFERENCES companies(id),
    product_id      BIGINT          NOT NULL REFERENCES products(id),
    supplier_id     BIGINT          NOT NULL REFERENCES companies(id),
    quantity        INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(15,2)   NOT NULL CHECK (unit_price > 0),
    total_price     NUMERIC(15,2)   NOT NULL CHECK (total_price > 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PLACED'
                        CHECK (status IN ('PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_catalog_orders_buyer ON catalog_orders (buyer_id);
CREATE INDEX idx_catalog_orders_supplier ON catalog_orders (supplier_id);
CREATE INDEX idx_catalog_orders_product ON catalog_orders (product_id);
CREATE INDEX idx_catalog_orders_status ON catalog_orders (status);

-- ============================================================
-- RFQs (Request for Quote)
-- ============================================================
CREATE TABLE rfqs (
    id                  BIGSERIAL       PRIMARY KEY,
    buyer_id            BIGINT          NOT NULL REFERENCES companies(id),
    product_id          BIGINT          NOT NULL REFERENCES products(id),
    supplier_id         BIGINT          NOT NULL REFERENCES companies(id),
    desired_quantity    INTEGER         NOT NULL CHECK (desired_quantity > 0),
    target_price        NUMERIC(15,2)   NOT NULL CHECK (target_price > 0),
    delivery_deadline   DATE            NOT NULL,
    notes               TEXT,
    status              VARCHAR(20)     NOT NULL DEFAULT 'SUBMITTED'
                            CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'COUNTERED',
                                              'ACCEPTED', 'REJECTED', 'EXPIRED')),
    current_round       INTEGER         NOT NULL DEFAULT 0,
    max_rounds          INTEGER         NOT NULL DEFAULT 3,
    expires_at          TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rfqs_buyer ON rfqs (buyer_id);
CREATE INDEX idx_rfqs_supplier ON rfqs (supplier_id);
CREATE INDEX idx_rfqs_product ON rfqs (product_id);
CREATE INDEX idx_rfqs_status ON rfqs (status);

-- ============================================================
-- Proposals (offers within an RFQ negotiation)
-- ============================================================
CREATE TABLE proposals (
    id              BIGSERIAL       PRIMARY KEY,
    rfq_id          BIGINT          NOT NULL REFERENCES rfqs(id),
    proposer_type   VARCHAR(20)     NOT NULL CHECK (proposer_type IN ('BUYER', 'SYSTEM')),
    proposed_price  NUMERIC(15,2)   NOT NULL CHECK (proposed_price > 0),
    proposed_qty    INTEGER         NOT NULL CHECK (proposed_qty > 0),
    delivery_days   INTEGER         NOT NULL CHECK (delivery_days > 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COUNTERED', 'EXPIRED')),
    round_number    INTEGER         NOT NULL,
    reason_code     VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_proposals_rfq ON proposals (rfq_id);
CREATE INDEX idx_proposals_status ON proposals (status);

-- ============================================================
-- Negotiation Rules (supplier-defined per product)
-- ============================================================
CREATE TABLE negotiation_rules (
    id                      BIGSERIAL       PRIMARY KEY,
    supplier_id             BIGINT          NOT NULL REFERENCES companies(id),
    product_id              BIGINT          NOT NULL REFERENCES products(id),
    price_floor             NUMERIC(15,2)   NOT NULL CHECK (price_floor > 0),
    auto_accept_threshold   NUMERIC(15,2)   NOT NULL CHECK (auto_accept_threshold > 0),
    max_delivery_days       INTEGER         NOT NULL CHECK (max_delivery_days > 0),
    max_rounds              INTEGER         NOT NULL DEFAULT 3 CHECK (max_rounds > 0),
    volume_discount_pct     NUMERIC(5,2)    NOT NULL DEFAULT 0 CHECK (volume_discount_pct >= 0 AND volume_discount_pct <= 100),
    volume_threshold        INTEGER         NOT NULL DEFAULT 0 CHECK (volume_threshold >= 0),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (supplier_id, product_id)
);

CREATE INDEX idx_negotiation_rules_supplier ON negotiation_rules (supplier_id);
CREATE INDEX idx_negotiation_rules_product ON negotiation_rules (product_id);
