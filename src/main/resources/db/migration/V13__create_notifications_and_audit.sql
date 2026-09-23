CREATE TABLE notifications (
    id             UUID PRIMARY KEY,
    recipient_id   UUID NOT NULL REFERENCES users(id),
    type           VARCHAR(40) NOT NULL,
    channel        VARCHAR(20) NOT NULL,
    title          VARCHAR(150) NOT NULL,
    message        VARCHAR(1000) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at        TIMESTAMP,
    read_at        TIMESTAMP,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_recipient ON notifications (recipient_id, status);

CREATE TABLE audit_logs (
    id           UUID PRIMARY KEY,
    actor_id     UUID REFERENCES users(id),
    entity_type  VARCHAR(60) NOT NULL,
    entity_id    UUID NOT NULL,
    action       VARCHAR(60) NOT NULL,
    details      TEXT,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_id);
