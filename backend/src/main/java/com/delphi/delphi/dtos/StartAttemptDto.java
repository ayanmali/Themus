package com.delphi.delphi.dtos;

/** For when the attempt has been started by the candidate
* NOT when they have been invited
*/
public class StartAttemptDto {
    private String githubRepositoryLink;
    private String languageChoice;
    private String candidateEmail;
    private Long assessmentId;
    private String password;

    public StartAttemptDto() {
    }

    public StartAttemptDto(String githubRepositoryLink, String languageChoice, String candidateEmail, Long assessmentId, String password) {
        this.githubRepositoryLink = githubRepositoryLink;
        this.languageChoice = languageChoice;
        this.candidateEmail = candidateEmail;
        this.assessmentId = assessmentId;
        this.password = password;
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

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
   
}