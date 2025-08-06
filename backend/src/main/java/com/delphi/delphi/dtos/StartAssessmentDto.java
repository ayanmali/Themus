package com.delphi.delphi.dtos;

public class StartAssessmentDto {

    private String candidateEmail;
    private Long assessmentId;
    private String languageOption;

    public StartAssessmentDto(String candidateEmail, Long assessmentId, String languageOption) {
        this.candidateEmail = candidateEmail;
        this.assessmentId = assessmentId;
        this.languageOption = languageOption;
    }

    public StartAssessmentDto() {
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }
    
    public Long getAssessmentId() {
        return assessmentId;
    }

    public String getLanguageOption() {
        return languageOption;
    }
}
