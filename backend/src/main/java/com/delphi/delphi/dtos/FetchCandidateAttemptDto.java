package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.enums.AttemptStatus;

public class FetchCandidateAttemptDto {
    private Long id;
    private String githubRepositoryLink;
    private AttemptStatus status;
    private String languageChoice;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime startedDate;
    private LocalDateTime completedDate;
    private LocalDateTime evaluatedDate;
    private FetchAssessmentDto assessment;
    private FetchEvaluationDto evaluation;
    private FetchCandidateDto candidate;

    public FetchCandidateAttemptDto() {
    }

    public FetchCandidateAttemptDto(CandidateAttempt candidateAttempt) {
        this.id = candidateAttempt.getId();
        this.githubRepositoryLink = candidateAttempt.getGithubRepositoryLink();
        this.status = candidateAttempt.getStatus();
        this.languageChoice = candidateAttempt.getLanguageChoice();
        this.createdDate = candidateAttempt.getCreatedDate();
        this.updatedDate = candidateAttempt.getUpdatedDate();
        this.startedDate = candidateAttempt.getStartedDate();
        this.completedDate = candidateAttempt.getCompletedDate();
        this.evaluatedDate = candidateAttempt.getEvaluatedDate();
        //this.assessmentId = candidateAttempt.getAssessment().getId();
        this.assessment = new FetchAssessmentDto(candidateAttempt.getAssessment());
        this.candidate = new FetchCandidateDto(candidateAttempt.getCandidate());
        if (candidateAttempt.getEvaluation() != null) {
            this.evaluation = new FetchEvaluationDto(candidateAttempt.getEvaluation());
        }
    }

    public FetchCandidateAttemptDto(CandidateAttemptCacheDto candidateAttempt) {
        this.id = candidateAttempt.getId();
        this.githubRepositoryLink = candidateAttempt.getGithubRepositoryLink();
        this.status = candidateAttempt.getStatus();
        this.languageChoice = candidateAttempt.getLanguageChoice();
        this.createdDate = candidateAttempt.getCreatedDate();
        this.updatedDate = candidateAttempt.getUpdatedDate();
        this.startedDate = candidateAttempt.getStartedDate();
        this.completedDate = candidateAttempt.getCompletedDate();
        this.evaluatedDate = candidateAttempt.getEvaluatedDate();
        //this.assessmentId = candidateAttempt.getAssessmentId();
        this.assessment = new FetchAssessmentDto(candidateAttempt.getAssessment());
        if (candidateAttempt.getEvaluation() != null) {
            this.evaluation = new FetchEvaluationDto(candidateAttempt.getEvaluation());
        }
        this.candidate = new FetchCandidateDto(candidateAttempt.getCandidate());
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

    public FetchAssessmentDto getAssessment() {
        return assessment;
    }

    public void setAssessment(FetchAssessmentDto assessment) {
        this.assessment = assessment;
    }

    public FetchEvaluationDto getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(FetchEvaluationDto evaluation) {
        this.evaluation = evaluation;
    }

    public FetchCandidateDto getCandidate() {
        return candidate;
    }

    public void setCandidate(FetchCandidateDto candidate) {
        this.candidate = candidate;
    }
}