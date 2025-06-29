package com.delphi.delphi.components.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.entities.ChatMessage;

@Component
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    
    public ChatMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;    
    }

    public void publishMessage(ChatMessage message) {
        rabbitTemplate.convertAndSend(TopicConfig.CHAT_TOPIC_EXCHANGE_NAME, "topic.chat", message);
    }
}
