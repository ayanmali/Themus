package com.delphi.delphi.dtos;

public class AuthenticateCandidateDto {
    private String candidateEmail;
    private String plainTextPassword;
    private Long assessmentId;
    public AuthenticateCandidateDto() {}

    public AuthenticateCandidateDto(String candidateEmail, String plainTextPassword, Long assessmentId) {
        this.candidateEmail = candidateEmail;
        this.plainTextPassword = plainTextPassword;
        this.assessmentId = assessmentId;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getPlainTextPassword() {
        return plainTextPassword;
    }

    public void setPlainTextPassword(String plainTextPassword) {
        this.plainTextPassword = plainTextPassword;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }
}


