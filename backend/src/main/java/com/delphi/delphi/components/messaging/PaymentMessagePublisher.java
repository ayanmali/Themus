package com.delphi.delphi.components.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.PaymentRequestDto;
import com.delphi.delphi.dtos.messaging.PaymentRequestDto.PaymentOperation;
import com.delphi.delphi.dtos.messaging.PaymentResponseDto;
import com.delphi.delphi.dtos.messaging.StripeWebhookDto;
import com.delphi.delphi.utils.payments.StripeSubCache;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;

@Component
public class PaymentMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Logger log = LoggerFactory.getLogger(PaymentMessagePublisher.class);

    public PaymentMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Publish request to create a customer
    public String publishCreateCustomerRequest(Long userId) {
        String requestId = UUID.randomUUID().toString();
        PaymentRequestDto request = new PaymentRequestDto(PaymentOperation.CREATE_CUSTOMER, userId, requestId);

        log.info("Publishing create customer request with ID: {} for userId: {}", requestId, userId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_TOPIC_EXCHANGE_NAME, 
            "topic.payment.customer.create", 
            request
        );
        
        return requestId;
    }

    // Publish request to create checkout session
    public String publishCreateCheckoutSessionRequest(String customerId) {
        String requestId = UUID.randomUUID().toString();
        PaymentRequestDto request = new PaymentRequestDto(PaymentOperation.CREATE_CHECKOUT_SESSION, customerId, requestId);

        log.info("Publishing create checkout session request with ID: {} for customerId: {}", requestId, customerId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_TOPIC_EXCHANGE_NAME, 
            "topic.payment.checkout.create", 
            request
        );
        
        return requestId;
    }

    // Publish request to sync subscription data
    public String publishSyncSubscriptionDataRequest(String customerId) {
        String requestId = UUID.randomUUID().toString();
        PaymentRequestDto request = new PaymentRequestDto(PaymentOperation.SYNC_SUBSCRIPTION_DATA, customerId, requestId);

        log.info("Publishing sync subscription data request with ID: {} for customerId: {}", requestId, customerId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_TOPIC_EXCHANGE_NAME, 
            "topic.payment.subscription.sync", 
            request
        );
        
        return requestId;
    }

    // Publish request to get subscription
    public String publishGetSubscriptionRequest(Long userId) {
        String requestId = UUID.randomUUID().toString();
        PaymentRequestDto request = new PaymentRequestDto(PaymentOperation.GET_SUBSCRIPTION, userId, requestId);

        log.info("Publishing get subscription request with ID: {} for userId: {}", requestId, userId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_TOPIC_EXCHANGE_NAME, 
            "topic.payment.subscription.get", 
            request
        );
        
        return requestId;
    }

    // Publish Stripe webhook for async processing
    public String publishStripeWebhook(String signature, String body) {
        String requestId = UUID.randomUUID().toString();
        StripeWebhookDto webhook = new StripeWebhookDto(signature, body, requestId);

        log.info("Publishing Stripe webhook for async processing with ID: {}", requestId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_WEBHOOK_TOPIC_EXCHANGE_NAME, 
            "topic.payment.webhook.stripe", 
            webhook
        );
        
        return requestId;
    }

    // Publish successful payment responses
    public void publishPaymentResponse(String requestId, Customer customer) {
        PaymentResponseDto response = new PaymentResponseDto(requestId, customer);
        
        log.info("Publishing payment response (customer) for request ID: {}", requestId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_RESPONSE_TOPIC_EXCHANGE_NAME,
            "topic.payment.response.customer",
            response
        );
    }

    public void publishPaymentResponse(String requestId, Session checkoutSession) {
        PaymentResponseDto response = new PaymentResponseDto(requestId, checkoutSession);
        
        log.info("Publishing payment response (checkout session) for request ID: {}", requestId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_RESPONSE_TOPIC_EXCHANGE_NAME,
            "topic.payment.response.checkout",
            response
        );
    }

    public void publishPaymentResponse(String requestId, StripeSubCache subscriptionData) {
        PaymentResponseDto response = new PaymentResponseDto(requestId, subscriptionData);
        
        log.info("Publishing payment response (subscription data) for request ID: {}", requestId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_RESPONSE_TOPIC_EXCHANGE_NAME,
            "topic.payment.response.subscription",
            response
        );
    }

    // Publish payment error
    public void publishPaymentError(String requestId, String error) {
        PaymentResponseDto response = new PaymentResponseDto(requestId, error);
        
        log.error("Publishing payment error for request ID: {}: {}", requestId, error);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.PAYMENT_RESPONSE_TOPIC_EXCHANGE_NAME,
            "topic.payment.response.error",
            response
        );
    }
} 