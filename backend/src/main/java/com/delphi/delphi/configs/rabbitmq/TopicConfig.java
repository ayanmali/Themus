package com.delphi.delphi.configs.rabbitmq;

import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
    public static final String LLM_TOPIC_EXCHANGE_NAME = "llm.topic";

    public static final String CREATE_ASSESSMENT_QUEUE_NAME = "llm.create_assessment.queue";
    public static final String CREATE_ASSESSMENT_ROUTING_KEY = "llm.create_assessment";

    public static final String LLM_CHAT_QUEUE_NAME = "llm.chat.queue";
    public static final String LLM_CHAT_ROUTING_KEY = "llm.chat";

    // public static final String LLM_TOPIC_DLX = "llm.topic.dlx";
    // public static final String LLM_TOPIC_DLQ = "llm.topic.dlq"; 

    public static final String LLM_RESPONSE_TOPIC_EXCHANGE_NAME = "llm.response.topic";

    public static final String LLM_RESPONSE_CREATE_ASSESSMENT_QUEUE_NAME = "llm.response.create_assessment.queue";
    public static final String LLM_RESPONSE_CREATE_ASSESSMENT_ROUTING_KEY = "llm.response.create_assessment";

    public static final String LLM_RESPONSE_CHAT_QUEUE_NAME = "llm.response.chat.queue";
    public static final String LLM_RESPONSE_CHAT_ROUTING_KEY  = "llm.response.chat";

    // public static final String LLM_RESPONSE_TOPIC_DLX = "llm.response.topic.dlx";
    // public static final String LLM_RESPONSE_TOPIC_DLQ = "llm.response.topic.dlq";

    public static final String EMAIL_TOPIC_EXCHANGE_NAME = "email.topic";
    public static final String EMAIL_QUEUE_NAME = "email.queue";
    public static final String EMAIL_ROUTING_KEY = "email";

    public final String CANDIDATE_INVITATION_TOPIC_EXCHANGE_NAME;
    public final String CANDIDATE_INVITATION_TOPIC_QUEUE_NAME;
    public final String CANDIDATE_INVITATION_ROUTING_KEY;
    
    public TopicConfig(@Value("${candidate.invitation.topic.exchange.name}") String candidateInvitationTopicExchangeName,
                       @Value("${candidate.invitation.topic.queue.name}") String candidateInvitationTopicQueueName,
                       @Value("${candidate.invitation.routing.key}") String candidateInvitationRoutingKey) {
        this.CANDIDATE_INVITATION_TOPIC_EXCHANGE_NAME = candidateInvitationTopicExchangeName;
        this.CANDIDATE_INVITATION_TOPIC_QUEUE_NAME = candidateInvitationTopicQueueName;
        this.CANDIDATE_INVITATION_ROUTING_KEY = candidateInvitationRoutingKey;
    }

    // // Exchange names
    // public static final String STRIPE_EXCHANGE = "stripe.exchange";
    // public static final String PAYMENT_DLX = "payment.dlx";
    
    // // Queue names
    // public static final String STRIPE_WEBHOOK_QUEUE = "stripe.webhook.queue";
    // public static final String PAYMENT_PROCESSING_QUEUE = "payment.processing.queue";
    // public static final String SUBSCRIPTION_UPDATE_QUEUE = "subscription.update.queue";
    // public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    // // Dead letter queues
    // public static final String STRIPE_WEBHOOK_DLQ = "stripe.webhook.dlq";
    // public static final String PAYMENT_PROCESSING_DLQ = "payment.processing.dlq";
    // public static final String SUBSCRIPTION_UPDATE_DLQ = "subscription.update.dlq";
    
    // // Routing keys
    // public static final String STRIPE_WEBHOOK_ROUTING_KEY = "stripe.webhook";
    // public static final String PAYMENT_PROCESSING_ROUTING_KEY = "payment.processing";
    // public static final String SUBSCRIPTION_UPDATE_ROUTING_KEY = "subscription.update";
    // public static final String NOTIFICATION_ROUTING_KEY = "notification";

    // public static final String EMAIL_TOPIC_EXCHANGE_NAME = "email.exchange";
    // public static final String EMAIL_DLX = "email.dlx";
    // public static final String EMAIL_TOPIC_QUEUE_NAME = "email.queue";
    // public static final String EMAIL_ROUTING_KEY = "resend.email";
    // public static final String EMAIL_DLQ = "email.dlq";

    @Bean
    public SimpleMessageConverter converter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("com.delphi.delphi.*", "java.util.*"));
        return converter;
    }

    /* CHAT TOPIC */
    @Bean
    public TopicExchange llmTopicExchange() {
        return new TopicExchange(LLM_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue assessmentCreationQueue() {
        return QueueBuilder.durable(CREATE_ASSESSMENT_QUEUE_NAME)
        // .deadLetterExchange(LLM_TOPIC_DLX)
        // .deadLetterRoutingKey(LLM_TOPIC_DLQ)
        .ttl(600000) // 10 minutes
        .build();
    }

    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(LLM_CHAT_QUEUE_NAME)
        .ttl(600000) // 10 minutes
        .build();
    }

    // @Bean
    // public Queue assessmentCreationDLQ() {
    //     return QueueBuilder.durable(LLM_TOPIC_DLQ)
    //     .deadLetterExchange(LLM_TOPIC_DLX)
    //     .deadLetterRoutingKey(LLM_TOPIC_QUEUE_NAME)
    //     .build();
    // }

    /*
     * Binding for assessment creation queue to the llm topic exchange
     */
    @Bean
    public Binding bindingLlmTopicExchange(Queue assessmentCreationQueue, TopicExchange llmTopicExchange) {
        return BindingBuilder.bind(assessmentCreationQueue).to(llmTopicExchange).with(CREATE_ASSESSMENT_ROUTING_KEY);
    }

    @Bean
    public Binding bindingLlmChatExchange(Queue chatQueue, TopicExchange llmTopicExchange) {
        return BindingBuilder.bind(chatQueue).to(llmTopicExchange).with(LLM_CHAT_ROUTING_KEY);
    }

    /* CHAT RESPONSE TOPIC */
    @Bean
    public TopicExchange llmResponseTopicExchange() {
        return new TopicExchange(LLM_RESPONSE_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue llmResponseCreateAssessmentQueue() {
        return QueueBuilder.durable(LLM_RESPONSE_CREATE_ASSESSMENT_QUEUE_NAME)
        .ttl(600000) // 10 minutes
        .build();
    }

    @Bean
    public Queue llmResponseChatQueue() {
        return QueueBuilder.durable(LLM_RESPONSE_CHAT_QUEUE_NAME)
        .ttl(600000) // 10 minutes
        .build();
    }

    @Bean
    public Binding bindingLlmResponseCreateAssessmentExchange(Queue llmResponseCreateAssessmentQueue, TopicExchange llmResponseTopicExchange) {
        return BindingBuilder.bind(llmResponseCreateAssessmentQueue).to(llmResponseTopicExchange).with(LLM_RESPONSE_CREATE_ASSESSMENT_ROUTING_KEY);
    }

    @Bean
    public Binding bindingLlmResponseChatExchange(Queue llmResponseChatQueue, TopicExchange llmResponseTopicExchange) {
        return BindingBuilder.bind(llmResponseChatQueue).to(llmResponseTopicExchange).with(LLM_RESPONSE_CHAT_ROUTING_KEY);
    }


    @Bean
    public TopicExchange emailTopicExchange() {
        return new TopicExchange(EMAIL_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder
        .durable(EMAIL_QUEUE_NAME)
        .ttl(600000) // 10 minutes
        .build();
    }

    @Bean
    public Binding bindingEmailExchange(Queue emailQueue, TopicExchange emailTopicExchange) {
        return BindingBuilder.bind(emailQueue).to(emailTopicExchange).with(EMAIL_ROUTING_KEY);
    }

    /* CANDIDATE INVITATION TOPIC */
    @Bean
    public TopicExchange candidateInvitationTopicExchange() {
        return new TopicExchange(CANDIDATE_INVITATION_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Queue candidateInvitationTopicQueue() {
        return QueueBuilder.durable(CANDIDATE_INVITATION_TOPIC_QUEUE_NAME)
        .build();
    }

    @Bean
    public Binding bindingCandidateInvitationExchange(Queue candidateInvitationTopicQueue, TopicExchange candidateInvitationTopicExchange) {
        return BindingBuilder.bind(candidateInvitationTopicQueue).to(candidateInvitationTopicExchange).with(CANDIDATE_INVITATION_ROUTING_KEY);
    }

    /* PAYMENTS TOPIC */
    // Main exchange for Stripe events
    // @Bean
    // public TopicExchange stripeExchange() {
    //     return new TopicExchange(STRIPE_EXCHANGE);
    // }

    // // Dead letter exchange
    // @Bean
    // public DirectExchange stripeDeadLetterExchange() {
    //     return new DirectExchange(PAYMENT_DLX);
    // }

    // // Stripe webhook queue - receives all webhook events
    // @Bean
    // public Queue stripeWebhookQueue() {
    //     return QueueBuilder.durable(STRIPE_WEBHOOK_QUEUE)
    //             .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
    //             .withArgument("x-dead-letter-routing-key", STRIPE_WEBHOOK_DLQ)
    //             .withArgument("x-message-ttl", 300000) // 5 minutes TTL
    //             .build();
    // }

    // @Bean
    // public Queue stripeWebhookDLQ() {
    //     return QueueBuilder.durable(STRIPE_WEBHOOK_DLQ).build();
    // }

    // @Bean
    // public Binding stripeWebhookBinding() {
    //     return BindingBuilder.bind(stripeWebhookQueue())
    //             .to(stripeExchange())
    //             .with(STRIPE_WEBHOOK_ROUTING_KEY);
    // }

    // @Bean
    // public Binding stripeWebhookDLQBinding() {
    //     return BindingBuilder.bind(stripeWebhookDLQ())
    //             .to(stripeDeadLetterExchange())
    //             .with(STRIPE_WEBHOOK_DLQ);
    // }

    // // Payment processing queue - handles payment-related events
    // @Bean
    // public Queue paymentProcessingQueue() {
    //     return QueueBuilder.durable(PAYMENT_PROCESSING_QUEUE)
    //             .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
    //             .withArgument("x-dead-letter-routing-key", PAYMENT_PROCESSING_DLQ)
    //             .withArgument("x-message-ttl", 600000) // 10 minutes TTL
    //             .build();
    // }

    // @Bean
    // public Queue paymentProcessingDLQ() {
    //     return QueueBuilder.durable(PAYMENT_PROCESSING_DLQ).build();
    // }

    // @Bean
    // public Binding paymentProcessingBinding() {
    //     return BindingBuilder.bind(paymentProcessingQueue())
    //             .to(stripeExchange())
    //             .with(PAYMENT_PROCESSING_ROUTING_KEY);
    // }

    // @Bean
    // public Binding paymentProcessingDLQBinding() {
    //     return BindingBuilder.bind(paymentProcessingDLQ())
    //             .to(stripeDeadLetterExchange())
    //             .with(PAYMENT_PROCESSING_DLQ);
    // }

    // // Subscription update queue - handles subscription changes
    // @Bean
    // public Queue subscriptionUpdateQueue() {
    //     return QueueBuilder.durable(SUBSCRIPTION_UPDATE_QUEUE)
    //             .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
    //             .withArgument("x-dead-letter-routing-key", SUBSCRIPTION_UPDATE_DLQ)
    //             .withArgument("x-message-ttl", 300000) // 5 minutes TTL
    //             .build();
    // }

    // @Bean
    // public Queue subscriptionUpdateDLQ() {
    //     return QueueBuilder.durable(SUBSCRIPTION_UPDATE_DLQ).build();
    // }

    // @Bean
    // public Binding subscriptionUpdateBinding() {
    //     return BindingBuilder.bind(subscriptionUpdateQueue())
    //             .to(stripeExchange())
    //             .with(SUBSCRIPTION_UPDATE_ROUTING_KEY);
    // }

    // @Bean
    // public Binding subscriptionUpdateDLQBinding() {
    //     return BindingBuilder.bind(subscriptionUpdateDLQ())
    //             .to(stripeDeadLetterExchange())
    //             .with(SUBSCRIPTION_UPDATE_DLQ);
    // }

    // // Notification queue - handles user notifications
    // @Bean
    // public Queue notificationQueue() {
    //     return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    // }

    // @Bean
    // public Binding notificationBinding() {
    //     return BindingBuilder.bind(notificationQueue())
    //             .to(stripeExchange())
    //             .with(NOTIFICATION_ROUTING_KEY);
    // }

    // /* EMAIL TOPIC */
    // @Bean
    // public TopicExchange emailTopicExchange() {
    //     return new TopicExchange(EMAIL_TOPIC_EXCHANGE_NAME);
    // }

    // // Dead letter exchange
    // @Bean
    // public DirectExchange emailDeadLetterExchange() {
    //     return new DirectExchange(EMAIL_DLX);
    // }

    // @Bean
    // public Queue emailTopicQueue() {
    //     return QueueBuilder.durable(EMAIL_TOPIC_QUEUE_NAME)
    //             .withArgument("x-dead-letter-exchange", EMAIL_DLX)
    //             .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
    //             .withArgument("x-message-ttl", 300000) // 5 minutes TTL
    //     .build();
    // }

    // @Bean
    // public Binding emailTopicBinding() {
    //     return BindingBuilder.bind(emailTopicQueue()).to(emailTopicExchange()).with(EMAIL_ROUTING_KEY);
    // }

    // @Bean
    // public Queue emailDLQ() {
    //     return QueueBuilder.durable(EMAIL_DLQ).build();
    // }

    // @Bean
    // public Binding emailDLQBinding() {
    //     return BindingBuilder.bind(emailDLQ()).to(emailDeadLetterExchange()).with(EMAIL_DLQ);
    // }

}
