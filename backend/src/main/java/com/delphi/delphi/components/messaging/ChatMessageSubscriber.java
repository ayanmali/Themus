package com.delphi.delphi.components.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.entities.ChatMessage;

/*
 * Listens for chat messages from the topic exchange
 */
@Component
@RabbitListener(queues = TopicConfig.CHAT_TOPIC_QUEUE_NAME)
public class ChatMessageSubscriber {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageSubscriber.class);

    @RabbitHandler
    public void processMessage(ChatMessage message) {
        log.info("Received message in chat topic queue: {}", message);
        // TODO: Process the message
    }
}
