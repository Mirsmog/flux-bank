CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE kyc_status AS ENUM ('NOT_STARTED', 'PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE document_type AS ENUM ('PASSPORT', 'NATIONAL_ID', 'DRIVERS_LICENSE');

CREATE TABLE kyc_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    status kyc_status NOT NULL DEFAULT 'PENDING',
    document_type document_type NOT NULL,
    document_number_hash VARCHAR(64) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    rejection_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_kyc_user_id ON kyc_records(user_id);
CREATE INDEX idx_kyc_status ON kyc_records(status);
