-- Complete database schema migration for Themus application
-- This migration creates all tables, relationships, and constraints
CREATE SCHEMA IF NOT EXISTS themus;

-- Create enum types
CREATE TYPE assessment_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'PUBLISHED');
CREATE TYPE attempt_status AS ENUM ('INVITED', 'STARTED', 'COMPLETED', 'EVALUATED', 'EXPIRED');
CREATE TYPE github_account_type AS ENUM ('USER', 'ORG');
CREATE TYPE job_status AS ENUM ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED');
CREATE TYPE job_type AS ENUM ('CREATE_ASSESSMENT', 'CHAT_COMPLETION', 'SEND_EMAIL', 'CANDIDATE_INVITATION');
CREATE TYPE message_type AS ENUM ('USER', 'ASSISTANT', 'SYSTEM', 'FUNCTION');

-- Create users table
CREATE TABLE IF NOT EXISTS themus.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    organization_name VARCHAR(200) NOT NULL,
    github_access_token TEXT,
    github_username VARCHAR(255),
    github_account_type github_account_type DEFAULT 'USER',
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create assessments table
CREATE TABLE IF NOT EXISTS themus.assessments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    details TEXT,
    role VARCHAR(100) NOT NULL,
    status assessment_status NOT NULL DEFAULT 'DRAFT',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    duration INTEGER NOT NULL,
    github_repository_link TEXT NOT NULL,
    github_repo_name TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES themus.users(id) ON DELETE CASCADE
);

-- Create assessment_skills table (ElementCollection)
CREATE TABLE IF NOT EXISTS themus.assessment_skills (
    assessment_id BIGINT NOT NULL REFERENCES themus.assessments(id) ON DELETE CASCADE,
    skill VARCHAR(255) NOT NULL,
    PRIMARY KEY (assessment_id, skill)
);

-- Create assessment_language_options table (ElementCollection)
CREATE TABLE IF NOT EXISTS themus.assessment_language_options (
    assessment_id BIGINT NOT NULL REFERENCES themus.assessments(id) ON DELETE CASCADE,
    language_option VARCHAR(255) NOT NULL,
    PRIMARY KEY (assessment_id, language_option)
);

-- Create assessment_metadata table (ElementCollection)
CREATE TABLE IF NOT EXISTS themus.assessment_metadata (
    assessment_id BIGINT NOT NULL REFERENCES themus.assessments(id) ON DELETE CASCADE,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (assessment_id, metadata_key)
);

-- Create candidates table
CREATE TABLE IF NOT EXISTS themus.candidates (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES themus.users(id) ON DELETE CASCADE
);

-- Create candidate_metadata table (ElementCollection)
CREATE TABLE IF NOT EXISTS themus.candidate_metadata (
    candidate_id BIGINT NOT NULL REFERENCES themus.candidates(id) ON DELETE CASCADE,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (candidate_id, metadata_key)
);

-- Create candidate_assessments table (Many-to-Many relationship)
CREATE TABLE IF NOT EXISTS themus.candidate_assessments (
    candidate_id BIGINT NOT NULL REFERENCES themus.candidates(id) ON DELETE CASCADE,
    assessment_id BIGINT NOT NULL REFERENCES themus.assessments(id) ON DELETE CASCADE,
    PRIMARY KEY (candidate_id, assessment_id)
);

-- Create candidate_attempts table
CREATE TABLE IF NOT EXISTS themus.candidate_attempts (
    id BIGSERIAL PRIMARY KEY,
    github_repository_link TEXT,
    status attempt_status NOT NULL DEFAULT 'INVITED',
    language_choice VARCHAR(100),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_date TIMESTAMP,
    completed_date TIMESTAMP,
    evaluated_date TIMESTAMP,
    candidate_id BIGINT NOT NULL REFERENCES themus.candidates(id) ON DELETE CASCADE,
    assessment_id BIGINT NOT NULL REFERENCES themus.assessments(id) ON DELETE CASCADE
);

-- Create evaluations table
CREATE TABLE IF NOT EXISTS themus.evaluations (
    id BIGSERIAL PRIMARY KEY,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    candidate_attempt_id BIGINT NOT NULL REFERENCES themus.candidate_attempts(id) ON DELETE CASCADE UNIQUE
);

-- Create evaluation_metadata table (ElementCollection)
CREATE TABLE IF NOT EXISTS themus.evaluation_metadata (
    evaluation_id BIGINT NOT NULL REFERENCES themus.evaluations(id) ON DELETE CASCADE,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (evaluation_id, metadata_key)
);

-- Create chat_message table
CREATE TABLE IF NOT EXISTS themus.chat_message (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    text TEXT NOT NULL,
    model VARCHAR(255),
    assessment_id BIGINT NOT NULL REFERENCES themus.assessments(id) ON DELETE CASCADE,
    message_type message_type NOT NULL
);

-- Create open_ai_tool_calls table
CREATE TABLE IF NOT EXISTS themus.open_ai_tool_calls (
    id VARCHAR(255) PRIMARY KEY,
    tool_name VARCHAR(255),
    arguments TEXT,
    message_id BIGINT NOT NULL REFERENCES themus.chat_message(id) ON DELETE CASCADE
);

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS themus.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES themus.users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE
);

-- Create jobs table
CREATE TABLE IF NOT EXISTS themus.jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status job_status,
    job_type job_type,
    result TEXT
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON themus.users(email);
CREATE INDEX IF NOT EXISTS idx_users_github_username ON themus.users(github_username);
CREATE INDEX IF NOT EXISTS idx_assessments_user_id ON themus.assessments(user_id);
CREATE INDEX IF NOT EXISTS idx_assessments_status ON themus.assessments(status);
CREATE INDEX IF NOT EXISTS idx_assessments_created_date ON themus.assessments(created_date);
CREATE INDEX IF NOT EXISTS idx_candidates_user_id ON themus.candidates(user_id);
CREATE INDEX IF NOT EXISTS idx_candidates_email ON themus.candidates(email);
CREATE INDEX IF NOT EXISTS idx_candidate_attempts_candidate_id ON themus.candidate_attempts(candidate_id);
CREATE INDEX IF NOT EXISTS idx_candidate_attempts_assessment_id ON themus.candidate_attempts(assessment_id);
CREATE INDEX IF NOT EXISTS idx_candidate_attempts_status ON themus.candidate_attempts(status);
CREATE INDEX IF NOT EXISTS idx_candidate_attempts_created_date ON themus.candidate_attempts(created_date);
CREATE INDEX IF NOT EXISTS idx_evaluations_candidate_attempt_id ON themus.evaluations(candidate_attempt_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_assessment_id ON themus.chat_message(assessment_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_created_at ON themus.chat_message(created_at);
CREATE INDEX IF NOT EXISTS idx_open_ai_tool_calls_message_id ON themus.open_ai_tool_calls(message_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON themus.refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON themus.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON themus.jobs(status);

-- Create constraints for data integrity
ALTER TABLE themus.candidate_attempts 
ADD CONSTRAINT chk_completed_after_started 
CHECK (completed_date IS NULL OR started_date IS NULL OR completed_date > started_date);

ALTER TABLE themus.candidate_attempts 
ADD CONSTRAINT chk_evaluated_after_completed 
CHECK (evaluated_date IS NULL OR completed_date IS NULL OR evaluated_date > completed_date);

ALTER TABLE themus.assessments 
ADD CONSTRAINT chk_end_date_after_start_date 
CHECK (end_date IS NULL OR start_date IS NULL OR end_date > start_date);

ALTER TABLE themus.assessments 
ADD CONSTRAINT chk_duration_positive 
CHECK (duration > 0);

-- Add comments for documentation
COMMENT ON TABLE themus.users IS 'Stores user account information including GitHub credentials';
COMMENT ON TABLE themus.assessments IS 'Stores assessment templates created by users';
COMMENT ON TABLE themus.candidates IS 'Stores candidate information for assessments';
COMMENT ON TABLE themus.candidate_attempts IS 'Stores individual candidate attempts at assessments';
COMMENT ON TABLE themus.evaluations IS 'Stores evaluation results for candidate attempts';
COMMENT ON TABLE themus.chat_message IS 'Stores chat messages for AI interactions';
COMMENT ON TABLE themus.open_ai_tool_calls IS 'Stores OpenAI tool calls associated with chat messages';
COMMENT ON TABLE themus.refresh_tokens IS 'Stores refresh tokens for authentication';
COMMENT ON TABLE themus.jobs IS 'Stores background job information';
