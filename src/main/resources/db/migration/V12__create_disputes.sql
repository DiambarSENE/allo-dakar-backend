CREATE TABLE disputes (
    id            UUID PRIMARY KEY,
    raised_by_id  UUID NOT NULL REFERENCES users(id),
    booking_id    UUID REFERENCES bookings(id),
    payment_id    UUID REFERENCES payments(id),
    type          VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description   VARCHAR(1000),
    resolution    VARCHAR(1000),
    resolved_at   TIMESTAMP,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,
    CONSTRAINT chk_disputes_status CHECK (status IN ('OPEN','UNDER_REVIEW','RESOLVED','REJECTED','CLOSED'))
);

CREATE TABLE dispute_actions (
    id             UUID PRIMARY KEY,
    dispute_id     UUID NOT NULL REFERENCES disputes(id) ON DELETE CASCADE,
    performed_by   UUID NOT NULL REFERENCES users(id),
    action         VARCHAR(100) NOT NULL,
    note           VARCHAR(1000),
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_dispute_actions_dispute ON dispute_actions (dispute_id);
