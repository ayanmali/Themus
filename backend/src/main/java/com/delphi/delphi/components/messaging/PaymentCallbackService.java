package com.delphi.delphi.components.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.dtos.messaging.PaymentResponseDto;

@Service
public class PaymentCallbackService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackService.class);
    private final RestTemplate restTemplate;

    public PaymentCallbackService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send HTTP callback to client-provided URL
     * Client registers callback URL when initiating payment operations
     */
    public void sendPaymentCallback(String callbackUrl, PaymentResponseDto response) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> callbackPayload = Map.of(
                "requestId", response.getRequestId(),
                "success", response.isSuccess(),
                "error", response.getError() != null ? response.getError() : "",
                "timestamp", System.currentTimeMillis(),
                "data", buildCallbackData(response)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(callbackPayload, headers);
            
            log.info("Sending payment callback to: {} for request: {}", callbackUrl, response.getRequestId());
            
            ResponseEntity<String> callbackResponse = restTemplate.exchange(
                callbackUrl, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (callbackResponse.getStatusCode().is2xxSuccessful()) {
                log.info("Callback sent successfully to: {} for request: {}", callbackUrl, response.getRequestId());
            } else {
                log.warn("Callback failed with status: {} for request: {}", 
                    callbackResponse.getStatusCode(), response.getRequestId());
            }
            
        } catch (Exception e) {
            log.error("Failed to send callback to: {} for request: {}: {}", 
                callbackUrl, response.getRequestId(), e.getMessage(), e);
            
            // TODO: Implement retry mechanism with exponential backoff
            // Could store failed callbacks in database for retry
        }
    }

    private Map<String, Object> buildCallbackData(PaymentResponseDto response) {
        if (!response.isSuccess()) {
            return Map.of();
        }

        if (response.getCustomer() != null) {
            return Map.of(
                "type", "customer",
                "customerId", response.getCustomer().getId(),
                "email", response.getCustomer().getEmail()
            );
        } else if (response.getCheckoutSession() != null) {
            return Map.of(
                "type", "checkout",
                "sessionId", response.getCheckoutSession().getId(),
                "url", response.getCheckoutSession().getUrl()
            );
        } else if (response.getSubscriptionData() != null) {
            return Map.of(
                "type", "subscription",
                "status", response.getSubscriptionData().getStatus(),
                "subscriptionId", response.getSubscriptionData().getSubscriptionId()
            );
        }

        return Map.of();
    }
} 