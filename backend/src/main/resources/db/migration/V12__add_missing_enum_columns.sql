-- Add missing enum columns to existing tables (idempotent)
-- Root cause: earlier tables existed, and CREATE TABLE IF NOT EXISTS does not add new columns

-- Users.github_account_type
ALTER TABLE IF EXISTS themus.users
  ADD COLUMN IF NOT EXISTS github_account_type themus.github_account_type DEFAULT 'USER';

-- Assessments.statussele
ALTER TABLE IF EXISTS themus.assessments
  ADD COLUMN IF NOT EXISTS status themus.assessment_status DEFAULT 'DRAFT';
-- Ensure not null by backfilling and then enforcing
UPDATE themus.assessments SET status = 'DRAFT' WHERE status IS NULL;
ALTER TABLE themus.assessments ALTER COLUMN status SET NOT NULL;

-- CandidateAttempts.status
ALTER TABLE IF EXISTS themus.candidate_attempts
  ADD COLUMN IF NOT EXISTS status themus.attempt_status DEFAULT 'INVITED';
UPDATE themus.candidate_attempts SET status = 'INVITED' WHERE status IS NULL;
ALTER TABLE themus.candidate_attempts ALTER COLUMN status SET NOT NULL;

-- ChatMessages.message_type
ALTER TABLE IF EXISTS themus.chat_messages
  ADD COLUMN IF NOT EXISTS message_type themus.message_type DEFAULT 'USER';
UPDATE themus.chat_messages SET message_type = 'USER' WHERE message_type IS NULL;
ALTER TABLE themus.chat_messages ALTER COLUMN message_type SET NOT NULL;

-- Jobs.status and job_type
ALTER TABLE IF EXISTS themus.jobs
  ADD COLUMN IF NOT EXISTS status themus.job_status,
  ADD COLUMN IF NOT EXISTS job_type themus.job_type;
