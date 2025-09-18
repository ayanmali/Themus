package com.delphi.delphi.dtos;

/** For when the attempt has been started by the candidate
* NOT when they have been invited
*/
public class StartAttemptDto {
    private String languageChoice;
    private String candidateEmail;
    private Long assessmentId;

    public StartAttemptDto() {
    }

    public StartAttemptDto(String languageChoice, String candidateEmail, Long assessmentId) {
        this.languageChoice = languageChoice;
        this.candidateEmail = candidateEmail;
        this.assessmentId = assessmentId;
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
   
}