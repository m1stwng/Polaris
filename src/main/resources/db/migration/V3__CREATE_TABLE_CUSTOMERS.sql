CREATE TABLE IF NOT EXISTS customers
(
    id            UUID PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),

    name          VARCHAR(50)  NOT NULL,
    surname       VARCHAR(150) NOT NULL,
    phone_number  VARCHAR(30),
    date_of_birth DATE         NOT NULL,

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),

    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID REFERENCES users(id),

    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES users(id)
);
