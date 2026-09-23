CREATE TABLE reports (
    id                UUID PRIMARY KEY,
    reporter_id       UUID NOT NULL REFERENCES users(id),
    reported_user_id  UUID REFERENCES users(id),
    trip_id           UUID REFERENCES trips(id),
    booking_id        UUID REFERENCES bookings(id),
    review_id         UUID REFERENCES reviews(id),
    reason            VARCHAR(30) NOT NULL,
    description       VARCHAR(1000),
    status            VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    handled_by        UUID REFERENCES users(id),
    resolution        VARCHAR(1000),
    resolved_at       TIMESTAMP,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,
    CONSTRAINT chk_reports_status CHECK (status IN ('OPEN','UNDER_REVIEW','RESOLVED','REJECTED','CLOSED'))
);

CREATE INDEX idx_reports_reporter ON reports (reporter_id);
CREATE INDEX idx_reports_status ON reports (status);
