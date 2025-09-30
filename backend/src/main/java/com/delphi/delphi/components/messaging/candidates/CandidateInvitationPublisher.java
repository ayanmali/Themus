package com.delphi.delphi.components.messaging.candidates;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.dtos.messaging.candidates.CandidateInvitationMessage;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;

@Component
public class CandidateInvitationPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String candidateInvitationTopic;
    
    private static final Logger log = LoggerFactory.getLogger(CandidateInvitationPublisher.class);

    public CandidateInvitationPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                       @Value("${candidate.invitation.topic.exchange.name}") String candidateInvitationTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.candidateInvitationTopic = candidateInvitationTopic;
    }

    public String publishCandidateInvitation(Assessment assessment, Candidate candidate, Long userId, String userEmail) {
        String invitationId = UUID.randomUUID().toString();
        // to avoid problems w/ JSON serialization and overnesting
        CandidateCacheDto candidateCacheDto = new CandidateCacheDto(candidate);
        
        CandidateInvitationMessage message = new CandidateInvitationMessage(
            assessment.getId(),
            assessment.getName(),
            assessment.getDescription(),
            assessment.getStartDate(),
            assessment.getEndDate(),
            assessment.getDuration(),
            candidateCacheDto,
            userId,
            userEmail,
            LocalDateTime.now(),
            invitationId
        );

        try {
            log.info("Publishing candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName());
            
            // Use assessment ID as key to ensure messages for same assessment go to same partition
            String key = assessment.getId().toString();
            
            SendResult<String, Object> result = kafkaTemplate.send(
                candidateInvitationTopic,
                key,
                message
            ).get(); // Block until send completes
            
            log.info("Successfully published candidate invitation message for candidate: {} -- email: {} -- assessment: {} -- partition: {} -- offset: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName(), 
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            
            return invitationId;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName(), e);
            throw new RuntimeException("Failed to publish candidate invitation message", e);
        } catch (ExecutionException e) {
            log.error("Failed to publish candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName(), e);
            throw new RuntimeException("Failed to publish candidate invitation message", e);
        } catch (Exception e) {
            log.error("Failed to publish candidate invitation message for candidate: {} -- email: {} -- assessment: {}", 
                candidate.getFullName(), candidate.getEmail(), assessment.getName(), e);
            throw new RuntimeException("Failed to publish candidate invitation message", e);
        }
    }
}