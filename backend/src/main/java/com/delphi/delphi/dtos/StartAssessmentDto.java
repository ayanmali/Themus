package com.delphi.delphi.dtos;

public class StartAssessmentDto {

    private String candidateEmail;
    private String assessmentId;
    private String languageOption;

    public StartAssessmentDto(String candidateEmail, String assessmentId, String languageOption) {
        this.candidateEmail = candidateEmail;
        this.assessmentId = assessmentId;
        this.languageOption = languageOption;
    }

    public StartAssessmentDto() {
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }
    
    public String getAssessmentId() {
        return assessmentId;
    }

    public String getLanguageOption() {
        return languageOption;
    }
}
