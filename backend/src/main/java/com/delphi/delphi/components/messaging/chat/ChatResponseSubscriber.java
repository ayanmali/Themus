// package com.delphi.delphi.components.messaging.chat;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.amqp.rabbit.annotation.RabbitHandler;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.stereotype.Component;

// import com.delphi.delphi.configs.rabbitmq.TopicConfig;
// import com.delphi.delphi.dtos.messaging.chat.ChatCompletionResponseDto;

// @Component
// @RabbitListener(queues = TopicConfig.CHAT_RESPONSE_TOPIC_QUEUE_NAME)
// public class ChatResponseSubscriber {

//     private static final Logger log = LoggerFactory.getLogger(ChatResponseSubscriber.class);
    
//     // You could inject a WebSocket handler here to send responses to clients
//     // private final WebSocketHandler webSocketHandler;

//     @RabbitHandler
//     public void processChatCompletionResponse(ChatCompletionResponseDto response) {
//         log.info("Received chat completion response for request ID: {}", response.getRequestId());
        
//         if (response.isSuccess()) {
//             // Handle successful response
//             // webSocketHandler.sendToUser(response.getRequestId(), response.getChatResponse());
//             log.info("Chat completion successful for request: {}", response.getRequestId());
//             log.info("Chat completion response: {}", response.getChatResponse().toString());
//         } else {
//             // Handle error response
//             log.error("Chat completion failed for request {}: {}", response.getRequestId(), response.getError());
//         }
//     }
// }
