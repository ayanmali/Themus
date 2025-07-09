package com.delphi.delphi.dtos.messaging.payments;

import java.util.Map;

// Payment processing message
public class PaymentProcessingMessage extends PaymentMessage {
    private String customerId;

    private Long userId;
    
    private String eventType;
    
    private String paymentIntentId;
    
    private Long amount;
    
    private String currency;
    
    private String status;
    
    private Map<String, String> metadata;
    
    public PaymentProcessingMessage() {}
    
    public PaymentProcessingMessage(String customerId, Long userId, String eventType) {
        super();
        this.customerId = customerId;
        this.userId = userId;
        this.eventType = eventType;
    }
    
    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
