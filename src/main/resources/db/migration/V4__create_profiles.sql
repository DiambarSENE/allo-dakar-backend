CREATE TABLE passenger_profiles (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preferences     VARCHAR(500),
    bio             VARCHAR(500),
    average_rating  NUMERIC(3,2) NOT NULL DEFAULT 0,
    total_trips     INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    CONSTRAINT uk_passenger_profiles_user UNIQUE (user_id)
);

CREATE TABLE driver_profiles (
    id                     UUID PRIMARY KEY,
    user_id                UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    license_number         VARCHAR(60) NOT NULL,
    license_expiry_date    DATE NOT NULL,
    verification_status    VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    average_rating         NUMERIC(3,2) NOT NULL DEFAULT 0,
    total_trips            INTEGER NOT NULL DEFAULT 0,
    total_reviews          INTEGER NOT NULL DEFAULT 0,
    created_at             TIMESTAMP NOT NULL,
    updated_at             TIMESTAMP NOT NULL,
    CONSTRAINT uk_driver_profiles_user UNIQUE (user_id),
    CONSTRAINT uk_driver_profiles_license UNIQUE (license_number),
    CONSTRAINT chk_driver_profiles_status CHECK (verification_status IN
        ('PENDING','UNDER_REVIEW','VERIFIED','REJECTED','REQUIRES_MORE_INFORMATION'))
);
