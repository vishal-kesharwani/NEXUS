CREATE TABLE conversations (
                               id UUID PRIMARY KEY,
                               mentor_id UUID NOT NULL,
                               mentee_id UUID NOT NULL,
                               created_at TIMESTAMP NOT NULL,

                               CONSTRAINT fk_conversation_mentor
                                   FOREIGN KEY (mentor_id)
                                       REFERENCES users(id),

                               CONSTRAINT fk_conversation_mentee
                                   FOREIGN KEY (mentee_id)
                                       REFERENCES users(id)
);

CREATE TABLE messages (
                          id UUID PRIMARY KEY,
                          conversation_id UUID NOT NULL,
                          sender_id UUID NOT NULL,
                          content TEXT NOT NULL,
                          sent_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_message_conversation
                              FOREIGN KEY (conversation_id)
                                  REFERENCES conversations(id),

                          CONSTRAINT fk_message_sender
                              FOREIGN KEY (sender_id)
                                  REFERENCES users(id)
);