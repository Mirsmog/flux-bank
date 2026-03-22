CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE card_type AS ENUM ('VIRTUAL', 'PHYSICAL');
CREATE TYPE card_status AS ENUM ('ACTIVE', 'BLOCKED', 'EXPIRED', 'CLOSED');

CREATE TABLE cards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL,
    user_id UUID NOT NULL,
    type card_type NOT NULL,
    status card_status NOT NULL DEFAULT 'ACTIVE',
    masked_pan VARCHAR(19) NOT NULL,
    last_four_digits VARCHAR(4) NOT NULL,
    cvv_hash VARCHAR(60) NOT NULL,
    expiry_month SMALLINT NOT NULL CHECK (expiry_month BETWEEN 1 AND 12),
    expiry_year SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_status ON cards(status);
