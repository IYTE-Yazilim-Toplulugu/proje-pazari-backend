CREATE TABLE application_messages (
    id VARCHAR(26) PRIMARY KEY,
    application_id VARCHAR(26) NOT NULL,
    sender_id VARCHAR(26) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_messages_application
        FOREIGN KEY (application_id) REFERENCES project_applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_application_messages_body_length
        CHECK (CHAR_LENGTH(body) <= 2000),
    CONSTRAINT ck_application_messages_body_not_blank
        CHECK (body ~ '[^[:space:]]')
);

CREATE INDEX idx_application_messages_thread_order
    ON application_messages (application_id, created_at, id);
