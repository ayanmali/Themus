package com.delphi.delphi.controllers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.payments.StripeSubCache;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final UserService userService;

    private final StripeService stripeService;
    private final RedisService redisService;
    private final Logger log = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(StripeService stripeService, RedisService redisService, UserService userService) {
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
        User user = getCurrentUser();
        Customer customer = stripeService.createCustomer(user);
        Session session = stripeService.createCheckoutSession(customer.getId());
        return ResponseEntity.ok(session.getUrl());
    }

    /**
     * Webhook endpoint for Stripe events
     * This now queues events for asynchronous processing via RabbitMQ
     */
    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> stripeWebhook(
            @RequestHeader(name = "Stripe-Signature", required = true) String signature, 
            @RequestBody String body) {
        
        try {
            log.info("Received Stripe webhook with signature: {}", signature.substring(0, 20) + "...");
            
            // This will validate the signature and queue the event for processing
            stripeService.doEventProcessing(signature, body);
            
            log.info("Successfully queued webhook event for processing");
            return ResponseEntity.ok("Webhook received and queued for processing");
            
        } catch (RuntimeException e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.badRequest()
                    .body("Webhook processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @GetMapping("/checkout/success")
    public ResponseEntity<?> checkoutSuccess() {
        User user = getCurrentUser();
        // get stripe customer id from redis
        String stripeCustomerId = (String)redisService.get("stripe:user:" + user.getId());

        // if stripe customer id is not found, return redirect to home page
        if (stripeCustomerId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Stripe customer ID not found");
            // redirect to home page
        }

        // sync stripe data to redis
        stripeService.syncStripeDataToKV(stripeCustomerId);

        // return redirect to home page
        // redirect("/");
        return ResponseEntity.ok("Checkout success");
    }

    @GetMapping("/subscription")    
    public ResponseEntity<?> getSubscription() {
        try {
            User user = getCurrentUser();
            StripeSubCache subData = stripeService.getSubscription(user.getId());
            return ResponseEntity.ok(subData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        
    }

    /**
     * Health check endpoint for payment system
     */
    // @GetMapping("/health")
    // public ResponseEntity<?> healthCheck() {
    //     try {
    //         // You could add more comprehensive health checks here
    //         // like checking RabbitMQ connectivity, Redis connectivity, etc.
    //         return ResponseEntity.ok("Payment system is healthy");
            
    //     } catch (Exception e) {
    //         log.error("Health check failed", e);
    //         return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
    //                 .body("Payment system is unhealthy: " + e.getMessage());
    //     }
    // }

    /**
     * Get payment processing status - useful for debugging
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long userId) {
        try {
            // Only allow users to check their own status or admin users
            User currentUser = getCurrentUser();
            if (!currentUser.getId().equals(userId) /*&& !isAdmin(currentUser)*/) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied");
            }
            
            String stripeCustomerId = (String) redisService.get("stripe:user:" + userId);
            if (stripeCustomerId == null) {
                return ResponseEntity.ok("No Stripe customer found");
            }
            
            StripeSubCache subData = stripeService.getSubscription(stripeCustomerId);
            return ResponseEntity.ok(subData);
            
        } catch (Exception e) {
            log.error("Failed to get payment status for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get payment status: " + e.getMessage());
        }
    }
    
}
