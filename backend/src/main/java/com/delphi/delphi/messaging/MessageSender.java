package com.delphi.delphi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.RabbitMQConfig;

// import java.util.concurrent.TimeUnit;

// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// @Component
// public class MessageSender implements CommandLineRunner {

//   private final RabbitTemplate rabbitTemplate;
//   private final Receiver receiver;

//   public MessageSender(Receiver receiver, RabbitTemplate rabbitTemplate) {
//     this.receiver = receiver;
//     this.rabbitTemplate = rabbitTemplate;
//   }

//   @Override
//   public void run(String... args) throws Exception {
//     System.out.println("Sending message...");
//     rabbitTemplate.convertAndSend(MessagingApplication.topicExchangeName, "foo.bar.baz", "Hello from RabbitMQ!");
//     receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
//   }

// }

@Component
public class MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final RabbitTemplate rabbitTemplate;

    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendChatMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        log.info("Sent message: {}", message);
    }
}

