package com.delphi.delphi.components.messaging.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.tools.GithubTools;
import com.delphi.delphi.configs.kafka.KafkaTopicsConfig;
import com.delphi.delphi.dtos.cache.ChatMessageCacheDto;
import com.delphi.delphi.dtos.messaging.chat.PublishChatJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.ChatService;
import com.delphi.delphi.utils.enums.JobStatus;

@Component
/**
 * Subscribes to the create assessment queue and processes the job.
 */
public class LLMChatWorker {
    private final JobRepository jobRepository;
    private final ChatService chatService;
    private final AssessmentService assessmentService;
    private final Logger log = LoggerFactory.getLogger(LLMChatWorker.class); 
    private final GithubTools githubTools;

    public LLMChatWorker(JobRepository jobRepository, ChatService chatService, AssessmentService assessmentService, GithubTools githubTools) {
        this.jobRepository = jobRepository;
        this.chatService = chatService;
        this.assessmentService = assessmentService;
        this.githubTools = githubTools;
    }

    @KafkaListener(topics = KafkaTopicsConfig.LLM_CHAT, containerFactory = "kafkaListenerContainerFactory")
    public void processLLMChatJob(PublishChatJobDto publishChatJobDto) {
        final UUID jobId = publishChatJobDto.getJobId();
        Job job = null;
        try {
            job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            job.setStatus(JobStatus.RUNNING);
            job = jobRepository.save(job);

            // Send SSE event that job is running
            chatService.sendSseEvent(jobId, "job_running",
                    Map.of("message", "Processing chat completion", "jobId", jobId.toString(), "status",
                            JobStatus.RUNNING.toString()));

            // Agent loop
            log.info("Chat job processing - running agent loop...", jobId.toString());
            // Get the assessment's chat messages as Spring AI messages
            List<Message> existingMessages = assessmentService.getChatMessagesById(publishChatJobDto.getAssessmentId()).stream().map(ChatMessageCacheDto::toMessage).collect(Collectors.toList());
            log.info("Assessment messages obtained");
            chatService.getChatCompletion(
                    jobId,
                    existingMessages, // No existing messages for new assessment
                    publishChatJobDto.getMessageText(),
                    publishChatJobDto.getModel(),
                    // for storing chat messages into the assessment's chat history
                    publishChatJobDto.getAssessmentId(),
                    // for making calls to the github api
                    publishChatJobDto.getEncryptedGithubToken(),
                    publishChatJobDto.getGithubUsername(),
                    publishChatJobDto.getGithubRepoName(),
                    githubTools,
                    MessageUtils.ASSESSMENT_CREATION_PRESET);

            log.info("Saving completed chat completion job with ID: {}", jobId.toString());
            job.setStatus(JobStatus.COMPLETED);
            job.setResult("Chat completion completed");
            jobRepository.save(job);

            // Send SSE event that assessment creation is complete
            chatService.sendSseEvent(jobId, "chat_completion",
                    Map.of("message", "Chat completion completed", "jobId", jobId.toString(),
                            "assessmentId", publishChatJobDto.getAssessmentId(), "status",
                            JobStatus.COMPLETED.toString()));

            // Complete the SSE emitter
            chatService.completeSseEmitter(jobId);

        } catch (IllegalArgumentException e) {
            log.error("Validation error in chat completion job {}: {}", jobId, e.getMessage());
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Validation error: " + e.getMessage());

        } catch (RuntimeException e) {
            log.error("Runtime error in chat completion job {}: {}", jobId, e.getMessage(), e);
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Service error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error in chat completion job {}: {}", jobId, e.getMessage(), e);
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Unexpected error: " + e.getMessage());

        } finally {
            // Always complete the SSE emitter, regardless of success or failure
            try {
                chatService.completeSseEmitter(jobId);
                log.info("SSE emitter completed for job: {}", jobId);
            } catch (Exception e) {
                log.error("Error completing SSE emitter for job {}: {}", jobId, e.getMessage());
                // Try to force remove the emitter
                chatService.removeSseEmitter(jobId);
            }
        }
    }


}
