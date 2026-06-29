CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id         UUID PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),

    token      UUID        NOT NULL UNIQUE,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);
