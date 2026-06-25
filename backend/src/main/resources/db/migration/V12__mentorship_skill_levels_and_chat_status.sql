ALTER TABLE user_skills
    ADD COLUMN IF NOT EXISTS self_level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    ADD COLUMN IF NOT EXISTS self_score INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS adjusted_score DOUBLE PRECISION NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS adjusted_level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER';

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS mentorship_request_id UUID,
    ADD COLUMN IF NOT EXISTS skill_id UUID,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP,
    ADD CONSTRAINT fk_conversation_mentorship_request
        FOREIGN KEY (mentorship_request_id) REFERENCES mentorship_requests(id),
    ADD CONSTRAINT fk_conversation_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id);

ALTER TABLE mentor_reviews
    DROP CONSTRAINT IF EXISTS unique_mentor_reviewer,
    ADD COLUMN IF NOT EXISTS skill_id UUID,
    ADD COLUMN IF NOT EXISTS skill_level_rating VARCHAR(20),
    ADD COLUMN IF NOT EXISTS mentorship_request_id UUID,
    ADD CONSTRAINT fk_review_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id),
    ADD CONSTRAINT fk_review_mentorship_request
        FOREIGN KEY (mentorship_request_id) REFERENCES mentorship_requests(id);

ALTER TABLE mentor_reviews
    ADD CONSTRAINT unique_review_mentorship_request UNIQUE (mentorship_request_id);
