-- Migration script to fix chat_message.text column length issue
-- Run this script on your PostgreSQL database

-- Alter the text column to use TEXT type instead of VARCHAR
ALTER TABLE chat_message ALTER COLUMN text TYPE TEXT;

-- Verify the change
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'chat_message' AND column_name = 'text'; 