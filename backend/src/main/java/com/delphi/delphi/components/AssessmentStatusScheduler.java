package com.delphi.delphi.components;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.CandidateAttemptService;

@Component
public class AssessmentStatusScheduler {

    private final CandidateAttemptService candidateAttemptService;
    private final AssessmentService assessmentService;
    private final Logger log = LoggerFactory.getLogger(AssessmentStatusScheduler.class);

    public AssessmentStatusScheduler(AssessmentService assessmentService, CandidateAttemptService candidateAttemptService) {
        this.assessmentService = assessmentService;
        this.candidateAttemptService = candidateAttemptService;
    }

    @Scheduled(cron = "0 0 2 * * *")    
    public void updateAssessmentStatus() {
        assessmentService.updateExpiredAssessments();
        log.info("Updated expired assessments for date: {}", LocalDate.now());
    }

    // @Scheduled(cron = "0 0 2 * * *")
    // public void updateCandidateAttemptStatus() {
    //     candidateAttemptService.updateExpiredAttempts();
    //     log.info("Updated expired candidate attempts for date: {}", LocalDate.now());
    // }

    @Scheduled(cron = "0 30 2 * * *") // Run 30 minutes after the other jobs
    public void updateAttemptsForInactiveAssessments() {
        candidateAttemptService.updateAttemptsForInactiveAssessments();
        log.info("Updated candidate attempts for inactive assessments for date: {}", LocalDate.now());
    }
}
