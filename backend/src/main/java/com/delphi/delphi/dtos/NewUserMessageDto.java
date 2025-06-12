package com.delphi.delphi.dtos;

public class NewUserMessageDto {
    private String message;
    private Long chatHistoryId;
    // sender is USER
    
    public NewUserMessageDto() {
    }

    public NewUserMessageDto(String message, Long chatHistoryId) {
        this.message = message;
        this.chatHistoryId = chatHistoryId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getChatHistoryId() {
        return chatHistoryId;
    }

    public void setChatHistoryId(Long chatHistoryId) {
        this.chatHistoryId = chatHistoryId;
    }
    
}