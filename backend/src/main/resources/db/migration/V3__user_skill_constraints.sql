ALTER TABLE user_skills
    ADD CONSTRAINT uk_user_skill
        UNIQUE(user_id, skill_id);