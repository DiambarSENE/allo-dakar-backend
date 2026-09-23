CREATE TABLE users (
    id                UUID PRIMARY KEY,
    first_name        VARCHAR(80) NOT NULL,
    last_name         VARCHAR(80) NOT NULL,
    email             VARCHAR(180) NOT NULL,
    phone             VARCHAR(20) NOT NULL,
    password          VARCHAR(255) NOT NULL,
    date_of_birth     DATE,
    profile_picture   VARCHAR(500),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified    BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified    BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at     TIMESTAMP,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','BANNED','PENDING'))
);

CREATE INDEX idx_users_status ON users (status);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);
