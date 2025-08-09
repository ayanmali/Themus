package com.delphi.delphi.dtos.messaging.chat;

import java.io.Serializable;
import java.util.Map;

public class ChatCompletionRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userMessage;
    private String userPromptTemplate;
    private Map<String, Object> userPromptVariables;
    private String model;
    private Long assessmentId;
    private Long userId;
    private String requestId; // For tracking async responses

    // Constructors
    public ChatCompletionRequestDto() {}

    // For simple user message
    public ChatCompletionRequestDto(String userMessage, String model, Long assessmentId, Long userId, String requestId) {
        this.userMessage = userMessage;
        this.model = model;
        this.assessmentId = assessmentId;
        this.userId = userId;
        this.requestId = requestId;
    }

    // For template-based message
    public ChatCompletionRequestDto(String userPromptTemplate, Map<String, Object> userPromptVariables, String model, Long assessmentId, Long userId, String requestId) {
        this.userPromptTemplate = userPromptTemplate;
        this.userPromptVariables = userPromptVariables;
        this.model = model;
        this.assessmentId = assessmentId;
        this.userId = userId;        
        this.requestId = requestId;
    }

    // Getters and setters
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    
    public String getUserPromptTemplate() { return userPromptTemplate; }
    public void setUserPromptTemplate(String userPromptTemplate) { this.userPromptTemplate = userPromptTemplate; }
    
    public Map<String, Object> getUserPromptVariables() { return userPromptVariables; }
    public void setUserPromptVariables(Map<String, Object> userPromptVariables) { this.userPromptVariables = userPromptVariables; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}