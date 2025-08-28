package com.delphi.delphi.dtos;

import java.util.Optional;

import com.delphi.delphi.utils.enums.AttemptStatus;

/** For when updating an existing candidate attempt
*/
public class UpdateCandidateAttemptDto {
    private String githubRepositoryLink;
    private String languageChoice;
    private AttemptStatus status;

    public UpdateCandidateAttemptDto() {
    }

    public UpdateCandidateAttemptDto(String githubRepositoryLink, String languageChoice, AttemptStatus status) {
        this.githubRepositoryLink = githubRepositoryLink;
        this.languageChoice = languageChoice;
        this.status = status;
    }

    public String getGithubRepositoryLink() {
        return githubRepositoryLink;
    }

    public void setGithubRepositoryLink(String githubRepositoryLink) {
        this.githubRepositoryLink = githubRepositoryLink;
    }

    public String getLanguageChoice() {
        return languageChoice;
    }

    public void setLanguageChoice(String languageChoice) {
        this.languageChoice = languageChoice;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public Optional<String> getLanguageChoiceOptional() {
        return Optional.ofNullable(languageChoice);
    }
}
