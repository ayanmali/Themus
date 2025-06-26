package com.delphi.delphi.utils.payments;

import com.stripe.model.Subscription;

public class StripeSubCache {
    private String status;
    private String priceId;
    private String customerId;
    private String subscriptionId;
    private String startDate;
    private String endDate;
    private Long currentPeriodEnd;
    private Long currentPeriodStart;
    private Boolean cancelAtPeriodEnd;
    private PaymentMethodInternal paymentMethod;

    public StripeSubCache() {
        this.status = "none";
    }

    public StripeSubCache(Subscription subscription) {
        this.subscriptionId = subscription.getId();
        this.status = subscription.getStatus();
        this.priceId = subscription.getItems().getData().getFirst().getPrice().getId();
        this.currentPeriodEnd = subscription.getItems().getData().getFirst().getCurrentPeriodEnd();
        this.currentPeriodStart = subscription.getItems().getData().getFirst().getCurrentPeriodStart(); 
        this.cancelAtPeriodEnd = subscription.getCancelAtPeriodEnd();
        if (subscription.getDefaultPaymentMethodObject() != null && subscription.getDefaultPaymentMethodObject().getCard() != null) {
            this.paymentMethod = new PaymentMethodInternal(subscription.getDefaultPaymentMethodObject());
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(Long currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public Long getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public void setCurrentPeriodStart(Long currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    public Boolean getCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(Boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public PaymentMethodInternal getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodInternal paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
}