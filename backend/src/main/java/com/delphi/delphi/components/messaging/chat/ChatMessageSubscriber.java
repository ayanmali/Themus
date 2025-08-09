package com.delphi.delphi.components.messaging.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.chat.ChatCompletionRequestDto;
import com.delphi.delphi.services.ChatService;

@Component
@RabbitListener(queues = TopicConfig.CHAT_TOPIC_QUEUE_NAME)
public class ChatMessageSubscriber {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageSubscriber.class);
    
    private final ChatService chatService;
    // to publish to the chat response queue
    private final ChatMessagePublisher chatMessagePublisher;

    public ChatMessageSubscriber(ChatService chatService, ChatMessagePublisher chatMessagePublisher) {
        this.chatService = chatService;
        this.chatMessagePublisher = chatMessagePublisher;
    }

    @RabbitHandler
    public void processChatCompletionRequest(ChatCompletionRequestDto request) {
        log.info("Processing chat completion request with ID: {}", request.getRequestId());
        
        try {
            ChatResponse response;
            
            if (request.getUserMessage() != null) {
                // Simple user message
                response = chatService.getChatCompletion(
                    request.getUserMessage(),
                    request.getModel(),
                    request.getAssessmentId(),
                    request.getUserId()
                );
            } else if (request.getUserPromptTemplate() != null) {
                // Template-based message
                response = chatService.getChatCompletion(
                    request.getUserPromptTemplate(),
                    request.getUserPromptVariables(),
                    request.getModel(),
                    request.getAssessmentId(),
                    request.getUserId()
                );
            } else {
                throw new IllegalArgumentException("Either userMessage or userPromptTemplate must be provided");
            }
            
            // Publish the response back
            chatMessagePublisher.publishChatCompletionResponse(request.getRequestId(), response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error processing chat completion request {}: {}", request.getRequestId(), e.getMessage(), e);
            chatMessagePublisher.publishChatCompletionError(request.getRequestId(), e.getMessage());
        }
    }
}
