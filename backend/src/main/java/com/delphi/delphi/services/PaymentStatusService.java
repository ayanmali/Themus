package com.delphi.delphi.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.messaging.PaymentResponseDto;

@Service
public class PaymentStatusService {
    
    private final RedisService redisService;
    private static final String STATUS_KEY_PREFIX = "payment:status:";
    private static final int STATUS_TTL_HOURS = 24; // Status expires after 24 hours
    
    public PaymentStatusService(RedisService redisService) {
        this.redisService = redisService;
    }
    
    public void updateStatus(String requestId, String status, Object data, String error) {
        String key = STATUS_KEY_PREFIX + requestId;
        
        Map<String, Object> statusData = Map.of(
            "status", status,
            "data", data != null ? data : "",
            "error", error != null ? error : "",
            "timestamp", System.currentTimeMillis()
        );
        
        // Store with TTL
        redisService.setWithExpiration(key, statusData, STATUS_TTL_HOURS, java.util.concurrent.TimeUnit.HOURS);
    }
    
    public void updateStatus(String requestId, PaymentResponseDto response) {
        String status = response.isSuccess() ? "completed" : "error";
        Object data = null;
        
        if (response.isSuccess()) {
            if (response.getCustomer() != null) {
                data = Map.of("type", "customer", "customerId", response.getCustomer().getId());
            } else if (response.getCheckoutSession() != null) {
                data = Map.of("type", "checkout", "url", response.getCheckoutSession().getUrl());
            } else if (response.getSubscriptionData() != null) {
                data = Map.of("type", "subscription", "status", response.getSubscriptionData().getStatus());
            }
        }
        
        updateStatus(requestId, status, data, response.getError());
    }
    
    public Map<String, Object> getStatus(String requestId) {
        String key = STATUS_KEY_PREFIX + requestId;
        return (Map<String, Object>) redisService.get(key);
    }
    
    public void markAsProcessing(String requestId) {
        updateStatus(requestId, "processing", null, null);
    }
    
    public void markAsCompleted(String requestId, Object result) {
        updateStatus(requestId, "completed", result, null);
    }
    
    public void markAsError(String requestId, String error) {
        updateStatus(requestId, "error", null, error);
    }
} 