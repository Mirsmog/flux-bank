CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE transaction_event_type AS ENUM (
    'DEPOSIT', 'WITHDRAWAL', 'TRANSFER_DEBIT', 'TRANSFER_CREDIT', 'FEE', 'REVERSAL'
);
CREATE TYPE transaction_status AS ENUM ('COMPLETED', 'FAILED', 'REVERSED');
CREATE TYPE ledger_entry_type   AS ENUM ('DEBIT', 'CREDIT');

CREATE TABLE transaction_events (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id              UUID                    NOT NULL,
    counterpart_account_id  UUID,
    correlation_id          UUID                    NOT NULL,
    event_type              transaction_event_type  NOT NULL,
    status                  transaction_status      NOT NULL DEFAULT 'COMPLETED',
    amount                  NUMERIC(19,4)           NOT NULL,
    currency                VARCHAR(3)              NOT NULL,
    balance_after           NUMERIC(19,4)           NOT NULL,
    description             VARCHAR(500),
    reference_id            VARCHAR(100),
    metadata                TEXT,
    occurred_at             TIMESTAMPTZ             NOT NULL,
    created_at              TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_events_account_id        ON transaction_events(account_id);
CREATE INDEX idx_tx_events_correlation_id    ON transaction_events(correlation_id);
CREATE INDEX idx_tx_events_occurred_at       ON transaction_events(occurred_at DESC);
CREATE INDEX idx_tx_events_account_occurred  ON transaction_events(account_id, occurred_at DESC);

CREATE TABLE ledger_entries (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_event_id  UUID              NOT NULL REFERENCES transaction_events(id),
    account_id            UUID              NOT NULL,
    entry_type            ledger_entry_type NOT NULL,
    amount                NUMERIC(19,4)     NOT NULL,
    currency              VARCHAR(3)        NOT NULL,
    correlation_id        UUID              NOT NULL,
    description           VARCHAR(500),
    occurred_at           TIMESTAMPTZ       NOT NULL
);

CREATE INDEX idx_ledger_account_id       ON ledger_entries(account_id);
CREATE INDEX idx_ledger_correlation_id   ON ledger_entries(correlation_id);
CREATE INDEX idx_ledger_account_occurred ON ledger_entries(account_id, occurred_at DESC);
