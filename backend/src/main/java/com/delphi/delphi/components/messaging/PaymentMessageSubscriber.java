package com.delphi.delphi.components.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.StripeService;
import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.PaymentRequestDto;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.payments.StripeSubCache;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;

@Component
@RabbitListener(queues = TopicConfig.PAYMENT_TOPIC_QUEUE_NAME)
public class PaymentMessageSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PaymentMessageSubscriber.class);
    
    private final StripeService stripeService;
    private final UserService userService;
    private final PaymentMessagePublisher paymentMessagePublisher;

    public PaymentMessageSubscriber(StripeService stripeService, UserService userService, PaymentMessagePublisher paymentMessagePublisher) {
        this.stripeService = stripeService;
        this.userService = userService;
        this.paymentMessagePublisher = paymentMessagePublisher;
    }

    @RabbitHandler
    public void processPaymentRequest(PaymentRequestDto request) {
        log.info("Processing payment request with ID: {} for operation: {}", request.getRequestId(), request.getOperation());
        
        try {
            switch (request.getOperation()) {
                case CREATE_CUSTOMER:
                    processCreateCustomer(request);
                    break;
                case CREATE_CHECKOUT_SESSION:
                    processCreateCheckoutSession(request);
                    break;
                case SYNC_SUBSCRIPTION_DATA:
                    processSyncSubscriptionData(request);
                    break;
                case GET_SUBSCRIPTION:
                    processGetSubscription(request);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown payment operation: " + request.getOperation());
            }
            
        } catch (Exception e) {
            log.error("Error processing payment request {}: {}", request.getRequestId(), e.getMessage(), e);
            paymentMessagePublisher.publishPaymentError(request.getRequestId(), e.getMessage());
        }
    }

    private void processCreateCustomer(PaymentRequestDto request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for creating customer");
        }

        User user = userService.getUserByIdOrThrow(request.getUserId());
        Customer customer = stripeService.createCustomer(user);
        
        paymentMessagePublisher.publishPaymentResponse(request.getRequestId(), customer);
    }

    private void processCreateCheckoutSession(PaymentRequestDto request) {
        if (request.getCustomerId() == null || request.getCustomerId().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required for creating checkout session");
        }

        Session session = stripeService.createCheckoutSession(request.getCustomerId());
        
        paymentMessagePublisher.publishPaymentResponse(request.getRequestId(), session);
    }

    private void processSyncSubscriptionData(PaymentRequestDto request) {
        if (request.getCustomerId() == null || request.getCustomerId().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required for syncing subscription data");
        }

        StripeSubCache subscriptionData = stripeService.syncStripeDataToKV(request.getCustomerId());
        
        paymentMessagePublisher.publishPaymentResponse(request.getRequestId(), subscriptionData);
    }

    private void processGetSubscription(PaymentRequestDto request) {
        StripeSubCache subscriptionData;
        
        if (request.getUserId() != null) {
            subscriptionData = stripeService.getSubscription(request.getUserId());
        } else if (request.getCustomerId() != null && !request.getCustomerId().isEmpty()) {
            subscriptionData = stripeService.getSubscription(request.getCustomerId());
        } else {
            throw new IllegalArgumentException("Either User ID or Customer ID is required for getting subscription");
        }
        
        paymentMessagePublisher.publishPaymentResponse(request.getRequestId(), subscriptionData);
    }
} 