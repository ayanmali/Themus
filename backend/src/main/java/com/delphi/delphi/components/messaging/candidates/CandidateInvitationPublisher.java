package com.delphi.delphi.components.messaging.candidates;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.delphi.delphi.dtos.messaging.candidates.CandidateInvitationMessage;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;

@Component
public class CandidateInvitationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String candidateInvitationTopicExchangeName;
    private final String candidateInvitationRoutingKey;
    
    private static final Logger log = LoggerFactory.getLogger(CandidateInvitationPublisher.class);

    public CandidateInvitationPublisher(RabbitTemplate rabbitTemplate,
                       @Value("${candidate.invitation.topic.exchange.name}") String candidateInvitationTopicExchangeName,
                       @Value("${candidate.invitation.routing.key}") String candidateInvitationRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.candidateInvitationTopicExchangeName = candidateInvitationTopicExchangeName;
        this.candidateInvitationRoutingKey = candidateInvitationRoutingKey;
    }

    public String publishCandidateInvitation(Assessment assessment, Candidate candidate, Long userId, String userEmail) {
        String invitationId = UUID.randomUUID().toString();
        
        CandidateInvitationMessage message = new CandidateInvitationMessage(
            assessment.getId(),
            assessment.getName(),
            assessment.getDescription(),
            assessment.getStartDate(),
            assessment.getEndDate(),
            assessment.getDuration(),
            candidate,
            userId,
            userEmail,
            LocalDateTime.now(),
            invitationId
        );

        try {
            log.info("Publishing candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName());
            
            rabbitTemplate.convertAndSend(
                candidateInvitationTopicExchangeName,
                candidateInvitationRoutingKey,
                message
            );
            
            log.info("Successfully published candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName());
            
            return invitationId;
        } catch (AmqpException e) {
            log.error("Failed to publish candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName(), e);
            throw new RuntimeException("Failed to publish candidate invitation message", e);
        }
    }
} 