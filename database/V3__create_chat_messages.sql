-- Chat Messages Table
-- Stores all chat messages sent through the ChatMe feature.
-- Hibernate ddl-auto=update will auto-create this, but this script is provided
-- for manual migration / documentation purposes.

CREATE TABLE IF NOT EXISTS chat_messages (
    id              BIGSERIAL PRIMARY KEY,
    sender_id       INTEGER NOT NULL REFERENCES users(user_id),
    sender_name     VARCHAR(100),
    sender_email    VARCHAR(100),
    message_type    VARCHAR(10) NOT NULL CHECK (message_type IN ('text', 'image', 'audio')),
    content         TEXT,
    media_url       TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender ON chat_messages(sender_id);
