-- Conversations table
CREATE TABLE conversations (
    id              BIGSERIAL       PRIMARY KEY,
    type            VARCHAR(20)     NOT NULL,
    reference_id    BIGINT,
    subject         VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Prevent duplicate scoped conversations for RFQ and ORDER types
CREATE UNIQUE INDEX uq_conversations_scoped
    ON conversations (type, reference_id)
    WHERE type IN ('RFQ', 'ORDER');

CREATE INDEX idx_conversations_type ON conversations (type);

-- Conversation participants (which companies are in a conversation)
CREATE TABLE conversation_participants (
    id              BIGSERIAL       PRIMARY KEY,
    conversation_id BIGINT          NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    company_id      BIGINT          NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    last_read_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_participant UNIQUE (conversation_id, company_id)
);

CREATE INDEX idx_participants_company ON conversation_participants (company_id);
CREATE INDEX idx_participants_conversation ON conversation_participants (conversation_id);

-- Messages (append-only)
CREATE TABLE messages (
    id                  BIGSERIAL   PRIMARY KEY,
    conversation_id     BIGINT      NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_company_id   BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    content             TEXT        NOT NULL,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, created_at);
CREATE INDEX idx_messages_sender ON messages (sender_company_id);
