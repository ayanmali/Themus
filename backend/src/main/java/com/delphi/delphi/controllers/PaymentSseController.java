package com.delphi.delphi.controllers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.delphi.delphi.dtos.messaging.PaymentResponseDto;

@RestController
@RequestMapping("/api/payments/sse")
public class PaymentSseController {

    private static final Logger log = LoggerFactory.getLogger(PaymentSseController.class);
    
    // Store SSE connections by requestId
    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    /**
     * Client subscribes to SSE updates for a specific payment request
     */
    @GetMapping(value = "/subscribe/{requestId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String requestId) {
        log.info("Client subscribing to SSE updates for request: {}", requestId);
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        connections.put(requestId, emitter);
        
        // Set up cleanup when connection closes
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for request: {}", requestId);
            connections.remove(requestId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE connection timed out for request: {}", requestId);
            connections.remove(requestId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE connection error for request: {}: {}", requestId, ex.getMessage());
            connections.remove(requestId);
        });
        
        try {
            // Send initial connection confirmation
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(Map.of(
                    "message", "Connected to payment updates",
                    "requestId", requestId,
                    "timestamp", System.currentTimeMillis()
                ))
            );
        } catch (IOException e) {
            log.error("Failed to send initial SSE message for request: {}", requestId, e);
            connections.remove(requestId);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * Send payment update to subscribed client
     * Called by PaymentResponseSubscriber
     */
    public void sendPaymentUpdate(String requestId, PaymentResponseDto response) {
        SseEmitter emitter = connections.get(requestId);
        if (emitter == null) {
            log.debug("No SSE connection found for request: {}", requestId);
            return;
        }

        try {
            Map<String, Object> eventData = Map.of(
                "requestId", response.getRequestId(),
                "success", response.isSuccess(),
                "error", response.getError() != null ? response.getError() : "",
                "timestamp", System.currentTimeMillis(),
                "data", buildEventData(response)
            );

            emitter.send(SseEmitter.event()
                .name(response.isSuccess() ? "payment-success" : "payment-error")
                .data(eventData)
            );

            log.info("Sent SSE payment update for request: {}", requestId);
            
            // Complete the connection after sending final result
            emitter.complete();
            connections.remove(requestId);
            
        } catch (IOException e) {
            log.error("Failed to send SSE payment update for request: {}: {}", requestId, e.getMessage());
            connections.remove(requestId);
            emitter.completeWithError(e);
        }
    }

    private Map<String, Object> buildEventData(PaymentResponseDto response) {
        if (!response.isSuccess()) {
            return Map.of();
        }

        if (response.getCustomer() != null) {
            return Map.of(
                "type", "customer",
                "customerId", response.getCustomer().getId()
            );
        } else if (response.getCheckoutSession() != null) {
            return Map.of(
                "type", "checkout",
                "url", response.getCheckoutSession().getUrl()
            );
        } else if (response.getSubscriptionData() != null) {
            return Map.of(
                "type", "subscription",
                "status", response.getSubscriptionData().getStatus()
            );
        }

        return Map.of();
    }

    /**
     * Get the count of active SSE connections (for monitoring)
     */
    @GetMapping("/connections/count")
    public Map<String, Object> getConnectionCount() {
        return Map.of(
            "activeConnections", connections.size(),
            "requestIds", connections.keySet()
        );
    }
} 