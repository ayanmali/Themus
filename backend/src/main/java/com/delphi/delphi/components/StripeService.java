package com.delphi.delphi.components;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.messaging.payments.PaymentMessagePublisher;
import com.delphi.delphi.dtos.messaging.payments.StripeWebhookMessage;
import com.delphi.delphi.entities.User;
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
    private final RedisService redisService;
    private final PaymentMessagePublisher paymentMessagePublisher;
    private final Logger log = LoggerFactory.getLogger(StripeService.class);

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
            "payment_intent.canceled");

    public StripeService(@Value("${stripe.api.key}") String stripeApiKey,
            @Value("${stripe.webhook.secret}") String stripeWebhookSecret, RedisService redisService,
            PaymentMessagePublisher paymentMessagePublisher) {
        Stripe.apiKey = stripeApiKey;
        Stripe.setAppInfo("Delphi", "0.0.1", "https://usedelphi.dev");

        this.stripeWebhookSecret = stripeWebhookSecret;
        this.redisService = redisService;
        this.paymentMessagePublisher = paymentMessagePublisher;
    }

    public Customer createCustomer(User user) {
        try {
            String customerId = (String) redisService.get("stripe:user:" + user.getId());

            // if the user ID has a customer ID in redis, return it
            if (customerId != null) {
                return Customer.retrieve(customerId);
            }
            // if the user ID does not have a stripe customer ID in redis, create a new
            // customer
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .setMetadata(Map.of("userId", user.getId().toString()))
                    .build();
            Customer customer = Customer.create(params);

            redisService.set("stripe:user:" + user.getId(), customer.getId());

            return customer;

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create customer", e);
        }
    }

    public Session createCheckoutSession(String customerId) {
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
                StripeSubCache subData = new StripeSubCache();
                redisService.set("stripe:customer:" + customerId, subData);
                return subData;
            }

            StripeSubCache subData = new StripeSubCache(subscriptions.getData().getFirst());
            redisService.set("stripe:customer:" + customerId, subData);
            return subData;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to get subscriptions", e);
        }
    }

    public void doEventProcessing(String signature, String body) {
        try {
            Event event = Webhook.constructEvent(body, signature, stripeWebhookSecret);

            // Create and publish webhook message to RabbitMQ
            StripeWebhookMessage webhookMessage = new StripeWebhookMessage(
                    event.getType(),
                    signature,
                    body,
                    event.getId());

            paymentMessagePublisher.publishStripeWebhookMessage(webhookMessage);

            log.info("Successfully queued Stripe webhook event: {} ({})", event.getType(), event.getId());
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            throw new RuntimeException("Invalid signature", e);
        } catch (Exception e) {
            log.error("Failed to process webhook event", e);
            throw new RuntimeException("Failed to process webhook event", e);
        }
    }

    /**
     * Processes a validated Stripe event (called by message consumers)
     */
    public StripeSubCache processValidatedEvent(Event event) {
        try {
            if (!EVENT_TYPES.contains(event.getType())) {
                log.warn("Received unsupported event type: {}", event.getType());
                return null;
            }

            // Extract the object from the event and get customer ID based on event type
            Optional<StripeObject> dataObjectOptional = event.getDataObjectDeserializer().getObject();
            if (dataObjectOptional.isEmpty()) {
                log.error("No data object in event: {}", event.getId());
                throw new RuntimeException("No data object in the event");
            }

            StripeObject dataObject = dataObjectOptional.get();
            String customerId = extractCustomerId(event.getType(), dataObject);

            if (customerId == null) {
                log.error("No customer ID found for event: {} ({})", event.getType(), event.getId());
                throw new RuntimeException("No customer ID found for this event");
            }

            // Sync stripe data to redis
            StripeSubCache result = syncStripeDataToKV(customerId);

            log.info("Successfully processed event: {} for customer: {}", event.getType(), customerId);
            return result;

        } catch (RuntimeException e) {
            log.error("Failed to process validated event: {} ({})", event.getType(), event.getId(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    private String extractCustomerId(String eventType, StripeObject dataObject) {
        String customerId = null;

        if (eventType.startsWith("customer.subscription")) {
            if (dataObject instanceof Subscription subscription) {
                customerId = subscription.getCustomer();
            }
        } else if (eventType.startsWith("invoice")) {
            if (dataObject instanceof Invoice invoice) {
                customerId = invoice.getCustomer();
            }
        } else if (eventType.startsWith("payment_intent")) {
            if (dataObject instanceof PaymentIntent paymentIntent) {
                customerId = paymentIntent.getCustomer();
            }
        } else if (eventType.startsWith("checkout.session")) {
            if (dataObject instanceof Session session) {
                customerId = session.getCustomer();
            }
        }

        return customerId;
    }

    public StripeSubCache getSubscription(Long userId) {
        String stripeCustomerId = (String) redisService.get("stripe:user:" + userId);
        if (stripeCustomerId == null) {
            throw new RuntimeException("No Stripe customer ID found for this user");
        }
        return getSubscription(stripeCustomerId);
    }

    public StripeSubCache getSubscription(String customerId) {
        return (StripeSubCache) redisService.get("stripe:customer:" + customerId);
    }

    /**
     * Get user ID from Stripe customer ID
     */
    public Long getUserIdFromCustomerId(String customerId) {
        try {
            Customer customer = Customer.retrieve(customerId);
            String userId = customer.getMetadata().get("userId");
            return userId != null ? Long.valueOf(userId) : null;
        } catch (StripeException e) {
            log.error("Failed to retrieve customer: {}", customerId, e);
            return null;
        }
    }

}