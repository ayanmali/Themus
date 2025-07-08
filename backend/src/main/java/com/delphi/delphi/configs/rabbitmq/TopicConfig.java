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

    /* TODO: EMAIL TOPIC */


    // @Bean
    // public Binding bindingExchangeMessages(Queue chatTopicQueue, TopicExchange topicExchange) {
    //     return BindingBuilder.bind(chatTopicQueue).to(topicExchange).with("topic.#");
    // }

}
