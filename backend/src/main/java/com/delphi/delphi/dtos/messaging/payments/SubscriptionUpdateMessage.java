package com.delphi.delphi.dtos.messaging.payments;

import java.util.Map;

// Subscription update message
public class SubscriptionUpdateMessage extends PaymentMessage {
    private String customerId;
    
    private Long userId;
    
    private String subscriptionId;
    
    private String eventType;
    
    private String status;
    
    private Long currentPeriodStart;
    
    private Long currentPeriodEnd;
    
    private String priceId;
    
    private Map<String, String> metadata;
    
    public SubscriptionUpdateMessage() {}
    
    public SubscriptionUpdateMessage(String customerId, Long userId, String subscriptionId, String eventType) {
        super();
        this.customerId = customerId;
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.eventType = eventType;
    }
    
    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(Long currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
    
    public Long getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(Long currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    
    public String getPriceId() { return priceId; }
    public void setPriceId(String priceId) { this.priceId = priceId; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
