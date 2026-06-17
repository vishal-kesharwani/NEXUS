CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE meetings (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    created_by UUID NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    meet_link VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_meeting_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_meeting_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE mentor_reviews (
    id UUID PRIMARY KEY,
    mentor_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    rating NUMERIC(3,2) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_review_mentor FOREIGN KEY (mentor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_mentor_reviewer UNIQUE (mentor_id, reviewer_id)
);

;