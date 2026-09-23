CREATE TABLE driver_verifications (
    id                UUID PRIMARY KEY,
    driver_id         UUID NOT NULL REFERENCES driver_profiles(id) ON DELETE CASCADE,
    document_type     VARCHAR(30) NOT NULL,
    document_number   VARCHAR(80),
    document_url      VARCHAR(1000) NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at      TIMESTAMP NOT NULL,
    reviewed_at       TIMESTAMP,
    reviewed_by       UUID REFERENCES users(id),
    rejection_reason  VARCHAR(500),
    comment           VARCHAR(500),
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,
    CONSTRAINT chk_driver_verifications_status CHECK (status IN
        ('SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','REQUIRES_MORE_INFORMATION'))
);

CREATE INDEX idx_driver_verifications_driver ON driver_verifications (driver_id);
