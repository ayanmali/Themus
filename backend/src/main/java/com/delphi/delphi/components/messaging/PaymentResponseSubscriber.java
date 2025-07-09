package com.delphi.delphi.components.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.PaymentResponseDto;
import com.delphi.delphi.services.PaymentStatusService;

@Component
@RabbitListener(queues = TopicConfig.PAYMENT_RESPONSE_TOPIC_QUEUE_NAME)
public class PaymentResponseSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PaymentResponseSubscriber.class);
    
    private final PaymentStatusService paymentStatusService;

    public PaymentResponseSubscriber(PaymentStatusService paymentStatusService) {
        this.paymentStatusService = paymentStatusService;
    }

    @RabbitHandler
    public void processPaymentResponse(PaymentResponseDto response) {
        log.info("Received payment response for request ID: {}", response.getRequestId());
        
        // Update status in Redis for polling-based architecture
        paymentStatusService.updateStatus(response.getRequestId(), response);
        
        if (response.isSuccess()) {
            // Handle successful response based on what data is available
            if (response.getCustomer() != null) {
                log.info("Customer created successfully for request: {} - Customer ID: {}", 
                    response.getRequestId(), response.getCustomer().getId());
            } else if (response.getCheckoutSession() != null) {
                log.info("Checkout session created successfully for request: {} - Session URL: {}", 
                    response.getRequestId(), response.getCheckoutSession().getUrl());
            } else if (response.getSubscriptionData() != null) {
                log.info("Subscription data processed successfully for request: {} - Status: {}", 
                    response.getRequestId(), response.getSubscriptionData().getStatus());
            }
        } else {
            // Handle error response
            log.error("Payment operation failed for request {}: {}", response.getRequestId(), response.getError());
        }
    }
} 