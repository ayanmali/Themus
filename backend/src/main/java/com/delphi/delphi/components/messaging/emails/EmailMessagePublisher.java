// package com.delphi.delphi.components.messaging.emails;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.amqp.AmqpException;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.stereotype.Component;

// import com.delphi.delphi.configs.rabbitmq.TopicConfig;
// import com.delphi.delphi.dtos.messaging.emails.EmailRequestDto;

// @Component
// public class EmailMessagePublisher {

//     private final RabbitTemplate rabbitTemplate;
//     private static final Logger log = LoggerFactory.getLogger(EmailMessagePublisher.class);

//     public EmailMessagePublisher(RabbitTemplate rabbitTemplate) {
//         this.rabbitTemplate = rabbitTemplate;
//     }

//     public void publishEmailMessage(EmailRequestDto emailRequest) {
//         try {
//             log.info("Publishing email message to candidate: {} -- email: {}", emailRequest.getTo().getFullName(), emailRequest.getTo().getEmail());
//             rabbitTemplate.convertAndSend(
//                 TopicConfig.EMAIL_TOPIC_EXCHANGE_NAME,
//                 TopicConfig.EMAIL_ROUTING_KEY,
//                 emailRequest
//             );
//             log.info("Successfully published email message to candidate: {} -- email: {}", emailRequest.getTo().getFullName(), emailRequest.getTo().getEmail());
//         } catch (AmqpException e) {
//             log.error("Failed to publish email message for candidate: {} -- email: {}", emailRequest.getTo().getFullName(), emailRequest.getTo().getEmail(), e);
//             throw new RuntimeException("Failed to publish email message", e);
//         }
//     }
    
    
// }
