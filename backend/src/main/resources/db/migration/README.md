# Database Migrations

This directory contains Flyway database migrations for the Themus application.

## Migration Files

### v1_0__create_user_schema.sql
- Creates the initial `themus` schema
- Creates a basic `users` table with minimal fields
- This is the initial migration that sets up the basic structure

### v2_0__create_complete_schema.sql
- Creates the complete database schema for the Themus application
- Includes all tables, relationships, constraints, and indexes
- Creates PostgreSQL enum types for status fields
- Sets up proper foreign key relationships with CASCADE delete rules

### v2_0__create_complete_schema_rollback.sql
- Rollback migration for the complete schema
- Drops all tables and enum types in the correct order
- Useful for development and testing purposes

## Schema Overview

### Core Tables
- **users**: User accounts with GitHub integration
- **assessments**: Assessment templates created by users
- **candidates**: Candidate information for assessments
- **candidate_attempts**: Individual candidate attempts at assessments
- **evaluations**: Evaluation results for candidate attempts

### Supporting Tables
- **chat_message**: AI chat interactions
- **open_ai_tool_calls**: OpenAI tool calls associated with chat messages
- **refresh_tokens**: Authentication refresh tokens
- **jobs**: Background job tracking

### Element Collections (JPA @ElementCollection)
- **assessment_skills**: Skills required for assessments
- **assessment_language_options**: Programming language options
- **assessment_metadata**: Custom metadata for assessments
- **candidate_metadata**: Custom metadata for candidates
- **evaluation_metadata**: Custom metadata for evaluations

### Junction Tables
- **candidate_assessments**: Many-to-many relationship between candidates and assessments

## Enum Types

- **assessment_status**: DRAFT, ACTIVE, INACTIVE, PUBLISHED
- **attempt_status**: INVITED, STARTED, COMPLETED, EVALUATED, EXPIRED
- **github_account_type**: USER, ORG
- **job_status**: PENDING, RUNNING, COMPLETED, FAILED
- **job_type**: CREATE_ASSESSMENT, CHAT_COMPLETION
- **message_type**: USER, ASSISTANT, SYSTEM

## Key Features

### Indexes
- Performance indexes on frequently queried columns
- Composite indexes for foreign key relationships
- Indexes on status and date fields for filtering

### Constraints
- Foreign key constraints with CASCADE delete rules
- Check constraints for date validation
- Unique constraints where appropriate
- NOT NULL constraints for required fields

### Data Integrity
- Proper foreign key relationships
- Check constraints for business logic validation
- Cascade delete rules for maintaining referential integrity

## Usage

### Running Migrations
```bash
# Using Flyway CLI
flyway migrate

# Using Maven
mvn flyway:migrate
```

### Rolling Back
```bash
# Using Flyway CLI
flyway undo

# Or manually run the rollback script
psql -d your_database -f v2_0__create_complete_schema_rollback.sql
```

### Development
- Always test migrations in a development environment first
- Use the rollback script for testing migration reversibility
- Consider data migration needs when modifying existing schemas

## Notes

- All tables use the `themus` schema
- Timestamps use PostgreSQL's `TIMESTAMP` type
- Text fields use `TEXT` type for unlimited length
- Primary keys use `BIGSERIAL` for auto-incrementing 64-bit integers
- Foreign keys use `BIGINT` to match primary key types
