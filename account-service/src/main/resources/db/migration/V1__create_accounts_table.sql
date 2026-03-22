CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE account_type   AS ENUM ('CHECKING', 'SAVINGS', 'CREDIT');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'CLOSED', 'SUSPENDED', 'PENDING_REVIEW');
CREATE TYPE currency_code  AS ENUM ('USD', 'EUR', 'GBP', 'RUB', 'KZT');

CREATE TABLE accounts (
    id                        UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_number            VARCHAR(16)    NOT NULL UNIQUE,
    user_id                   UUID           NOT NULL,
    type                      account_type   NOT NULL,
    status                    account_status NOT NULL DEFAULT 'ACTIVE',
    name                      VARCHAR(100)   NOT NULL,
    balance_amount            NUMERIC(19,4)  NOT NULL DEFAULT 0,
    balance_currency          currency_code  NOT NULL,
    reserved_balance_amount   NUMERIC(19,4)  NOT NULL DEFAULT 0,
    reserved_balance_currency currency_code  NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    closed_at                 TIMESTAMPTZ,
    version                   BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounts_user_id        ON accounts(user_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status         ON accounts(status);
CREATE INDEX idx_accounts_user_status    ON accounts(user_id, status);
