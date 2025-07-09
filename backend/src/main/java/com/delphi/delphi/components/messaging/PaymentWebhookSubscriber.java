package com.delphi.delphi.components.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.StripeService;
import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.StripeWebhookDto;

@Component
@RabbitListener(queues = TopicConfig.PAYMENT_WEBHOOK_TOPIC_QUEUE_NAME)
public class PaymentWebhookSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookSubscriber.class);
    
    private final StripeService stripeService;

    public PaymentWebhookSubscriber(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @RabbitHandler
    public void processStripeWebhook(StripeWebhookDto webhook) {
        log.info("Processing Stripe webhook with request ID: {}", webhook.getRequestId());
        
        try {
            // Process the webhook using the existing StripeService method
            stripeService.doEventProcessing(webhook.getSignature(), webhook.getBody());
            
            log.info("Successfully processed Stripe webhook for request ID: {}", webhook.getRequestId());
            
            // Optional: You could publish a response to confirm webhook processing
            // or trigger additional business logic based on the webhook event
            
        } catch (RuntimeException e) {
            log.error("Failed to process Stripe webhook for request ID: {}: {}", 
                webhook.getRequestId(), e.getMessage(), e);
            
            // Optional: Implement retry logic or dead letter queue for failed webhooks
            // For now, we'll just log the error. In production, you might want to:
            // 1. Retry with exponential backoff
            // 2. Send to a dead letter queue after max retries
            // 3. Alert monitoring systems
            
            throw e; // Re-throw to potentially trigger retry mechanism
        }
    }
} 