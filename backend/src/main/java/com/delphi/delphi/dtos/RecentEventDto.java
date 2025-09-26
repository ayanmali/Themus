package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

public class RecentEventDto {
    private final Long attemptId;
    private final Long assessmentId;
    private final Long candidateId;
    private final String eventType;
    private final LocalDateTime eventTime;

    public RecentEventDto(Long attemptId, Long assessmentId, Long candidateId, String eventType, LocalDateTime eventTime) {
        this.attemptId = attemptId;
        this.assessmentId = assessmentId;
        this.candidateId = candidateId;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    public Long getAttemptId() { return attemptId; }
    public Long getAssessmentId() { return assessmentId; }
    public Long getCandidateId() { return candidateId; }
    public String getEventType() { return eventType; }
    public LocalDateTime getEventTime() { return eventTime; }
}


