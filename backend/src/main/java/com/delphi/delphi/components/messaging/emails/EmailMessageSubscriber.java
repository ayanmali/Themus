// package com.delphi.delphi.components.messaging.emails;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.stereotype.Component;

// import com.delphi.delphi.components.ResendService;
// import com.delphi.delphi.configs.rabbitmq.TopicConfig;
// import com.delphi.delphi.dtos.messaging.emails.EmailRequestDto;

// @Component
// @RabbitListener(queues = TopicConfig.EMAIL_TOPIC_QUEUE_NAME)
// public class EmailMessageSubscriber {
    
//     private final RabbitTemplate rabbitTemplate;
//     private final ResendService resendService;
//     private static final Logger log = LoggerFactory.getLogger(EmailMessageSubscriber.class);
    
//     public EmailMessageSubscriber(RabbitTemplate rabbitTemplate, ResendService resendService) {
//         this.rabbitTemplate = rabbitTemplate;
//         this.resendService = resendService;
//     }

//     public void handleEmailMessage(EmailRequestDto emailRequest) {
//         try {
//             log.info("Received email message for user: {}", emailRequest.getUserId());
//         } catch (Exception e) {
//             log.error("Failed to handle email message for user: {}", emailRequest.getUserId(), e);
//             emailRequest.incrementRetryCount();
//             if (emailRequest.getRetryCount() < 3) {
//                 throw new RuntimeException("Retryable error processing email", e);
//             } else {
//                 log.error("Max retries reached for email message: {}", emailRequest.getRequestId());
//                 // Message will be sent to DLQ
//             }
//         }
//     }
    
// }
