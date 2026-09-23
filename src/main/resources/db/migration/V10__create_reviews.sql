CREATE TABLE reviews (
    id              UUID PRIMARY KEY,
    trip_id         UUID NOT NULL REFERENCES trips(id),
    booking_id      UUID NOT NULL REFERENCES bookings(id),
    author_id       UUID NOT NULL REFERENCES users(id),
    target_user_id  UUID NOT NULL REFERENCES users(id),
    rating          INTEGER NOT NULL,
    comment         VARCHAR(1000),
    status          VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    CONSTRAINT uk_reviews_trip_author_target UNIQUE (trip_id, author_id, target_user_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_reviews_status CHECK (status IN ('PENDING','PUBLISHED','HIDDEN','REJECTED'))
);

CREATE INDEX idx_reviews_target ON reviews (target_user_id);
