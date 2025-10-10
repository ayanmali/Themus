package com.delphi.delphi.components.messaging.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.kafka.KafkaTopicsConfig;
import com.delphi.delphi.dtos.messaging.chat.PublishAnalyzeRepoJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.services.ChatService;
import com.delphi.delphi.utils.AssessmentCreationPrompts;
import com.delphi.delphi.utils.enums.JobStatus;

@Component
/**
 * Subscribes to the analyze repository topic and processes the job.
 */
public class RepoAnalyzerWorker {
    private final JobRepository jobRepository;
    private final ChatService chatService;
    private final Logger log = LoggerFactory.getLogger(RepoAnalyzerWorker.class);

    public RepoAnalyzerWorker(JobRepository jobRepository, ChatService chatService) {
        this.jobRepository = jobRepository;
        this.chatService = chatService;
    }

    @KafkaListener(topics = KafkaTopicsConfig.LLM_ANALYZE_BASE_REPO, containerFactory = "kafkaListenerContainerFactory")
    public void processAnalyzeRepoJob(PublishAnalyzeRepoJobDto publishAnalyzeRepoJobDto) {
        final UUID jobId = publishAnalyzeRepoJobDto.getJobId();
        Job job = null;
        try {
            job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            job.setStatus(JobStatus.RUNNING);
            job = jobRepository.save(job);

            // Send SSE event that job is running
            chatService.sendSseEvent(jobId, "repo_analysis_running",
                    Map.of("message", "Analyzing repository structure and content", "jobId", jobId.toString(), "status",
                            JobStatus.RUNNING.toString()));

            // Run repository analysis agent
            log.info("Repository analysis job processing - running agent loop...", jobId.toString());
            chatService.getChatCompletion(
                    jobId,
                    List.of(), // No existing messages for new analysis
                    AssessmentCreationPrompts.BRANCH_CREATOR_USER_PROMPT,
                    publishAnalyzeRepoJobDto.getUserPromptVariables(),
                    publishAnalyzeRepoJobDto.getModel(),
                    // for storing chat messages into the assessment's chat history
                    publishAnalyzeRepoJobDto.getAssessment().getId(),
                    // for making calls to the github api
                    publishAnalyzeRepoJobDto.getUser().getGithubAccessToken(),
                    publishAnalyzeRepoJobDto.getUser().getGithubUsername(),
                    publishAnalyzeRepoJobDto.getAssessment().getGithubRepoName());

            log.info("Saving completed repository analysis job with ID: {}", jobId.toString());
            job.setStatus(JobStatus.COMPLETED);
            job.setResult("Repository analysis completed successfully");
            jobRepository.save(job);

            // Send SSE event that repository analysis is complete
            chatService.sendSseEvent(jobId, "repo_analysis_completed",
                    Map.of("message", "Repository analysis completed successfully", "jobId", jobId.toString(),
                            "assessmentId", publishAnalyzeRepoJobDto.getAssessment().getId(), "status",
                            JobStatus.COMPLETED.toString()));

            // Complete the SSE emitter
            chatService.completeSseEmitter(jobId);

        } catch (IllegalArgumentException e) {
            log.error("Validation error in repository analysis job {}: {}", jobId, e.getMessage());
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Validation error: " + e.getMessage());

        } catch (RuntimeException e) {
            log.error("Runtime error in repository analysis job {}: {}", jobId, e.getMessage(), e);
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Service error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error in repository analysis job {}: {}", jobId, e.getMessage(), e);
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
