CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE payment_status AS ENUM (
    'INITIATED', 'VALIDATING', 'DEBITED', 'CREDITED',
    'COMPLETED', 'FAILED', 'COMPENSATION_PENDING', 'COMPENSATED'
);
CREATE TYPE payment_type AS ENUM ('P2P_TRANSFER', 'DEPOSIT', 'WITHDRAWAL');

CREATE TABLE payments (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    idempotency_key             VARCHAR(64)    NOT NULL UNIQUE,
    sender_id                   UUID           NOT NULL,
    sender_account_id           UUID           NOT NULL,
    receiver_account_id         UUID           NOT NULL,
    amount                      NUMERIC(19,4)  NOT NULL,
    currency                    VARCHAR(3)     NOT NULL,
    type                        payment_type   NOT NULL,
    status                      payment_status NOT NULL DEFAULT 'INITIATED',
    description                 VARCHAR(500),
    debit_transaction_id        UUID,
    credit_transaction_id       UUID,
    compensation_transaction_id UUID,
    failure_reason              VARCHAR(500),
    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    completed_at                TIMESTAMPTZ,
    version                     BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_payments_sender_id        ON payments(sender_id);
CREATE INDEX idx_payments_idempotency_key  ON payments(idempotency_key);
CREATE INDEX idx_payments_status           ON payments(status);
CREATE INDEX idx_payments_sender_account   ON payments(sender_account_id);

CREATE TABLE outbox_events (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id  VARCHAR(36)  NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    topic         VARCHAR(200) NOT NULL,
    payload       TEXT         NOT NULL,
    published     BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(published, created_at) WHERE published = FALSE;
