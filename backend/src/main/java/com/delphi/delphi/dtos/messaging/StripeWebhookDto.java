package com.delphi.delphi.dtos.messaging;

import java.io.Serializable;

public class StripeWebhookDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String signature;
    private String body;
    private String eventType;
    private String customerId;
    private String requestId; // For tracking async processing
    
    // Constructors
    public StripeWebhookDto() {}
    
    public StripeWebhookDto(String signature, String body, String requestId) {
        this.signature = signature;
        this.body = body;
        this.requestId = requestId;
    }
    
    public StripeWebhookDto(String signature, String body, String eventType, String customerId, String requestId) {
        this.signature = signature;
        this.body = body;
        this.eventType = eventType;
        this.customerId = customerId;
        this.requestId = requestId;
    }
    
    // Getters and setters
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
} 