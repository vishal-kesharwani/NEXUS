ALTER TABLE meetings
    ADD COLUMN IF NOT EXISTS meet_link_expires_at TIMESTAMP;
