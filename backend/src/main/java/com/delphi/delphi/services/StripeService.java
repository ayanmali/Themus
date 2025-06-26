package com.delphi.delphi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.delphi.delphi.utils.SubscriptionInternal;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;

@Service
/*
 * Customers use this link to manage their subscriptions:
 * https://billing.stripe.com/p/login/00wcN50UWfN6c1D4Vc6Ri00
 */
public class StripeService {

    public StripeService(@Value("${stripe.api.key}") String stripeApiKey) {
        Stripe.apiKey = stripeApiKey;
    }

    public Customer createCustomer(Long userId, String email, String name) {
        try {
            // TODO: check if the user ID has a customer ID in redis

            // if the user ID has a customer ID in redis, use it
            // if the user ID does not have a customer ID in redis, create a new customer
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(name)
                    .setEmail(email)
                    .build();
            return Customer.create(params);
            // store user ID and customer ID in redis
            // customer.getId();

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create customer", e);
        }
    }

    public Session createCheckoutSession(String customerId, String priceId) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(customerId)
                    .setSuccessUrl("https://usedelphi.dev/checkout/success")
                    // .addLineItem(
                    // SessionCreateParams.LineItem.builder()
                    // .setPrice("price_1MotwRLkdIwHu7ixYcPLm5uZ")
                    // .setQuantity(2L)
                    // .build())
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .build();
            return Session.create(params);
            // return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create checkout session", e);
        }
    }

    public SubscriptionInternal syncStripeDataToKV(String customerId) {
        try {
            // get subscriptions for a customer
            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(customerId)
                    .build();
            SubscriptionCollection subscriptions = Subscription.list(params);

            if (!subscriptions.getData().isEmpty()) {
                // TODO: store the subscriptions in redis
                // await kv.set(`stripe:customer:${customerId}`, subData);
                return new SubscriptionInternal();
            }

            Subscription subscription = subscriptions.getData().getFirst();
            SubscriptionInternal subData = new SubscriptionInternal(subscription);
            // TODO: store the subscription in redis
            // await kv.set(`stripe:customer:${customerId}`, subData);
            return subData;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to get subscriptions", e);
        }
    }
    
}
