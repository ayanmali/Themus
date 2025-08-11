package com.delphi.delphi.configs;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.delphi.delphi.services.AssessmentService;

@Component
public class AssessmentStatusScheduler {
    private final AssessmentService assessmentService;
    private final Logger log = LoggerFactory.getLogger(AssessmentStatusScheduler.class);

    public AssessmentStatusScheduler(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @Scheduled(cron = "0 0 2 * * *")    
    public void updateAssessmentStatus() {
        assessmentService.updateExpiredAssessments();
        log.info("Updated expired assessments for date: {}", LocalDate.now());
    }
}
