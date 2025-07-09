package com.delphi.delphi.dtos.messaging.payments;

// Stripe webhook event message
public class StripeWebhookMessage extends PaymentMessage {
    private String eventType;
    
    private String signature;
    
    private String body;
    
    private String stripeEventId;
    
    public StripeWebhookMessage() {}
    
    public StripeWebhookMessage(String eventType, String signature, String body, String stripeEventId) {
        super();
        this.eventType = eventType;
        this.signature = signature;
        this.body = body;
        this.stripeEventId = stripeEventId;
    }
    
    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public String getStripeEventId() { return stripeEventId; }
    public void setStripeEventId(String stripeEventId) { this.stripeEventId = stripeEventId; }
}
