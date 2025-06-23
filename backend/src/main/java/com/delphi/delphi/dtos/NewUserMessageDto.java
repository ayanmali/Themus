package com.delphi.delphi.dtos;

public class NewUserMessageDto {
    private String message;
    private Long assessmentId;
    private Long userId;
    private String model;
    // sender is USER
    
    public NewUserMessageDto() {
    }

    public NewUserMessageDto(String message, Long assessmentId, Long userId, String model) {
        this.message = message;
        this.assessmentId = assessmentId;
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    
}