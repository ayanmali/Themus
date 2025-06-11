package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.AttemptStatus;

import lombok.Data;

@Data
public class FetchCandidateAttemptDto {
    private Long id;
    private String githubRepositoryLink;
    private AttemptStatus status;
    private String languageChoice;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime startedDate;
    private LocalDateTime submittedDate;
    private LocalDateTime evaluatedDate;
    private Long candidateId;
    private Long assessmentId;
    private Long evaluationId;

    public FetchCandidateAttemptDto(CandidateAttempt candidateAttempt) {
        this.id = candidateAttempt.getId();
        this.githubRepositoryLink = candidateAttempt.getGithubRepositoryLink();
        this.status = candidateAttempt.getStatus();
        this.languageChoice = candidateAttempt.getLanguageChoice();
        this.createdDate = candidateAttempt.getCreatedDate();
        this.updatedDate = candidateAttempt.getUpdatedDate();
        this.startedDate = candidateAttempt.getStartedDate();
        this.submittedDate = candidateAttempt.getSubmittedDate();
        this.evaluatedDate = candidateAttempt.getEvaluatedDate();
        this.candidateId = candidateAttempt.getCandidate().getId();
        this.assessmentId = candidateAttempt.getAssessment().getId();
        this.evaluationId = candidateAttempt.getEvaluation().getId();
    }
}
