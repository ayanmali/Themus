package com.delphi.delphi.dtos.messaging.payments;

import java.util.Map;

// Notification message
public class NotificationMessage extends PaymentMessage {
    private Long userId;
    
    private String type;
    
    private String title;
    
    private String message;
    
    private Map<String, Object> data;
    
    public NotificationMessage() {}
    
    public NotificationMessage(Long userId, String type, String title, String message) {
        super();
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
    }
    
    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
