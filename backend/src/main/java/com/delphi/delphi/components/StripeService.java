package com.delphi.delphi.components;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.delphi.delphi.utils.payments.StripeSubCache;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;

@Component
/*
 * Customers use this link to manage their subscriptions:
 * https://billing.stripe.com/p/login/00wcN50UWfN6c1D4Vc6Ri00
 */
public class StripeService {

    private final String stripeWebhookSecret;

    public static final List<String> EVENT_TYPES = List.of(
            "checkout.session.completed",
            "customer.subscription.created",
            "customer.subscription.updated",
            "customer.subscription.deleted",
            "customer.subscription.paused",
            "customer.subscription.resumed",
            "customer.subscription.pending_update_applied",
            "customer.subscription.pending_update_expired",
            "customer.subscription.trial_will_end",
            "invoice.paid",
            "invoice.payment_failed",
            "invoice.payment_action_required",
            "invoice.upcoming",
            "invoice.marked_uncollectible",
            "invoice.payment_succeeded",
            "payment_intent.succeeded",
            "payment_intent.payment_failed",
            "payment_intent.canceled"
    );

    public StripeService(@Value("${stripe.api.key}") String stripeApiKey, @Value("${stripe.webhook.secret}") String stripeWebhookSecret) {
        Stripe.apiKey = stripeApiKey;
        Stripe.setAppInfo("Delphi", "0.0.1", "https://usedelphi.dev");
        this.stripeWebhookSecret = stripeWebhookSecret;
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

    public StripeSubCache syncStripeDataToKV(String customerId) {
        try {
            // get subscriptions for a customer
            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(customerId)
                    .build();
            SubscriptionCollection subscriptions = Subscription.list(params);

            if (!subscriptions.getData().isEmpty()) {
                // TODO: store the subscriptions in redis
                // await kv.set(`stripe:customer:${customerId}`, subData);
                return new StripeSubCache();
            }

            Subscription subscription = subscriptions.getData().getFirst();
            StripeSubCache subData = new StripeSubCache(subscription);
            // TODO: store the subscription in redis
            // await kv.set(`stripe:customer:${customerId}`, subData);
            return subData;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to get subscriptions", e);
        }
    }

    public void doEventProcessing(String signature, String body) {
        try {
            Event event = Webhook.constructEvent(body, signature, stripeWebhookSecret);
            processEvent(event);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid signature", e);
        }
    }

    private StripeSubCache processEvent(Event event) {
        if (!EVENT_TYPES.contains(event.getType())) {
            return null;
        }

        // Extract the object from the event and get customer ID based on event type
        Optional<StripeObject> dataObjectOptional = event.getDataObjectDeserializer().getObject();
        if (dataObjectOptional.isEmpty()) {
            throw new RuntimeException("No data object in the event");
        }

        StripeObject dataObject = dataObjectOptional.get();

        String customerId = null;

        if (event.getType().startsWith("customer.subscription")) {
            if (dataObject instanceof Subscription subscription) {
                customerId = subscription.getCustomer();
            }
        }

        if (event.getType().startsWith("invoice")) {
            if (dataObject instanceof Invoice invoice) {
                customerId = invoice.getCustomer();
            }
        }

        if (event.getType().startsWith("payment_intent")) {
            if (dataObject instanceof PaymentIntent paymentIntent) {
                customerId = paymentIntent.getCustomer();
            }
        }

        if (event.getType().startsWith("checkout.session")) {
            if (dataObject instanceof Session session) {
                customerId = session.getCustomer();
            }
        }

        if (customerId == null) {
            throw new RuntimeException("No customer ID found for this event");
        }

        // sync stripe data to redis
        return syncStripeDataToKV(customerId);
    }

}