ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS mentor_last_read_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS mentee_last_read_at TIMESTAMP;
