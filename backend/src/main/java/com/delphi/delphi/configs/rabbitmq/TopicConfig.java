package com.delphi.delphi.configs.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
    public static final String CHAT_TOPIC_EXCHANGE_NAME = "chatTopicExchange";
    public static final String CHAT_TOPIC_QUEUE_NAME = "chatTopicQueue";

    public static final String CHAT_RESPONSE_TOPIC_EXCHANGE_NAME = "chatResponseTopicExchange";
    public static final String CHAT_RESPONSE_TOPIC_QUEUE_NAME = "chatResponseTopicQueue";

    // Exchange names
    public static final String STRIPE_EXCHANGE = "stripe.exchange";
    public static final String PAYMENT_DLX = "payment.dlx";
    
    // Queue names
    public static final String STRIPE_WEBHOOK_QUEUE = "stripe.webhook.queue";
    public static final String PAYMENT_PROCESSING_QUEUE = "payment.processing.queue";
    public static final String SUBSCRIPTION_UPDATE_QUEUE = "subscription.update.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    // Dead letter queues
    public static final String STRIPE_WEBHOOK_DLQ = "stripe.webhook.dlq";
    public static final String PAYMENT_PROCESSING_DLQ = "payment.processing.dlq";
    public static final String SUBSCRIPTION_UPDATE_DLQ = "subscription.update.dlq";
    
    // Routing keys
    public static final String STRIPE_WEBHOOK_ROUTING_KEY = "stripe.webhook";
    public static final String PAYMENT_PROCESSING_ROUTING_KEY = "payment.processing";
    public static final String SUBSCRIPTION_UPDATE_ROUTING_KEY = "subscription.update";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";

    /* CHAT TOPIC */
    @Bean
    public TopicExchange chatTopicExchange() {
        return new TopicExchange(CHAT_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue chatTopicQueue() {
        return new Queue(CHAT_TOPIC_QUEUE_NAME);
    }

    /*
     * Binding for chat topic queue to the chat topic exchange
     */
    @Bean
    public Binding bindingExchangeMessage(Queue chatTopicQueue, TopicExchange chatTopicExchange) {
        return BindingBuilder.bind(chatTopicQueue).to(chatTopicExchange).with("topic.chat");
    }

    /* CHAT RESPONSE TOPIC */
    @Bean
    public TopicExchange chatResponseTopicExchange() {
        return new TopicExchange(CHAT_RESPONSE_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue chatResponseTopicQueue() {
        return new Queue(CHAT_RESPONSE_TOPIC_QUEUE_NAME);
    }

    @Bean
    public Binding bindingChatResponseExchange(Queue chatResponseTopicQueue, TopicExchange chatResponseTopicExchange) {
        return BindingBuilder.bind(chatResponseTopicQueue).to(chatResponseTopicExchange).with("topic.chat.response");
    }

    /* TODO: PAYMENTS TOPIC */
    // Main exchange for Stripe events
    @Bean
    public TopicExchange stripeExchange() {
        return new TopicExchange(STRIPE_EXCHANGE);
    }

    // Dead letter exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(PAYMENT_DLX);
    }

    // Stripe webhook queue - receives all webhook events
    @Bean
    public Queue stripeWebhookQueue() {
        return QueueBuilder.durable(STRIPE_WEBHOOK_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", STRIPE_WEBHOOK_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    @Bean
    public Queue stripeWebhookDLQ() {
        return QueueBuilder.durable(STRIPE_WEBHOOK_DLQ).build();
    }

    @Bean
    public Binding stripeWebhookBinding() {
        return BindingBuilder.bind(stripeWebhookQueue())
                .to(stripeExchange())
                .with(STRIPE_WEBHOOK_ROUTING_KEY);
    }

    @Bean
    public Binding stripeWebhookDLQBinding() {
        return BindingBuilder.bind(stripeWebhookDLQ())
                .to(deadLetterExchange())
                .with(STRIPE_WEBHOOK_DLQ);
    }

    // Payment processing queue - handles payment-related events
    @Bean
    public Queue paymentProcessingQueue() {
        return QueueBuilder.durable(PAYMENT_PROCESSING_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_PROCESSING_DLQ)
                .withArgument("x-message-ttl", 600000) // 10 minutes TTL
                .build();
    }

    @Bean
    public Queue paymentProcessingDLQ() {
        return QueueBuilder.durable(PAYMENT_PROCESSING_DLQ).build();
    }

    @Bean
    public Binding paymentProcessingBinding() {
        return BindingBuilder.bind(paymentProcessingQueue())
                .to(stripeExchange())
                .with(PAYMENT_PROCESSING_ROUTING_KEY);
    }

    @Bean
    public Binding paymentProcessingDLQBinding() {
        return BindingBuilder.bind(paymentProcessingDLQ())
                .to(deadLetterExchange())
                .with(PAYMENT_PROCESSING_DLQ);
    }

    // Subscription update queue - handles subscription changes
    @Bean
    public Queue subscriptionUpdateQueue() {
        return QueueBuilder.durable(SUBSCRIPTION_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", SUBSCRIPTION_UPDATE_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    @Bean
    public Queue subscriptionUpdateDLQ() {
        return QueueBuilder.durable(SUBSCRIPTION_UPDATE_DLQ).build();
    }

    @Bean
    public Binding subscriptionUpdateBinding() {
        return BindingBuilder.bind(subscriptionUpdateQueue())
                .to(stripeExchange())
                .with(SUBSCRIPTION_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding subscriptionUpdateDLQBinding() {
        return BindingBuilder.bind(subscriptionUpdateDLQ())
                .to(deadLetterExchange())
                .with(SUBSCRIPTION_UPDATE_DLQ);
    }

    // Notification queue - handles user notifications
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(stripeExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /* TODO: EMAIL TOPIC */


    // @Bean
    // public Binding bindingExchangeMessages(Queue chatTopicQueue, TopicExchange topicExchange) {
    //     return BindingBuilder.bind(chatTopicQueue).to(topicExchange).with("topic.#");
    // }

}
