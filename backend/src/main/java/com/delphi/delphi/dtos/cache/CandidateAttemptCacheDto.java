package com.delphi.delphi.dtos.cache;

import java.time.LocalDateTime;

import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.AttemptStatus;

public class CandidateAttemptCacheDto {
    private Long id;
    private String githubRepositoryLink;
    private AttemptStatus status;
    private String languageChoice;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime startedDate;
    private LocalDateTime completedDate;
    private LocalDateTime evaluatedDate;
    private CandidateCacheDto candidate;
    private Long assessmentId;
    private Long evaluationId;

    // Default constructor for JSON deserialization
    public CandidateAttemptCacheDto() {
    }

    public CandidateAttemptCacheDto(CandidateAttempt candidateAttempt) {
        this.id = candidateAttempt.getId();
        this.githubRepositoryLink = candidateAttempt.getGithubRepositoryLink();
        this.status = candidateAttempt.getStatus();
        this.languageChoice = candidateAttempt.getLanguageChoice();
        this.createdDate = candidateAttempt.getCreatedDate();
        this.updatedDate = candidateAttempt.getUpdatedDate();
        this.startedDate = candidateAttempt.getStartedDate();
        this.completedDate = candidateAttempt.getCompletedDate();
        this.evaluatedDate = candidateAttempt.getEvaluatedDate();
        this.candidate = new CandidateCacheDto(candidateAttempt.getCandidate());
        this.assessmentId = candidateAttempt.getAssessment().getId();
        this.evaluationId = candidateAttempt.getEvaluation().getId();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGithubRepositoryLink() {
        return githubRepositoryLink;
    }
    public void setGithubRepositoryLink(String githubRepositoryLink) {
        this.githubRepositoryLink = githubRepositoryLink;
    }
    public AttemptStatus getStatus() {
        return status;
    }
    public void setStatus(AttemptStatus status) {
        this.status = status;
    }
    public String getLanguageChoice() {
        return languageChoice;
    }
    public void setLanguageChoice(String languageChoice) {
        this.languageChoice = languageChoice;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    public LocalDateTime getStartedDate() {
        return startedDate;
    }
    public void setStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }
    public LocalDateTime getEvaluatedDate() {
        return evaluatedDate;
    }
    public void setEvaluatedDate(LocalDateTime evaluatedDate) {
        this.evaluatedDate = evaluatedDate;
    }
    public CandidateCacheDto getCandidate() {
        return candidate;
    }
    public void setCandidate(CandidateCacheDto candidate) {
        this.candidate = candidate;
    }
    public Long getAssessmentId() {
        return assessmentId;
    }
    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }
    public Long getEvaluationId() {
        return evaluationId;
    }
    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    
}
