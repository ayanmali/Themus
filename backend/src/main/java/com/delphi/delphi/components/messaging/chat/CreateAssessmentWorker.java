package com.delphi.delphi.components.messaging.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.chat.PublishAssessmentCreationJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;

import com.delphi.delphi.services.ChatService;
import com.delphi.delphi.utils.AssessmentCreationPrompts;
import com.delphi.delphi.utils.enums.JobStatus;

@Component
/**
 * Subscribes to the create assessment queue and processes the job.
 */
public class CreateAssessmentWorker {
    private final JobRepository jobRepository;
    private final ChatService chatService;
    private final Logger log = LoggerFactory.getLogger(CreateAssessmentWorker.class);

    public CreateAssessmentWorker(JobRepository jobRepository, ChatService chatService) {
        this.jobRepository = jobRepository;
        this.chatService = chatService;
    }

    @RabbitListener(queues = TopicConfig.CREATE_ASSESSMENT_QUEUE_NAME)
    public void processCreateAssessmentJob(PublishAssessmentCreationJobDto publishAssessmentCreationJobDto) {
        final UUID jobId = publishAssessmentCreationJobDto.getJobId();
        Job job = null;
        try {
            job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            job.setStatus(JobStatus.RUNNING);
            job = jobRepository.save(job);

            // Send SSE event that job is running
            chatService.sendSseEvent(jobId, "job_running",
                    Map.of("message", "Processing assessment creation", "jobId", jobId.toString(), "status",
                            JobStatus.RUNNING.toString()));

            // Agent loop
            log.info("Create assessment job processing - running agent loop...", jobId.toString());
            chatService.getChatCompletion(
                    jobId,
                    List.of(), // No existing messages for new assessment
                    AssessmentCreationPrompts.USER_PROMPT,
                    publishAssessmentCreationJobDto.getUserPromptVariables(),
                    publishAssessmentCreationJobDto.getModel(),
                    // for storing chat messages into the assessment's chat history
                    publishAssessmentCreationJobDto.getAssessmentId(),
                    // for making calls to the github api
                    publishAssessmentCreationJobDto.getEncryptedGithubToken(),
                    publishAssessmentCreationJobDto.getGithubUsername(),
                    publishAssessmentCreationJobDto.getGithubRepoName());

            log.info("Saving completed assessment creation job with ID: {}", jobId.toString());
            job.setStatus(JobStatus.COMPLETED);
            job.setResult("Assessment created successfully");
            jobRepository.save(job);

            // Send SSE event that assessment creation is complete
            chatService.sendSseEvent(jobId, "assessment_created",
                    Map.of("message", "Assessment created successfully", "jobId", jobId.toString(),
                            "assessmentId", publishAssessmentCreationJobDto.getAssessmentId(), "status",
                            JobStatus.COMPLETED.toString()));

            // Complete the SSE emitter
            chatService.completeSseEmitter(jobId);

        } catch (IllegalArgumentException e) {
            log.error("Validation error in assessment creation job {}: {}", jobId, e.getMessage());
            handleJobFailure(job, jobId, e, "Validation error: " + e.getMessage());

        } catch (RuntimeException e) {
            log.error("Runtime error in assessment creation job {}: {}", jobId, e.getMessage(), e);
            handleJobFailure(job, jobId, e, "Service error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error in assessment creation job {}: {}", jobId, e.getMessage(), e);
            handleJobFailure(job, jobId, e, "Unexpected error: " + e.getMessage());

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

    private void handleJobFailure(Job job, UUID jobId, Exception e, String errorMessage) {
        try {
            // Update job status in database
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setResult(errorMessage);
                jobRepository.save(job);
            }
            
            // Send error event via SSE
            chatService.sendSseEvent(jobId, "error", 
                Map.of("error", errorMessage,
                       "jobId", jobId.toString(),
                       "status", "failed"));
                       
        } catch (Exception ex) {
            log.error("Additional error while handling job failure for {}: {}", jobId, ex.getMessage());
        }
    }
}