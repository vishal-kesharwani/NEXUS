CREATE TABLE mentor_availability (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                     user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                                     day_of_week INTEGER NOT NULL,
                                     start_time TIME NOT NULL,
                                     end_time TIME NOT NULL,

                                     timezone VARCHAR(100) NOT NULL,

                                     created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE mentorship_requests (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                     mentor_id UUID NOT NULL REFERENCES users(id),
                                     mentee_id UUID NOT NULL REFERENCES users(id),

                                     status VARCHAR(20) DEFAULT 'PENDING',

                                     message TEXT,

                                     created_at TIMESTAMP DEFAULT NOW()
);