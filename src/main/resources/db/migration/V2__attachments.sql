-- V2__attachments.sql
-- File attachment support for products, orders, and RFQs

CREATE TABLE attachments (
    id              BIGSERIAL       PRIMARY KEY,
    file_name       VARCHAR(255)    NOT NULL,
    content_type    VARCHAR(100)    NOT NULL,
    file_size       BIGINT          NOT NULL CHECK (file_size > 0),
    storage_path    VARCHAR(500)    NOT NULL,
    entity_type     VARCHAR(20)     NOT NULL CHECK (entity_type IN ('PRODUCT', 'ORDER', 'RFQ')),
    entity_id       BIGINT          NOT NULL,
    uploader_id     BIGINT          NOT NULL REFERENCES companies(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_entity ON attachments (entity_type, entity_id);
CREATE INDEX idx_attachments_uploader ON attachments (uploader_id);
