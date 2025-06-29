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

    /* CHAT TOPIC */
    @Bean
    public TopicExchange chatTopicExchange() {
        return new TopicExchange(CHAT_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue chatTopicQueue() {
        return new Queue("chatTopicQueue");
    }

    /*
     * Binding for chat topic queue to the chat topic exchange
     */
    @Bean
    public Binding bindingExchangeMessage(Queue chatTopicQueue, TopicExchange chatTopicExchange) {
        return BindingBuilder.bind(chatTopicQueue).to(chatTopicExchange).with("topic.chat");
    }

    /* PAYMENTS TOPIC */

    /* EMAIL TOPIC */


    // @Bean
    // public Binding bindingExchangeMessages(Queue chatTopicQueue, TopicExchange topicExchange) {
    //     return BindingBuilder.bind(chatTopicQueue).to(topicExchange).with("topic.#");
    // }

}
