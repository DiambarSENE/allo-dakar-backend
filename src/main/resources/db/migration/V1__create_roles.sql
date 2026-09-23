CREATE TABLE roles (
    id          UUID PRIMARY KEY,
    name        VARCHAR(40) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name)
);
