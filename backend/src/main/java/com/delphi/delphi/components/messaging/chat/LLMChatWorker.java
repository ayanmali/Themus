package com.delphi.delphi.components.messaging.chat;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.cache.ChatMessageCacheDto;
import com.delphi.delphi.dtos.messaging.chat.PublishChatJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.services.ChatService;
import com.delphi.delphi.services.AssessmentService;
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

    public LLMChatWorker(JobRepository jobRepository, ChatService chatService, AssessmentService assessmentService) {
        this.jobRepository = jobRepository;
        this.chatService = chatService;
        this.assessmentService = assessmentService;
    }

    @RabbitListener(queues = TopicConfig.LLM_CHAT_QUEUE_NAME)
    public void processLLMChatJob(PublishChatJobDto publishLLMChatJobDto) {
        Job job = jobRepository.findById(publishLLMChatJobDto.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        try {
            job.setStatus(JobStatus.RUNNING);
            jobRepository.save(job);
            
            // Send SSE event that job is running
            chatService.sendSseEvent(publishLLMChatJobDto.getJobId(), "job_running", 
                Map.of("message", "Processing chat completion", "jobId", publishLLMChatJobDto.getJobId().toString()));
            
            List<Message> existingMessages = assessmentService.getChatMessagesById(publishLLMChatJobDto.getAssessmentId());
                // Agent loop
            List<ChatMessageCacheDto> messages = chatService.getChatCompletion(
                publishLLMChatJobDto.getMessageText(), 
                publishLLMChatJobDto.getModel(), 
                publishLLMChatJobDto.getAssessmentId(), 
                publishLLMChatJobDto.getEncryptedGithubToken(), 
                publishLLMChatJobDto.getGithubUsername(), 
                publishLLMChatJobDto.getGithubRepoName()
            );

            log.info("Saving completed chat completion job with ID: {}", publishLLMChatJobDto.getJobId().toString());    
            job.setStatus(JobStatus.COMPLETED);
            job.setResult("Chat completion completed");
            jobRepository.save(job);
            
            // Send SSE event with the new messages
            chatService.sendSseEvent(publishLLMChatJobDto.getJobId(), "chat_completed", 
                Map.of("messages", messages, "jobId", publishLLMChatJobDto.getJobId().toString()));
            
            // Complete the SSE emitter
            chatService.completeSseEmitter(publishLLMChatJobDto.getJobId());

        } catch (IllegalArgumentException e) {
            job.setStatus(JobStatus.FAILED);
            job.setResult(e.getMessage());
            log.error("Error processing chat completion request {}: {}", publishLLMChatJobDto.getJobId().toString(), e.getMessage(), e);
            
            // Send SSE error event
            chatService.sendSseEvent(publishLLMChatJobDto.getJobId(), "error", 
                Map.of("error", e.getMessage(), "jobId", publishLLMChatJobDto.getJobId().toString()));
            chatService.completeSseEmitter(publishLLMChatJobDto.getJobId());
            
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setResult(e.getMessage());
            log.error("Error processing chat completion request {}: {}", publishLLMChatJobDto.getJobId().toString(), e.getMessage(), e);
            
            // Send SSE error event
            chatService.sendSseEvent(publishLLMChatJobDto.getJobId(), "error", 
                Map.of("error", e.getMessage(), "jobId", publishLLMChatJobDto.getJobId().toString()));
            chatService.completeSseEmitter(publishLLMChatJobDto.getJobId());
        } 
    }
}
