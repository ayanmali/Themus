package com.delphi.delphi.dtos.messaging;

import java.io.Serializable;

public class PaymentRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum PaymentOperation {
        CREATE_CUSTOMER,
        CREATE_CHECKOUT_SESSION,
        SYNC_SUBSCRIPTION_DATA,
        GET_SUBSCRIPTION
    }
    
    private PaymentOperation operation;
    private Long userId;
    private String customerId;
    private String requestId; // For tracking async responses
    
    // Constructors
    public PaymentRequestDto() {}
    
    public PaymentRequestDto(PaymentOperation operation, Long userId, String requestId) {
        this.operation = operation;
        this.userId = userId;
        this.requestId = requestId;
    }
    
    public PaymentRequestDto(PaymentOperation operation, String customerId, String requestId) {
        this.operation = operation;
        this.customerId = customerId;
        this.requestId = requestId;
    }
    
    public PaymentRequestDto(PaymentOperation operation, Long userId, String customerId, String requestId) {
        this.operation = operation;
        this.userId = userId;
        this.customerId = customerId;
        this.requestId = requestId;
    }
    
    // Getters and setters
    public PaymentOperation getOperation() { return operation; }
    public void setOperation(PaymentOperation operation) { this.operation = operation; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
} 