package com.delphi.delphi.controllers;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.components.StripeService;
import com.delphi.delphi.components.messaging.PaymentMessagePublisher;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.PaymentStatusService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.payments.StripeSubCache;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final UserService userService;
    private final PaymentMessagePublisher paymentMessagePublisher;
    private final PaymentStatusService paymentStatusService;
    private final StripeService stripeService;
    private final RedisService redisService;

    public PaymentController(PaymentMessagePublisher paymentMessagePublisher, PaymentStatusService paymentStatusService, StripeService stripeService, RedisService redisService, UserService userService) {
        this.paymentMessagePublisher = paymentMessagePublisher;
        this.paymentStatusService = paymentStatusService;
        this.stripeService = stripeService;
        this.redisService = redisService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    @GetMapping("/initiate-checkout")
    public ResponseEntity<?> initiateStripeCheckout() {
        try {
            User user = getCurrentUser();
            
            // Check if customer already exists in Redis
            String existingCustomerId = (String) redisService.get("stripe:user:" + user.getId());
            
            if (existingCustomerId != null) {
                // Customer exists, create checkout session async
                String checkoutRequestId = paymentMessagePublisher.publishCreateCheckoutSessionRequest(existingCustomerId);
                paymentStatusService.markAsProcessing(checkoutRequestId);
                
                return ResponseEntity.accepted().body(Map.of(
                    "message", "Checkout session creation initiated",
                    "requestId", checkoutRequestId,
                    "status", "processing"
                ));
            } else {
                // Customer needs to be created first
                String customerRequestId = paymentMessagePublisher.publishCreateCustomerRequest(user.getId());
                paymentStatusService.markAsProcessing(customerRequestId);
                
                return ResponseEntity.accepted().body(Map.of(
                    "message", "Customer creation initiated, checkout session will follow",
                    "requestId", customerRequestId,
                    "status", "processing"
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error initiating checkout: " + e.getMessage());
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> stripeWebhook(
            @RequestHeader(name = "Stripe-Signature", required = true) String signature, 
            @RequestBody String body) {
        
        try {
            // Publish webhook for async processing instead of processing synchronously
            String requestId = paymentMessagePublisher.publishStripeWebhook(signature, body);
            paymentStatusService.markAsProcessing(requestId);
            
            // Return immediately - webhook will be processed asynchronously
            return ResponseEntity.accepted().body(Map.of(
                "message", "Webhook received and queued for processing",
                "requestId", requestId,
                "status", "processing"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/checkout/success")
    public ResponseEntity<?> checkoutSuccess() {
        try {
            User user = getCurrentUser();
            String stripeCustomerId = (String)redisService.get("stripe:user:" + user.getId());

            if (stripeCustomerId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Stripe customer ID not found");
            }

            // Async sync stripe data
            String requestId = paymentMessagePublisher.publishSyncSubscriptionDataRequest(stripeCustomerId);
            paymentStatusService.markAsProcessing(requestId);
            
            return ResponseEntity.accepted().body(Map.of(
                "message", "Subscription data sync initiated",
                "requestId", requestId,
                "status", "processing"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing checkout success: " + e.getMessage());
        }
    }

    @GetMapping("/subscription")    
    public ResponseEntity<?> getSubscription() {
        try {
            User user = getCurrentUser();
            
            // Try to get from cache first (synchronous for immediate response)
            try {
                StripeSubCache subData = stripeService.getSubscription(user.getId());
                return ResponseEntity.ok(subData);
            } catch (Exception e) {
                // If not in cache, request async update
                String requestId = paymentMessagePublisher.publishGetSubscriptionRequest(user.getId());
                paymentStatusService.markAsProcessing(requestId);
                
                return ResponseEntity.accepted().body(Map.of(
                    "message", "Subscription data fetch initiated",
                    "requestId", requestId,
                    "status", "processing",
                    "note", "Poll /api/payments/status/" + requestId + " for updates"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    // Polling endpoint for checking async operation status
    @GetMapping("/status/{requestId}")
    public ResponseEntity<?> getOperationStatus(@PathVariable String requestId) {
        try {
            Map<String, Object> status = paymentStatusService.getStatus(requestId);
            
            if (status == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Request ID not found or expired",
                    "requestId", requestId
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "requestId", requestId,
                "status", status.get("status"),
                "data", status.get("data"),
                "error", status.get("error"),
                "timestamp", status.get("timestamp")
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving status: " + e.getMessage());
        }
    }
}
