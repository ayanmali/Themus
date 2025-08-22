-- Complete database schema migration for Themus application
-- This migration creates all tables, relationships, and constraints
-- Ensure pgcrypto is available for gen_random_uuid()
--CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create openai_tool_responses table
CREATE TABLE IF NOT EXISTS themus.openai_tool_responses (
    id VARCHAR(255) PRIMARY KEY,
    tool_name VARCHAR(255),
    response_data TEXT,
    message_id BIGINT NOT NULL REFERENCES themus.chat_messages(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_openai_tool_responses_message_id ON themus.openai_tool_responses(message_id);

COMMENT ON TABLE themus.openai_tool_calls IS 'Stores OpenAI tool responses associated with chat messages';
