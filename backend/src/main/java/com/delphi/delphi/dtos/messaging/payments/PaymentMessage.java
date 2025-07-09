// package com.delphi.delphi.dtos.messaging.payments;

// import java.io.Serializable;
// import java.time.LocalDateTime;

// // Base message class for all payment-related messages
// public abstract class PaymentMessage implements Serializable {
//     private static final long serialVersionUID = 1L;
    
//     private String messageId;
    
//     private LocalDateTime timestamp;
    
//     private int retryCount = 0;
    
//     public PaymentMessage() {
//         this.timestamp = LocalDateTime.now();
//         this.messageId = java.util.UUID.randomUUID().toString();
//     }
    
//     // Getters and setters
//     public String getMessageId() { return messageId; }
//     public void setMessageId(String messageId) { this.messageId = messageId; }
    
//     public LocalDateTime getTimestamp() { return timestamp; }
//     public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
//     public int getRetryCount() { return retryCount; }
//     public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
//     public void incrementRetryCount() { this.retryCount++; }
// }
