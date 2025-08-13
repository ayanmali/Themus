# Scheduled Jobs Documentation

This document describes the scheduled jobs implemented in the Themus application.

## Overview

The application uses Spring's `@Scheduled` annotation to run periodic tasks. All scheduled jobs are configured in the `AssessmentStatusScheduler` class and run daily at 2:00 AM.

## Existing Jobs

### 1. Update Assessment Status
- **Method**: `updateAssessmentStatus()`
- **Schedule**: `0 0 2 * * *` (Daily at 2:00 AM)
- **Purpose**: Updates assessments from ACTIVE to INACTIVE status when their end date has passed
- **Implementation**: Calls `assessmentService.updateExpiredAssessments()`

### 2. Update Candidate Attempt Status
- **Method**: `updateCandidateAttemptStatus()`
- **Schedule**: `0 0 2 * * *` (Daily at 2:00 AM)
- **Purpose**: Updates candidate attempts from STARTED/INVITED to EXPIRED status when they exceed the assessment duration
- **Implementation**: Calls `candidateAttemptService.updateExpiredAttempts()`

## New Job: Update Attempts for Inactive Assessments

### 3. Update Attempts for Inactive Assessments
- **Method**: `updateAttemptsForInactiveAssessments()`
- **Schedule**: `0 30 2 * * *` (Daily at 2:30 AM - 30 minutes after other jobs)
- **Purpose**: Updates candidate attempts from STARTED/INVITED to EXPIRED status when their associated assessment becomes INACTIVE
- **Implementation**: Calls `candidateAttemptService.updateAttemptsForInactiveAssessments()`

### How it Works

1. **Assessment Status Changes**: Assessments automatically become INACTIVE when their `endDate` passes (handled by `@PreUpdate` in Assessment entity)

2. **Attempt Status Updates**: The scheduled job finds all candidate attempts with status:
   - `INVITED` or `STARTED`
   - Associated with assessments that have status `INACTIVE`
   
3. **Bulk Update**: Updates these attempts to `EXPIRED` status using a single SQL query for efficiency

### Database Query

The job uses the following JPA query:
```sql
UPDATE CandidateAttempt ca 
SET ca.status = 'EXPIRED' 
WHERE (ca.status = 'STARTED' OR ca.status = 'INACTIVE') 
  AND ca.assessment.status = 'INACTIVE'
```

### Benefits

- **Automatic Cleanup**: Ensures candidate attempts are properly marked as expired when assessments end
- **Data Consistency**: Maintains consistency between assessment and attempt statuses
- **Performance**: Uses efficient bulk update operations
- **Timing**: Runs after assessment status updates to ensure proper sequencing

## Configuration

All scheduled jobs are enabled by the `@EnableScheduling` annotation on the main `DelphiApplication` class.

## Monitoring

Each job logs its execution with the current date for monitoring purposes:
```java
log.info("Updated candidate attempts for inactive assessments for date: {}", LocalDate.now());
```

## Future Enhancements

- Add metrics collection for job execution times and affected record counts
- Implement retry logic for failed job executions
- Add email notifications for job failures
- Consider more frequent execution for time-sensitive operations
