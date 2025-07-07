package com.delphi.delphi.dtos.messaging;

import org.springframework.ai.chat.model.ChatResponse;

public class ChatCompletionResponseDto {
    private String requestId;
    private ChatResponse chatResponse;
    private String error;
    private boolean success;

    public ChatCompletionResponseDto() {}

    public ChatCompletionResponseDto(String requestId, ChatResponse chatResponse) {
        this.requestId = requestId;
        this.chatResponse = chatResponse;
        this.success = true;
    }

    public ChatCompletionResponseDto(String requestId, String error) {
        this.requestId = requestId;
        this.error = error;
        this.success = false;
    }

    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public ChatResponse getChatResponse() { return chatResponse; }
    public void setChatResponse(ChatResponse chatResponse) { this.chatResponse = chatResponse; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
