CREATE TABLE vehicles (
    id                    UUID PRIMARY KEY,
    driver_id             UUID NOT NULL REFERENCES driver_profiles(id) ON DELETE CASCADE,
    brand                 VARCHAR(60) NOT NULL,
    model                 VARCHAR(60) NOT NULL,
    registration_number   VARCHAR(30) NOT NULL,
    color                 VARCHAR(30),
    year                  INTEGER,
    number_of_seats       INTEGER NOT NULL,
    vehicle_type          VARCHAR(20) NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at            TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP NOT NULL,
    CONSTRAINT uk_vehicles_registration UNIQUE (registration_number),
    CONSTRAINT chk_vehicles_seats CHECK (number_of_seats > 0),
    CONSTRAINT chk_vehicles_status CHECK (status IN ('ACTIVE','INACTIVE','MAINTENANCE'))
);

CREATE INDEX idx_vehicles_driver ON vehicles (driver_id);
