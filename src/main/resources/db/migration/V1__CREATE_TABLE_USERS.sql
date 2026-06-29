CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY      DEFAULT GEN_RANDOM_UUID(),

    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL CHECK (role IN ('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_CUSTOMER')),

    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),

    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID REFERENCES users(id),

    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES users(id)
);
