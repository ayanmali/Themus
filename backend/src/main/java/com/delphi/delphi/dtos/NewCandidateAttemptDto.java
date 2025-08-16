package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.Optional;

import com.delphi.delphi.utils.AttemptStatus;

/** For when the attempt has been started by the candidate
* NOT when they have been invited
*/
public class NewCandidateAttemptDto {
    private String githubRepositoryLink;
    private Optional<String> languageChoice;
    private Long candidateId;
    private Long assessmentId;
    private AttemptStatus status;
    private LocalDateTime startedDate;

    public NewCandidateAttemptDto() {
    }

    public NewCandidateAttemptDto(String githubRepositoryLink, Optional<String> languageChoice, Long candidateId, Long assessmentId) {
        this.githubRepositoryLink = githubRepositoryLink;
        this.languageChoice = languageChoice;
        this.candidateId = candidateId;
        this.assessmentId = assessmentId;
        this.status = AttemptStatus.STARTED;
        this.startedDate = LocalDateTime.now();
    }

    public String getGithubRepositoryLink() {
        return githubRepositoryLink;
    }

    public void setGithubRepositoryLink(String githubRepositoryLink) {
        this.githubRepositoryLink = githubRepositoryLink;
    }

    public Optional<String> getLanguageChoice() {
        return languageChoice;
    }

    public void setLanguageChoice(Optional<String> languageChoice) {
        this.languageChoice = languageChoice;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }
   
}