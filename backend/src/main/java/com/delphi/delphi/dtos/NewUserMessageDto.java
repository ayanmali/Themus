package com.delphi.delphi.dtos;

public class NewUserMessageDto {
    private String message;
    private Long assessmentId;
    private String model;
    // sender is USER
    
    public NewUserMessageDto() {
    }

    public NewUserMessageDto(String message, Long assessmentId, String model) {
        this.message = message;
        this.assessmentId = assessmentId;
        this.model = model;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    
}