-- Rollback migration for complete schema
-- This migration drops all tables and types created in v2_0__create_complete_schema.sql

-- Drop tables in reverse order (respecting foreign key constraints)
DROP TABLE IF EXISTS themus.open_ai_tool_calls CASCADE;
DROP TABLE IF EXISTS themus.open_ai_tool_call CASCADE;
DROP TABLE IF EXISTS themus.openai_tool_calls CASCADE;
DROP TABLE IF EXISTS themus.open_ai_tool_calls CASCADE;
DROP TABLE IF EXISTS themus.chat_message CASCADE;
DROP TABLE IF EXISTS themus.jobs CASCADE;
DROP TABLE IF EXISTS themus.refresh_tokens CASCADE;
DROP TABLE IF EXISTS themus.refresh_token CASCADE;
DROP TABLE IF EXISTS themus.evaluation_metadata CASCADE;
DROP TABLE IF EXISTS themus.evaluations CASCADE;
DROP TABLE IF EXISTS themus.candidate_attempts CASCADE;
DROP TABLE IF EXISTS themus.candidate_assessments CASCADE;
DROP TABLE IF EXISTS themus.candidate_metadata CASCADE;
DROP TABLE IF EXISTS themus.candidates CASCADE;
DROP TABLE IF EXISTS themus.assessment_metadata CASCADE;
DROP TABLE IF EXISTS themus.assessment_language_options CASCADE;
DROP TABLE IF EXISTS themus.assessment_skills CASCADE;
DROP TABLE IF EXISTS themus.assessments CASCADE;
DROP TABLE IF EXISTS themus.users CASCADE;

-- Drop enum types
DROP TYPE IF EXISTS message_type CASCADE;
DROP TYPE IF EXISTS job_type CASCADE;
DROP TYPE IF EXISTS job_status CASCADE;
DROP TYPE IF EXISTS github_account_type CASCADE;
DROP TYPE IF EXISTS attempt_status CASCADE;
DROP TYPE IF EXISTS assessment_status CASCADE;