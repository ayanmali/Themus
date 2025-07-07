package com.delphi.delphi.controllers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/{initiate-checkout")
    public ResponseEntity<?> initiateStripeCheckout() {
        User user = getCurrentUser();
        Customer customer = stripeService.createCustomer(user);
        Session session = stripeService.createCheckoutSession(customer.getId());
        return ResponseEntity.ok(session.getUrl());
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> stripeWebhook(
            @RequestHeader(name = "Stripe-Signature", required = true) String signature, 
            @RequestBody String body) {
        
        try {
            stripeService.doEventProcessing(signature, body);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
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
    
}
