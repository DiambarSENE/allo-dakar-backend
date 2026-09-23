CREATE TABLE trips (
    id                     UUID PRIMARY KEY,
    driver_id              UUID NOT NULL REFERENCES driver_profiles(id),
    vehicle_id             UUID NOT NULL REFERENCES vehicles(id),
    departure_city         VARCHAR(100) NOT NULL,
    departure_address      VARCHAR(255),
    departure_latitude     DOUBLE PRECISION,
    departure_longitude    DOUBLE PRECISION,
    destination_city       VARCHAR(100) NOT NULL,
    destination_address    VARCHAR(255),
    destination_latitude   DOUBLE PRECISION,
    destination_longitude  DOUBLE PRECISION,
    departure_date         DATE NOT NULL,
    departure_time         TIME NOT NULL,
    meeting_point          VARCHAR(255),
    price_per_seat         NUMERIC(10,2) NOT NULL,
    available_seats        INTEGER NOT NULL,
    total_seats            INTEGER NOT NULL,
    description            VARCHAR(1000),
    rules                  VARCHAR(1000),
    status                 VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at           TIMESTAMP,
    version                BIGINT NOT NULL DEFAULT 0,
    created_at             TIMESTAMP NOT NULL,
    updated_at             TIMESTAMP NOT NULL,
    CONSTRAINT chk_trips_price CHECK (price_per_seat >= 0),
    CONSTRAINT chk_trips_seats CHECK (total_seats > 0 AND available_seats >= 0 AND available_seats <= total_seats),
    CONSTRAINT chk_trips_status CHECK (status IN
        ('DRAFT','PUBLISHED','FULL','IN_PROGRESS','COMPLETED','CANCELLED','EXPIRED'))
);

CREATE INDEX idx_trips_search ON trips (departure_city, destination_city, departure_date, status);
CREATE INDEX idx_trips_driver ON trips (driver_id);

CREATE TABLE meeting_points (
    id          UUID PRIMARY KEY,
    trip_id     UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    name        VARCHAR(120) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(100),
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    description VARCHAR(500),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

CREATE INDEX idx_meeting_points_trip ON meeting_points (trip_id);
