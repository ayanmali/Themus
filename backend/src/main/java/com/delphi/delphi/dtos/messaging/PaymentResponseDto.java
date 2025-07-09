package com.delphi.delphi.dtos.messaging;

import java.io.Serializable;

import com.delphi.delphi.utils.payments.StripeSubCache;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;

public class PaymentResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String requestId;
    private boolean success;
    private String error;
    
    // Response data based on operation type
    private Customer customer;
    private Session checkoutSession;
    private StripeSubCache subscriptionData;
    
    // Constructors
    public PaymentResponseDto() {}
    
    // Success constructors
    public PaymentResponseDto(String requestId, Customer customer) {
        this.requestId = requestId;
        this.success = true;
        this.customer = customer;
    }
    
    public PaymentResponseDto(String requestId, Session checkoutSession) {
        this.requestId = requestId;
        this.success = true;
        this.checkoutSession = checkoutSession;
    }
    
    public PaymentResponseDto(String requestId, StripeSubCache subscriptionData) {
        this.requestId = requestId;
        this.success = true;
        this.subscriptionData = subscriptionData;
    }
    
    // Error constructor
    public PaymentResponseDto(String requestId, String error) {
        this.requestId = requestId;
        this.success = false;
        this.error = error;
    }
    
    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public Session getCheckoutSession() { return checkoutSession; }
    public void setCheckoutSession(Session checkoutSession) { this.checkoutSession = checkoutSession; }
    
    public StripeSubCache getSubscriptionData() { return subscriptionData; }
    public void setSubscriptionData(StripeSubCache subscriptionData) { this.subscriptionData = subscriptionData; }
} 