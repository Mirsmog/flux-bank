CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE notification_channel AS ENUM ('EMAIL', 'PUSH', 'SMS');
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'FAILED');

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    channel notification_channel NOT NULL DEFAULT 'EMAIL',
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status notification_status NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
