CREATE TABLE bookings (
    id                   UUID PRIMARY KEY,
    booking_reference    VARCHAR(30) NOT NULL,
    trip_id              UUID NOT NULL REFERENCES trips(id),
    passenger_id         UUID NOT NULL REFERENCES users(id),
    number_of_seats      INTEGER NOT NULL,
    unit_price           NUMERIC(10,2) NOT NULL,
    total_amount         NUMERIC(10,2) NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    booked_at            TIMESTAMP NOT NULL,
    confirmed_at         TIMESTAMP,
    cancelled_at         TIMESTAMP,
    completed_at         TIMESTAMP,
    cancellation_reason  VARCHAR(500),
    created_at           TIMESTAMP NOT NULL,
    updated_at           TIMESTAMP NOT NULL,
    CONSTRAINT uk_bookings_reference UNIQUE (booking_reference),
    CONSTRAINT chk_bookings_seats CHECK (number_of_seats > 0),
    CONSTRAINT chk_bookings_status CHECK (status IN
        ('PENDING','CONFIRMED','CANCELLED','REJECTED','COMPLETED','EXPIRED')),
    CONSTRAINT chk_bookings_payment_status CHECK (payment_status IN
        ('PENDING','PROCESSING','SUCCESS','FAILED','CANCELLED','REFUNDED'))
);

CREATE INDEX idx_bookings_passenger ON bookings (passenger_id);
CREATE INDEX idx_bookings_trip ON bookings (trip_id);
