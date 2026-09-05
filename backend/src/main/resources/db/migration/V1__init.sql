-- Requires PostgreSQL 13+ (gen_random_uuid() is built in, no extension needed).

CREATE TABLE manager (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE lead (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255),
    phone           VARCHAR(32),
    instagram_handle VARCHAR(255),
    budget          VARCHAR(255),
    project_type    VARCHAR(255),
    timeline        VARCHAR(255),
    location        VARCHAR(255),
    funnel_stage    VARCHAR(32) NOT NULL DEFAULT 'CONTACTED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE conversation (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id             UUID NOT NULL REFERENCES lead(id),
    channel             VARCHAR(16) NOT NULL,
    external_thread_id  VARCHAR(255) NOT NULL,
    owner_type          VARCHAR(16) NOT NULL DEFAULT 'BOT',
    owner_manager_id    UUID REFERENCES manager(id),
    status              VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (channel, external_thread_id)
);

CREATE TABLE message (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id     UUID NOT NULL REFERENCES conversation(id),
    sender              VARCHAR(16) NOT NULL,
    content             TEXT NOT NULL,
    external_message_id VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_message_conversation_id ON message(conversation_id);

CREATE TABLE booking (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id         UUID NOT NULL REFERENCES lead(id),
    conversation_id UUID REFERENCES conversation(id),
    scheduled_at    TIMESTAMPTZ NOT NULL,
    google_event_id VARCHAR(255),
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Seed manager account for initial login. Email: admin@studio.local / Password: ChangeMe123!
-- CHANGE THIS PASSWORD IMMEDIATELY after first login — this hash is public (it's in source control).
INSERT INTO manager (email, password_hash, name)
VALUES ('admin@studio.local', '$2b$10$GYWJ2y9Y2NXZbLQ3Inp6yuWHTBphu/hg1.JkTF2uNLYwSUgXFXy7a', 'Admin');
