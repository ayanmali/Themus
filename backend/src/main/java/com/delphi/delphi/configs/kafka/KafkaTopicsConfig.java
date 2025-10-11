package com.delphi.delphi.configs.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    public static final String LLM_CREATE_ASSESSMENT = "llm.create_assessment";
    public static final String LLM_CHAT = "llm.chat";
    public static final String LLM_RESPONSE_CREATE_ASSESSMENT = "llm.response.create_assessment";
    public static final String LLM_RESPONSE_CHAT = "llm.response.chat";
    // public static final String LLM_ANALYZE_BASE_REPO = "llm.analyze_base_repo";
    // public static final String LLM_RESPONSE_ANALYZE_BASE_REPO = "llm.response.analyze_base_repo";
    public static final String EMAIL = "email";

    private final String CANDIDATE_INVITATION_TOPIC;

    public KafkaTopicsConfig(
        @Value("${candidate.invitation.topic.exchange.name}") String candidateInvitationTopic
    ) {
        this.CANDIDATE_INVITATION_TOPIC = candidateInvitationTopic;
    }

    @Bean
    public NewTopic llmCreateAssessment() {
        return TopicBuilder.name(LLM_CREATE_ASSESSMENT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic llmChat() {
        return TopicBuilder.name(LLM_CHAT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic llmResponseCreateAssessment() {
        return TopicBuilder.name(LLM_RESPONSE_CREATE_ASSESSMENT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic llmResponseChat() {
        return TopicBuilder.name(LLM_RESPONSE_CHAT).partitions(3).replicas(1).build();
    }

    // @Bean
    // public NewTopic llmAnalyzeBaseRepo() {
    //     return TopicBuilder.name(LLM_ANALYZE_BASE_REPO).partitions(3).replicas(1).build();
    // }

    // @Bean
    // public NewTopic llmResponseAnalyzeBaseRepo() {
    //     return TopicBuilder.name(LLM_RESPONSE_ANALYZE_BASE_REPO).partitions(3).replicas(1).build();
    // }

    @Bean
    public NewTopic email() {
        return TopicBuilder.name(EMAIL).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic candidateInvitation() {
        return TopicBuilder.name(CANDIDATE_INVITATION_TOPIC).partitions(3).replicas(1).build();
    }
}