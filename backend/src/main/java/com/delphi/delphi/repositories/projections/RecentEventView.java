package com.delphi.delphi.repositories.projections;

import java.time.LocalDateTime;

public interface RecentEventView {
    Long getAttemptId();
    Long getAssessmentId();
    Long getCandidateId();
    String getEventType();
    LocalDateTime getEventTime();
}


