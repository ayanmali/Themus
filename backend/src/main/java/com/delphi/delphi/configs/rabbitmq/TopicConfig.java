package com.delphi.delphi.configs.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
    public static final String TOPIC_EXCHANGE_NAME = "topicExchange";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue chatTopicQueue() {
        return new Queue("topic.chat");
    }

    /*
     * Binding for chat topic queue to the topic exchange
     */
    @Bean
    public Binding bindingExchangeMessage(Queue chatTopicQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(chatTopicQueue).to(topicExchange).with("topic.chat");
    }

    // @Bean
    // public Binding bindingExchangeMessages(Queue chatTopicQueue, TopicExchange topicExchange) {
    //     return BindingBuilder.bind(chatTopicQueue).to(topicExchange).with("topic.#");
    // }

}
