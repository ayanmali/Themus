package com.delphi.delphi.configs.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
    public static final String CHAT_TOPIC_EXCHANGE_NAME = "chat.topic";
    public static final String CHAT_TOPIC_QUEUE_NAME = "chat.queue";

    // public static final String CHAT_TOPIC_DLX = "chat.topic.dlx";
    // public static final String CHAT_TOPIC_DLQ = "chat.topic.dlq";

    public static final String CHAT_RESPONSE_TOPIC_EXCHANGE_NAME = "chat.response.topic";
    public static final String CHAT_RESPONSE_TOPIC_QUEUE_NAME = "chat.response.queue";

    // public static final String CHAT_RESPONSE_TOPIC_DLX = "chat.response.topic.dlx";
    // public static final String CHAT_RESPONSE_TOPIC_DLQ = "chat.response.topic.dlq";

    // // Exchange names
    // public static final String STRIPE_EXCHANGE = "stripe.exchange";
    // public static final String PAYMENT_DLX = "payment.dlx";
    
    // // Queue names
    // public static final String STRIPE_WEBHOOK_QUEUE = "stripe.webhook.queue";
    // public static final String PAYMENT_PROCESSING_QUEUE = "payment.processing.queue";
    // public static final String SUBSCRIPTION_UPDATE_QUEUE = "subscription.update.queue";
    // public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    // // Dead letter queues
    // public static final String STRIPE_WEBHOOK_DLQ = "stripe.webhook.dlq";
    // public static final String PAYMENT_PROCESSING_DLQ = "payment.processing.dlq";
    // public static final String SUBSCRIPTION_UPDATE_DLQ = "subscription.update.dlq";
    
    // // Routing keys
    // public static final String STRIPE_WEBHOOK_ROUTING_KEY = "stripe.webhook";
    // public static final String PAYMENT_PROCESSING_ROUTING_KEY = "payment.processing";
    // public static final String SUBSCRIPTION_UPDATE_ROUTING_KEY = "subscription.update";
    // public static final String NOTIFICATION_ROUTING_KEY = "notification";

    // public static final String EMAIL_TOPIC_EXCHANGE_NAME = "email.exchange";
    // public static final String EMAIL_DLX = "email.dlx";
    // public static final String EMAIL_TOPIC_QUEUE_NAME = "email.queue";
    // public static final String EMAIL_ROUTING_KEY = "resend.email";
    // public static final String EMAIL_DLQ = "email.dlq";

    /* CHAT TOPIC */
    @Bean
    public TopicExchange chatTopicExchange() {
        return new TopicExchange(CHAT_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue chatTopicQueue() {
        return QueueBuilder.durable(CHAT_TOPIC_QUEUE_NAME)
        .deadLetterExchange(CHAT_TOPIC_EXCHANGE_NAME)
        .deadLetterRoutingKey(CHAT_TOPIC_QUEUE_NAME)
        .ttl(30000) // 5 minutes
        .build();
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
        return QueueBuilder.durable(CHAT_RESPONSE_TOPIC_QUEUE_NAME)
        .build();
    }

    @Bean
    public Binding bindingChatResponseExchange(Queue chatResponseTopicQueue, TopicExchange chatResponseTopicExchange) {
        return BindingBuilder.bind(chatResponseTopicQueue).to(chatResponseTopicExchange).with("topic.chat.response");
    }

    /* PAYMENTS TOPIC */
    // Main exchange for Stripe events
    // @Bean
    // public TopicExchange stripeExchange() {
    //     return new TopicExchange(STRIPE_EXCHANGE);
    // }

    // // Dead letter exchange
    // @Bean
    // public DirectExchange stripeDeadLetterExchange() {
    //     return new DirectExchange(PAYMENT_DLX);
    // }

    // // Stripe webhook queue - receives all webhook events
    // @Bean
    // public Queue stripeWebhookQueue() {
    //     return QueueBuilder.durable(STRIPE_WEBHOOK_QUEUE)
    //             .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
    //             .withArgument("x-dead-letter-routing-key", STRIPE_WEBHOOK_DLQ)
    //             .withArgument("x-message-ttl", 300000) // 5 minutes TTL
    //             .build();
    // }

    // @Bean
    // public Queue stripeWebhookDLQ() {
    //     return QueueBuilder.durable(STRIPE_WEBHOOK_DLQ).build();
    // }

    // @Bean
    // public Binding stripeWebhookBinding() {
    //     return BindingBuilder.bind(stripeWebhookQueue())
    //             .to(stripeExchange())
    //             .with(STRIPE_WEBHOOK_ROUTING_KEY);
    // }

    // @Bean
    // public Binding stripeWebhookDLQBinding() {
    //     return BindingBuilder.bind(stripeWebhookDLQ())
    //             .to(stripeDeadLetterExchange())
    //             .with(STRIPE_WEBHOOK_DLQ);
    // }

    // // Payment processing queue - handles payment-related events
    // @Bean
    // public Queue paymentProcessingQueue() {
    //     return QueueBuilder.durable(PAYMENT_PROCESSING_QUEUE)
    //             .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
    //             .withArgument("x-dead-letter-routing-key", PAYMENT_PROCESSING_DLQ)
    //             .withArgument("x-message-ttl", 600000) // 10 minutes TTL
    //             .build();
    // }

    // @Bean
    // public Queue paymentProcessingDLQ() {
    //     return QueueBuilder.durable(PAYMENT_PROCESSING_DLQ).build();
    // }

    // @Bean
    // public Binding paymentProcessingBinding() {
    //     return BindingBuilder.bind(paymentProcessingQueue())
    //             .to(stripeExchange())
    //             .with(PAYMENT_PROCESSING_ROUTING_KEY);
    // }

    // @Bean
    // public Binding paymentProcessingDLQBinding() {
    //     return BindingBuilder.bind(paymentProcessingDLQ())
    //             .to(stripeDeadLetterExchange())
    //             .with(PAYMENT_PROCESSING_DLQ);
    // }

    // // Subscription update queue - handles subscription changes
    // @Bean
    // public Queue subscriptionUpdateQueue() {
    //     return QueueBuilder.durable(SUBSCRIPTION_UPDATE_QUEUE)
    //             .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
    //             .withArgument("x-dead-letter-routing-key", SUBSCRIPTION_UPDATE_DLQ)
    //             .withArgument("x-message-ttl", 300000) // 5 minutes TTL
    //             .build();
    // }

    // @Bean
    // public Queue subscriptionUpdateDLQ() {
    //     return QueueBuilder.durable(SUBSCRIPTION_UPDATE_DLQ).build();
    // }

    // @Bean
    // public Binding subscriptionUpdateBinding() {
    //     return BindingBuilder.bind(subscriptionUpdateQueue())
    //             .to(stripeExchange())
    //             .with(SUBSCRIPTION_UPDATE_ROUTING_KEY);
    // }

    // @Bean
    // public Binding subscriptionUpdateDLQBinding() {
    //     return BindingBuilder.bind(subscriptionUpdateDLQ())
    //             .to(stripeDeadLetterExchange())
    //             .with(SUBSCRIPTION_UPDATE_DLQ);
    // }

    // // Notification queue - handles user notifications
    // @Bean
    // public Queue notificationQueue() {
    //     return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    // }

    // @Bean
    // public Binding notificationBinding() {
    //     return BindingBuilder.bind(notificationQueue())
    //             .to(stripeExchange())
    //             .with(NOTIFICATION_ROUTING_KEY);
    // }

    // /* EMAIL TOPIC */
    // @Bean
    // public TopicExchange emailTopicExchange() {
    //     return new TopicExchange(EMAIL_TOPIC_EXCHANGE_NAME);
    // }

    // // Dead letter exchange
    // @Bean
    // public DirectExchange emailDeadLetterExchange() {
    //     return new DirectExchange(EMAIL_DLX);
    // }

    // @Bean
    // public Queue emailTopicQueue() {
    //     return QueueBuilder.durable(EMAIL_TOPIC_QUEUE_NAME)
    //             .withArgument("x-dead-letter-exchange", EMAIL_DLX)
    //             .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
    //             .withArgument("x-message-ttl", 300000) // 5 minutes TTL
    //     .build();
    // }

    // @Bean
    // public Binding emailTopicBinding() {
    //     return BindingBuilder.bind(emailTopicQueue()).to(emailTopicExchange()).with(EMAIL_ROUTING_KEY);
    // }

    // @Bean
    // public Queue emailDLQ() {
    //     return QueueBuilder.durable(EMAIL_DLQ).build();
    // }

    // @Bean
    // public Binding emailDLQBinding() {
    //     return BindingBuilder.bind(emailDLQ()).to(emailDeadLetterExchange()).with(EMAIL_DLQ);
    // }

}
