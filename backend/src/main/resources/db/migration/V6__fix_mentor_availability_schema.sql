-- Fix mentor_availability table schema to match updated entity model

-- Convert day_of_week from INTEGER to VARCHAR
ALTER TABLE mentor_availability
    ALTER COLUMN day_of_week TYPE VARCHAR(50);

-- Ensure start_time and end_time are VARCHAR for time strings
ALTER TABLE mentor_availability
    ALTER COLUMN start_time TYPE VARCHAR(50)
    USING start_time::VARCHAR;

ALTER TABLE mentor_availability
    ALTER COLUMN end_time TYPE VARCHAR(50)
    USING end_time::VARCHAR;

-- Remove timezone column if it exists
ALTER TABLE mentor_availability
    DROP COLUMN IF EXISTS timezone;

-- Add skill_id to mentorship_requests if it doesn't exist
ALTER TABLE mentorship_requests
    ADD COLUMN IF NOT EXISTS skill_id UUID;

-- Add foreign key constraint for skill_id
ALTER TABLE mentorship_requests
    ADD CONSTRAINT fk_mentorship_requests_skill FOREIGN KEY (skill_id)
    REFERENCES skills(id) ON DELETE SET NULL;

-- Add category and description to skills if they don't exist
ALTER TABLE skills
    ADD COLUMN IF NOT EXISTS category VARCHAR(100);

ALTER TABLE skills
    ADD COLUMN IF NOT EXISTS description TEXT;

-- Add experience, role, linkedIn, and github to users if they don't exist
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS experience_years INTEGER;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role_title VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(255);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS github_url VARCHAR(255);
