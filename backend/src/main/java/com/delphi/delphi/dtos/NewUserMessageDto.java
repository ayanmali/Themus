package com.delphi.delphi.dtos;

public class NewUserMessageDto {
    private String message;
    private Long chatHistoryId;
    private String model;
    // sender is USER
    
    public NewUserMessageDto() {
    }

    public NewUserMessageDto(String message, Long chatHistoryId, String model) {
        this.message = message;
        this.chatHistoryId = chatHistoryId;
        this.model = model;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    
}