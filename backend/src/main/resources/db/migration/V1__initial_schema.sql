CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,

                       first_name VARCHAR(100),
                       last_name VARCHAR(100),

                       headline VARCHAR(255),
                       bio TEXT,

                       created_at TIMESTAMP DEFAULT NOW(),
                       updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE skills (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                        name VARCHAR(100) UNIQUE NOT NULL,
                        slug VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE user_skills (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                             user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,

                             years_exp INTEGER DEFAULT 0,
                             can_mentor BOOLEAN DEFAULT FALSE
);