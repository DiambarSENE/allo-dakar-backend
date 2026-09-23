CREATE TABLE payments (
    id                  UUID PRIMARY KEY,
    payment_reference   VARCHAR(30) NOT NULL,
    booking_id          UUID NOT NULL REFERENCES bookings(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    amount              NUMERIC(10,2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'XOF',
    payment_method      VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id      VARCHAR(120),
    paid_at             TIMESTAMP,
    failed_at           TIMESTAMP,
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    CONSTRAINT uk_payments_reference UNIQUE (payment_reference),
    CONSTRAINT chk_payments_amount CHECK (amount >= 0),
    CONSTRAINT chk_payments_method CHECK (payment_method IN
        ('CASH','CARD','MOBILE_MONEY','BANK_TRANSFER','OTHER')),
    CONSTRAINT chk_payments_status CHECK (status IN
        ('PENDING','PROCESSING','SUCCESS','FAILED','CANCELLED','REFUNDED'))
);

CREATE INDEX idx_payments_booking ON payments (booking_id);
CREATE INDEX idx_payments_user ON payments (user_id);
