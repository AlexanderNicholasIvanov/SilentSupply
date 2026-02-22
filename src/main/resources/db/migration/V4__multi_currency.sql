-- Add currency column to existing tables (default USD for backward compatibility)
ALTER TABLE products ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE catalog_orders ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE rfqs ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE proposals ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE negotiation_rules ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';

-- Exchange rates table
CREATE TABLE exchange_rates (
    id              BIGSERIAL       PRIMARY KEY,
    from_currency   VARCHAR(3)      NOT NULL,
    to_currency     VARCHAR(3)      NOT NULL,
    rate            NUMERIC(18, 8)  NOT NULL,
    effective_date  DATE            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_exchange_rate UNIQUE (from_currency, to_currency, effective_date)
);

CREATE INDEX idx_exchange_rates_pair ON exchange_rates (from_currency, to_currency, effective_date DESC);

-- Seed initial exchange rates (approximate rates)
INSERT INTO exchange_rates (from_currency, to_currency, rate, effective_date) VALUES
    ('USD', 'EUR', 0.92000000, '2026-01-01'),
    ('EUR', 'USD', 1.08700000, '2026-01-01'),
    ('USD', 'GBP', 0.79000000, '2026-01-01'),
    ('GBP', 'USD', 1.26580000, '2026-01-01'),
    ('USD', 'JPY', 149.50000000, '2026-01-01'),
    ('JPY', 'USD', 0.00669000, '2026-01-01'),
    ('USD', 'CAD', 1.36000000, '2026-01-01'),
    ('CAD', 'USD', 0.73530000, '2026-01-01'),
    ('USD', 'AUD', 1.53000000, '2026-01-01'),
    ('AUD', 'USD', 0.65360000, '2026-01-01'),
    ('EUR', 'GBP', 0.85870000, '2026-01-01'),
    ('GBP', 'EUR', 1.16460000, '2026-01-01');
