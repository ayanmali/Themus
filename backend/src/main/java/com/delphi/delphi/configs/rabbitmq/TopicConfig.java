package com.delphi.delphi.configs.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
    public static final String CHAT_TOPIC_EXCHANGE_NAME = "chatTopicExchange";
    public static final String CHAT_TOPIC_QUEUE_NAME = "chatTopicQueue";

    public static final String CHAT_RESPONSE_TOPIC_EXCHANGE_NAME = "chatResponseTopicExchange";
    public static final String CHAT_RESPONSE_TOPIC_QUEUE_NAME = "chatResponseTopicQueue";

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
    public static final String PAYMENT_TOPIC_EXCHANGE_NAME = "paymentTopicExchange";
    public static final String PAYMENT_TOPIC_QUEUE_NAME = "paymentTopicQueue";
    
    public static final String PAYMENT_RESPONSE_TOPIC_EXCHANGE_NAME = "paymentResponseTopicExchange";
    public static final String PAYMENT_RESPONSE_TOPIC_QUEUE_NAME = "paymentResponseTopicQueue";
    
    public static final String PAYMENT_WEBHOOK_TOPIC_EXCHANGE_NAME = "paymentWebhookTopicExchange";
    public static final String PAYMENT_WEBHOOK_TOPIC_QUEUE_NAME = "paymentWebhookTopicQueue";

    @Bean
    public TopicExchange paymentTopicExchange() {
        return new TopicExchange(PAYMENT_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue paymentTopicQueue() {
        return new Queue(PAYMENT_TOPIC_QUEUE_NAME);
    }

    @Bean
    public Binding bindingPaymentExchange(Queue paymentTopicQueue, TopicExchange paymentTopicExchange) {
        return BindingBuilder.bind(paymentTopicQueue).to(paymentTopicExchange).with("topic.payment.#");
    }

    /* PAYMENT RESPONSE TOPIC */
    @Bean
    public TopicExchange paymentResponseTopicExchange() {
        return new TopicExchange(PAYMENT_RESPONSE_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue paymentResponseTopicQueue() {
        return new Queue(PAYMENT_RESPONSE_TOPIC_QUEUE_NAME);
    }

    @Bean
    public Binding bindingPaymentResponseExchange(Queue paymentResponseTopicQueue, TopicExchange paymentResponseTopicExchange) {
        return BindingBuilder.bind(paymentResponseTopicQueue).to(paymentResponseTopicExchange).with("topic.payment.response.#");
    }

    /* PAYMENT WEBHOOK TOPIC */
    @Bean
    public TopicExchange paymentWebhookTopicExchange() {
        return new TopicExchange(PAYMENT_WEBHOOK_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue paymentWebhookTopicQueue() {
        return new Queue(PAYMENT_WEBHOOK_TOPIC_QUEUE_NAME);
    }

    @Bean
    public Binding bindingPaymentWebhookExchange(Queue paymentWebhookTopicQueue, TopicExchange paymentWebhookTopicExchange) {
        return BindingBuilder.bind(paymentWebhookTopicQueue).to(paymentWebhookTopicExchange).with("topic.payment.webhook.#");
    }

    /* TODO: EMAIL TOPIC */


    // @Bean
    // public Binding bindingExchangeMessages(Queue chatTopicQueue, TopicExchange topicExchange) {
    //     return BindingBuilder.bind(chatTopicQueue).to(topicExchange).with("topic.#");
    // }

}
