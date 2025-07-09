package com.delphi.delphi.components.messaging.payments;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.StripeService;
import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.payments.NotificationMessage;
import com.delphi.delphi.dtos.messaging.payments.PaymentProcessingMessage;
import com.delphi.delphi.dtos.messaging.payments.StripeWebhookMessage;
import com.delphi.delphi.dtos.messaging.payments.SubscriptionUpdateMessage;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@Component
public class PaymentMessageConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentMessageConsumer.class);
    
    private final StripeService stripeService;
    private final PaymentMessagePublisher messagePublisher;
    private final String stripeWebhookSecret;
    
    public PaymentMessageConsumer(StripeService stripeService, PaymentMessagePublisher messagePublisher, @Value("${stripe.webhook.secret}") String stripeWebhookSecret) {
        this.stripeService = stripeService;
        this.messagePublisher = messagePublisher;
        this.stripeWebhookSecret = stripeWebhookSecret;
    }
    
    @RabbitListener(queues = TopicConfig.STRIPE_WEBHOOK_QUEUE)
    public void handleStripeWebhookMessage(StripeWebhookMessage message) {
        log.info("Processing Stripe webhook message: {} ({})", message.getEventType(), message.getMessageId());
        
        try {
            // Parse the event from the webhook body
            Event event = Webhook.constructEvent(
                message.getBody(), 
                message.getSignature(), 
                stripeWebhookSecret
            );
            
            // Process the validated event
            stripeService.processValidatedEvent(event);
            
            // Route to appropriate specialized queues based on event type
            routeEventToSpecializedQueues(event);
            
            log.info("Successfully processed webhook message: {}", message.getMessageId());
            
        } catch (SignatureVerificationException e) {
            log.error("Failed to process webhook message: {}", message.getMessageId(), e);
            
            // Increment retry count and re-throw to trigger retry mechanism
            message.incrementRetryCount();
            if (message.getRetryCount() < 3) {
                throw new RuntimeException("Retryable error processing webhook", e);
            } else {
                log.error("Max retries reached for webhook message: {}", message.getMessageId());
                // Message will be sent to DLQ
            }
        }
    }
    
    private void routeEventToSpecializedQueues(Event event) {
        String eventType = event.getType();
        String customerId = extractCustomerId(event);
        
        if (customerId == null) {
            log.warn("No customer ID found for event: {}", event.getId());
            return;
        }
        
        Long userId = stripeService.getUserIdFromCustomerId(customerId);
        
        // Route payment-related events
        if (eventType.startsWith("payment_intent") || eventType.startsWith("invoice")) {
            PaymentProcessingMessage paymentMessage = createPaymentMessage(event, customerId, userId);
            if (paymentMessage != null) {
                messagePublisher.publishPaymentProcessingMessage(paymentMessage);
            }
        }
        
        // Route subscription-related events
        if (eventType.startsWith("customer.subscription")) {
            SubscriptionUpdateMessage subscriptionMessage = createSubscriptionMessage(event, customerId, userId);
            if (subscriptionMessage != null) {
                messagePublisher.publishSubscriptionUpdateMessage(subscriptionMessage);
            }
        }
        
        // Send notification for important events
        if (userId != null && shouldSendNotification(eventType)) {
            messagePublisher.publishPaymentNotification(userId, eventType, customerId);
        }
    }
    
    @RabbitListener(queues = TopicConfig.PAYMENT_PROCESSING_QUEUE)
    public void handlePaymentProcessingMessage(PaymentProcessingMessage message) {
        log.info("Processing payment message: {} for customer: {}", 
                   message.getEventType(), message.getCustomerId());
        
        try {
            // Handle payment-specific logic here
            processPaymentEvent(message);
            
            log.info("Successfully processed payment message: {}", message.getMessageId());
            
        } catch (Exception e) {
            log.error("Failed to process payment message: {}", message.getMessageId(), e);
            
            message.incrementRetryCount();
            if (message.getRetryCount() < 3) {
                throw new RuntimeException("Retryable error processing payment", e);
            } else {
                log.error("Max retries reached for payment message: {}", message.getMessageId());
            }
        }
    }
    
    @RabbitListener(queues = TopicConfig.SUBSCRIPTION_UPDATE_QUEUE)
    public void handleSubscriptionUpdateMessage(SubscriptionUpdateMessage message) {
        log.info("Processing subscription update: {} for customer: {}", 
                   message.getEventType(), message.getCustomerId());
        
        try {
            // Handle subscription-specific logic here
            processSubscriptionEvent(message);
            
            log.info("Successfully processed subscription message: {}", message.getMessageId());
            
        } catch (Exception e) {
            log.error("Failed to process subscription message: {}", message.getMessageId(), e);
            
            message.incrementRetryCount();
            if (message.getRetryCount() < 3) {
                throw new RuntimeException("Retryable error processing subscription", e);
            } else {
                log.error("Max retries reached for subscription message: {}", message.getMessageId());
            }
        }
    }
    
    @RabbitListener(queues = TopicConfig.NOTIFICATION_QUEUE)
    public void handleNotificationMessage(NotificationMessage message) {
        log.info("Processing notification: {} for user: {}", 
                   message.getType(), message.getUserId());
        
        try {
            // Handle notification logic here (email, push notifications, etc.)
            processNotification(message);
            
            log.info("Successfully processed notification message: {}", message.getMessageId());
            
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message.getMessageId(), e);
            // Notifications are typically not retried to avoid spam
        }
    }
    
    private String extractCustomerId(Event event) {
        try {
            Optional<StripeObject> dataObjectOptional = event.getDataObjectDeserializer().getObject();
            if (dataObjectOptional.isEmpty()) {
                return null;
            }
            
            StripeObject dataObject = dataObjectOptional.get();
            String eventType = event.getType();
            
            if (eventType.startsWith("customer.subscription") && dataObject instanceof Subscription subscription) {
                return subscription.getCustomer();
            } else if (eventType.startsWith("invoice") && dataObject instanceof Invoice invoice) {
                return invoice.getCustomer();
            } else if (eventType.startsWith("payment_intent") && dataObject instanceof PaymentIntent paymentIntent) {
                return paymentIntent.getCustomer();
            } else if (eventType.startsWith("checkout.session") && dataObject instanceof Session session) {
                return session.getCustomer();
            }
            
            return null;
        } catch (Exception e) {
            log.error("Failed to extract customer ID from event: {}", event.getId(), e);
            return null;
        }
    }
    
    private PaymentProcessingMessage createPaymentMessage(Event event, String customerId, Long userId) {
        try {
            PaymentProcessingMessage message = new PaymentProcessingMessage(customerId, userId, event.getType());
            
            Optional<StripeObject> dataObjectOptional = event.getDataObjectDeserializer().getObject();
            if (dataObjectOptional.isPresent()) {
                StripeObject dataObject = dataObjectOptional.get();
                
                switch (dataObject) {
                    case PaymentIntent paymentIntent -> {
                        message.setPaymentIntentId(paymentIntent.getId());
                        message.setAmount(paymentIntent.getAmount());
                        message.setCurrency(paymentIntent.getCurrency());
                        message.setStatus(paymentIntent.getStatus());
                        message.setMetadata(paymentIntent.getMetadata());
                    }
                    case Invoice invoice -> {
                        message.setAmount(invoice.getAmountPaid());
                        message.setCurrency(invoice.getCurrency());
                        message.setStatus(invoice.getStatus());
                        message.setMetadata(invoice.getMetadata());
                    }
                    default -> {
                    }
                }
            }
            
            return message;
        } catch (Exception e) {
            log.error("Failed to create payment message for event: {}", event.getId(), e);
            return null;
        }
    }
    
    private SubscriptionUpdateMessage createSubscriptionMessage(Event event, String customerId, Long userId) {
        try {
            SubscriptionUpdateMessage message = new SubscriptionUpdateMessage(customerId, userId, null, event.getType());
            
            Optional<StripeObject> dataObjectOptional = event.getDataObjectDeserializer().getObject();
            if (dataObjectOptional.isPresent() && dataObjectOptional.get() instanceof Subscription subscription) {
                message.setSubscriptionId(subscription.getId());
                message.setStatus(subscription.getStatus());
                message.setCurrentPeriodStart(subscription.getItems().getData().getFirst().getCurrentPeriodStart());
                message.setCurrentPeriodEnd(subscription.getItems().getData().getFirst().getCurrentPeriodEnd());
                message.setMetadata(subscription.getMetadata());
                
                if (!subscription.getItems().getData().isEmpty()) {
                    message.setPriceId(subscription.getItems().getData().get(0).getPrice().getId());
                }
            }
            
            return message;
        } catch (Exception e) {
            log.error("Failed to create subscription message for event: {}", event.getId(), e);
            return null;
        }
    }
    
    private boolean shouldSendNotification(String eventType) {
        return eventType.equals("payment_intent.succeeded") ||
               eventType.equals("payment_intent.payment_failed") ||
               eventType.equals("customer.subscription.created") ||
               eventType.equals("customer.subscription.deleted") ||
               eventType.equals("invoice.payment_failed");
    }
    
    private void processPaymentEvent(PaymentProcessingMessage message) {
        // Implement payment-specific business logic
        log.info("Processing payment event: {} for customer: {}", 
                   message.getEventType(), message.getCustomerId());
        
        // Example: Update user credits, send analytics events, etc.
        switch (message.getEventType()) {
            case "payment_intent.succeeded" -> // Handle successful payment
                handleSuccessfulPayment(message);
            case "payment_intent.payment_failed" -> // Handle failed payment
                handleFailedPayment(message);
            case "invoice.payment_succeeded" -> // Handle successful subscription payment
                handleSuccessfulSubscriptionPayment(message);
            case "invoice.payment_failed" -> // Handle failed subscription payment
                handleFailedSubscriptionPayment(message);
        }
    }
    
    private void processSubscriptionEvent(SubscriptionUpdateMessage message) {
        // Implement subscription-specific business logic
        log.info("Processing subscription event: {} for customer: {}", 
                   message.getEventType(), message.getCustomerId());
        
        // Example: Update user subscription status, manage feature access, etc.
        switch (message.getEventType()) {
            case "customer.subscription.created" -> // Handle new subscription
                handleNewSubscription(message);
            case "customer.subscription.updated" -> // Handle subscription changes
                handleSubscriptionUpdate(message);
            case "customer.subscription.deleted" -> // Handle subscription cancellation
                handleSubscriptionCancellation(message);
        }
    }
    
    private void processNotification(NotificationMessage message) {
        // Implement notification logic (email, push notifications, etc.)
        log.info("Sending notification: {} to user: {}", 
                   message.getTitle(), message.getUserId());
        
        // Example: Send email, push notification, in-app notification, etc.
        // You would integrate with your notification service here
    }
    
    // Placeholder methods for specific event handling
    private void handleSuccessfulPayment(PaymentProcessingMessage message) {
        // Implement successful payment logic
    }
    
    private void handleFailedPayment(PaymentProcessingMessage message) {
        // Implement failed payment logic
    }
    
    private void handleSuccessfulSubscriptionPayment(PaymentProcessingMessage message) {
        // Implement successful subscription payment logic
    }
    
    private void handleFailedSubscriptionPayment(PaymentProcessingMessage message) {
        // Implement failed subscription payment logic
    }
    
    private void handleNewSubscription(SubscriptionUpdateMessage message) {
        // Implement new subscription logic
    }
    
    private void handleSubscriptionUpdate(SubscriptionUpdateMessage message) {
        // Implement subscription update logic
    }
    
    private void handleSubscriptionCancellation(SubscriptionUpdateMessage message) {
        // Implement subscription cancellation logic
    }
}
