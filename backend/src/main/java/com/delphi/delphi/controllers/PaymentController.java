package com.delphi.delphi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.services.StripeService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> stripeWebhook(@RequestHeader(name = "Stripe-Signature", required = true) String signature, @RequestBody String body) {
        // TODO: implement this
        // verify the signature
        // if (!stripeService.verifySignature(signature)) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        // }

        // get the event
        // Event event = stripeService.getEvent(signature);
        return ResponseEntity.ok("Stripe webhook received");
    }

    @GetMapping("/{userId}/checkout/success")
    public ResponseEntity<?> checkoutSuccess(@PathVariable Long userId) {
        // TODO: implement this
        // get stripe customer id from redis
        // String stripeCustomerId = await kv.get(`stripe:user:${user.id}`);

        // if stripe customer id is not found, return redirect to home page
        // if (!stripeCustomerId) {
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Stripe customer ID not found");
        //     return redirect("/");
        // }

        // sync stripe data to redis
        // await syncStripeDataToKV(stripeCustomerId);

        // return redirect to home page
        // return redirect("/");
        return ResponseEntity.ok("Checkout success");
    }
    
}
