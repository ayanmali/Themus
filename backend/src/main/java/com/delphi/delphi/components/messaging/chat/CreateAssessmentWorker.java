package com.delphi.delphi.components.messaging.chat;

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
import com.delphi.delphi.utils.JobStatus;
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
        Job job = jobRepository.findById(publishAssessmentCreationJobDto.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        try {
            job.setStatus(JobStatus.RUNNING);
            jobRepository.save(job);
            // Agent loop
            log.info("Create assessment job processing - running agent loop...", publishAssessmentCreationJobDto.getJobId().toString());
            chatService.getChatCompletion(
                AssessmentCreationPrompts.USER_PROMPT, 
                publishAssessmentCreationJobDto.getUserPromptVariables(), 
                publishAssessmentCreationJobDto.getModel(), 
                // for storing chat messages into the assessment's chat history
                publishAssessmentCreationJobDto.getAssessmentId(),
                // for making calls to the github api
                publishAssessmentCreationJobDto.getEncryptedGithubToken(),
                publishAssessmentCreationJobDto.getGithubUsername(),
                publishAssessmentCreationJobDto.getGithubRepoName()
            );

            log.info("Saving completed assessment creation job with ID: {}", publishAssessmentCreationJobDto.getJobId().toString());    
            job.setStatus(JobStatus.COMPLETED);
            job.setResult("Assessment created successfully");
            jobRepository.save(job);

        // } catch (InterruptedException e) {
        //     job.setStatus(JobStatus.FAILED);
        //     job.setResult(e.getMessage());
        //     log.error("Error processing chat completion request {}: {}", publishAssessmentCreationJobDto.getJobId().toString(), e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            job.setStatus(JobStatus.FAILED);
            job.setResult(e.getMessage());
            log.error("Error processing chat completion request {}: {}", publishAssessmentCreationJobDto.getJobId().toString(), e.getMessage(), e);
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setResult(e.getMessage());
            log.error("Error processing chat completion request {}: {}", publishAssessmentCreationJobDto.getJobId().toString(), e.getMessage(), e);
        } 
    }
}
