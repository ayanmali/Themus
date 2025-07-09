package com.delphi.delphi.components.messaging.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.payments.NotificationMessage;
import com.delphi.delphi.dtos.messaging.payments.PaymentProcessingMessage;
import com.delphi.delphi.dtos.messaging.payments.StripeWebhookMessage;
import com.delphi.delphi.dtos.messaging.payments.SubscriptionUpdateMessage;

@Service
public class PaymentMessagePublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentMessagePublisher.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public PaymentMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publishStripeWebhookMessage(StripeWebhookMessage message) {
        try {
            logger.info("Publishing Stripe webhook message for event: {}", message.getEventType());
            rabbitTemplate.convertAndSend(
                TopicConfig.STRIPE_EXCHANGE,
                TopicConfig.STRIPE_WEBHOOK_ROUTING_KEY,
                message
            );
            logger.debug("Successfully published Stripe webhook message: {}", message.getMessageId());
        } catch (AmqpException e) {
            logger.error("Failed to publish Stripe webhook message: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to publish Stripe webhook message", e);
        }
    }
    
    public void publishPaymentProcessingMessage(PaymentProcessingMessage message) {
        try {
            logger.info("Publishing payment processing message for customer: {}", message.getCustomerId());
            rabbitTemplate.convertAndSend(
                TopicConfig.STRIPE_EXCHANGE,
                TopicConfig.PAYMENT_PROCESSING_ROUTING_KEY,
                message
            );
            logger.debug("Successfully published payment processing message: {}", message.getMessageId());
        } catch (AmqpException e) {
            logger.error("Failed to publish payment processing message: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to publish payment processing message", e);
        }
    }
    
    public void publishSubscriptionUpdateMessage(SubscriptionUpdateMessage message) {
        try {
            logger.info("Publishing subscription update message for customer: {}", message.getCustomerId());
            rabbitTemplate.convertAndSend(
                TopicConfig.STRIPE_EXCHANGE,
                TopicConfig.SUBSCRIPTION_UPDATE_ROUTING_KEY,
                message
            );
            logger.debug("Successfully published subscription update message: {}", message.getMessageId());
        } catch (AmqpException e) {
            logger.error("Failed to publish subscription update message: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to publish subscription update message", e);
        }
    }
    
    public void publishNotificationMessage(NotificationMessage message) {
        try {
            logger.info("Publishing notification message for user: {}", message.getUserId());
            rabbitTemplate.convertAndSend(
                TopicConfig.STRIPE_EXCHANGE,
                TopicConfig.NOTIFICATION_ROUTING_KEY,
                message
            );
            logger.debug("Successfully published notification message: {}", message.getMessageId());
        } catch (AmqpException e) {
            logger.error("Failed to publish notification message: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to publish notification message", e);
        }
    }
    
    // Helper method to publish notification based on event type
    public void publishPaymentNotification(Long userId, String eventType, String customerId) {
        String title;
        String message;
        
        switch (eventType) {
            case "payment_intent.succeeded" -> {
                title = "Payment Successful";
                message = "Your payment has been processed successfully.";
            }
            case "payment_intent.payment_failed" -> {
                title = "Payment Failed";
                message = "We were unable to process your payment. Please update your payment method.";
            }
            case "customer.subscription.created" -> {
                title = "Subscription Active";
                message = "Your subscription has been activated successfully.";
            }
            case "customer.subscription.deleted" -> {
                title = "Subscription Cancelled";
                message = "Your subscription has been cancelled.";
            }
            case "invoice.payment_failed" -> {
                title = "Payment Failed";
                message = "We couldn't process your subscription payment. Please update your payment method.";
            }
            default -> {
                return; // Don't send notification for other events
            }
        }
        
        NotificationMessage notificationMessage = new NotificationMessage(
            userId, 
            "payment", 
            title, 
            message
        );
        
        publishNotificationMessage(notificationMessage);
    }
}
