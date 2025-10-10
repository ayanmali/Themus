package com.delphi.delphi.components.messaging.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.kafka.KafkaTopicsConfig;
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

    @KafkaListener(topics = KafkaTopicsConfig.LLM_CREATE_ASSESSMENT, containerFactory = "kafkaListenerContainerFactory")
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

            // Check if base repository URL is provided for analysis
            if (publishAssessmentCreationJobDto.getBaseRepoUrl() != null && !publishAssessmentCreationJobDto.getBaseRepoUrl().isEmpty()) {
                log.info("Base repository URL provided, starting repository analysis first...");
                
                // Send SSE event that repository analysis is starting
                chatService.sendSseEvent(jobId, "repo_analysis_starting",
                        Map.of("message", "Starting repository analysis", "jobId", jobId.toString(), "status",
                                JobStatus.RUNNING.toString()));

                // First, run repository analysis
                @SuppressWarnings("unchecked")
                List<String> languageOptions = (List<String>) publishAssessmentCreationJobDto.getUserPromptVariables().getOrDefault("LANGUAGE_OPTIONS", List.of());
                Map<String, Object> repoAnalysisVariables = Map.of(
                    "TECH_STACK", String.join(", ", languageOptions),
                    "BRANCH_NAME", "main" // Default branch name
                );

                log.info("Repository analysis job processing - running agent loop...", jobId.toString());
                chatService.getChatCompletion(
                        jobId,
                        List.of(), // No existing messages for new analysis
                        AssessmentCreationPrompts.BRANCH_CREATOR_USER_PROMPT,
                        repoAnalysisVariables,
                        "@preset/repo-analyzer",
                        // for storing chat messages into the assessment's chat history
                        publishAssessmentCreationJobDto.getAssessmentId(),
                        // for making calls to the github api
                        publishAssessmentCreationJobDto.getEncryptedGithubToken(),
                        publishAssessmentCreationJobDto.getGithubUsername(),
                        publishAssessmentCreationJobDto.getGithubRepoName());

                // Send SSE event that repository analysis is complete
                chatService.sendSseEvent(jobId, "repo_analysis_completed",
                        Map.of("message", "Repository analysis completed, starting assessment creation", "jobId", jobId.toString(), "status",
                                JobStatus.RUNNING.toString()));
            }

            // Agent loop for assessment creation
            log.info("Create assessment job processing - running agent loop...", jobId.toString());
            
            // Add repository analysis context to user prompt variables if available
            Map<String, Object> assessmentVariables = publishAssessmentCreationJobDto.getUserPromptVariables();
            if (publishAssessmentCreationJobDto.getBaseRepoUrl() != null && !publishAssessmentCreationJobDto.getBaseRepoUrl().isEmpty()) {
                // TODO: Extract repository analysis results from chat history and add to context
                assessmentVariables.put("REPO_ANALYSIS_CONTEXT", "\n\n<REPOSITORY_ANALYSIS>\nRepository analysis results will be included here based on the previous analysis.\n</REPOSITORY_ANALYSIS>");
            } else {
                assessmentVariables.put("REPO_ANALYSIS_CONTEXT", "");
            }

            chatService.getChatCompletion(
                    jobId,
                    List.of(), // No existing messages for new assessment
                    AssessmentCreationPrompts.USER_PROMPT,
                    assessmentVariables,
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
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Validation error: " + e.getMessage());

        } catch (RuntimeException e) {
            log.error("Runtime error in assessment creation job {}: {}", jobId, e.getMessage(), e);
            MessageUtils.handleJobFailure(chatService, jobRepository, job, jobId, e, "Service error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error in assessment creation job {}: {}", jobId, e.getMessage(), e);
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