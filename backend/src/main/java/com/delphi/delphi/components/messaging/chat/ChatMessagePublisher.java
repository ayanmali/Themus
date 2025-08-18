package com.delphi.delphi.components.messaging.chat;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.chat.ChatCompletionRequestDto;
import com.delphi.delphi.dtos.messaging.chat.ChatCompletionResponseDto;

@Component
// Publishes chat completion requests to the chat message queue
// Publishes chat completion responses to the chat response queue
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Logger log = LoggerFactory.getLogger(ChatMessagePublisher.class);

    public ChatMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public String publishChatCompletionRequest(String userMessage, String model, Long assessmentId, Long userId) {
        String requestId = UUID.randomUUID().toString();
        
        ChatCompletionRequestDto request = new ChatCompletionRequestDto(
            userMessage, model, assessmentId, userId, requestId
        );

        log.info("Publishing chat completion request with ID: {} for model: {}", requestId, model);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.CHAT_TOPIC_EXCHANGE_NAME, 
            "topic.chat", 
            request
        );
        
        return requestId;
    }

    public String publishChatCompletionRequest(String userPromptTemplate, 
                                             Map<String, Object> userPromptVariables,
                                             String model, 
                                             Long assessmentId, 
                                             Long userId) {
        String requestId = UUID.randomUUID().toString();
        
        ChatCompletionRequestDto request = new ChatCompletionRequestDto(
            userPromptTemplate, userPromptVariables, model, assessmentId, userId, requestId
        );

        log.info("Publishing template-based chat completion request with ID: {} for model: {}", requestId, model);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.CHAT_TOPIC_EXCHANGE_NAME, 
            "topic.chat", 
            request
        );
        
        return requestId;
    }

    public void publishChatCompletionResponse(String requestId, ChatResponse chatResponse) {
        ChatCompletionResponseDto response = new ChatCompletionResponseDto(requestId, chatResponse);
        
        log.info("Publishing chat completion response for request ID: {}", requestId);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.CHAT_RESPONSE_TOPIC_EXCHANGE_NAME,
            "topic.chat.response",
            response
        );
    }

    public void publishChatCompletionError(String requestId, String error) {
        ChatCompletionResponseDto response = new ChatCompletionResponseDto(requestId, error);
        
        log.error("Publishing chat completion error for request ID: {}: {}", requestId, error);
        
        rabbitTemplate.convertAndSend(
            TopicConfig.CHAT_RESPONSE_TOPIC_EXCHANGE_NAME,
            "topic.chat.response",
            response
        );
    }
}
