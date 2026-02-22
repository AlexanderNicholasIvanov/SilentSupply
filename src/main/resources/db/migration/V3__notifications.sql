-- V3__notifications.sql
-- Real-time notification support

CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    recipient_id    BIGINT          NOT NULL REFERENCES companies(id),
    type            VARCHAR(50)     NOT NULL,
    message         TEXT            NOT NULL,
    reference_id    BIGINT,
    reference_type  VARCHAR(20)     CHECK (reference_type IN ('ORDER', 'RFQ', 'PROPOSAL')),
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient_unread ON notifications (recipient_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_recipient ON notifications (recipient_id);
