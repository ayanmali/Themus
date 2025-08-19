package com.delphi.delphi.components.messaging.chat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.chat.PublishAssessmentCreationJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.utils.JobStatus;

@Component
public class CreateAssessmentWorker {
    private final JobRepository jobRepository;

    public CreateAssessmentWorker(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @RabbitListener(queues = TopicConfig.CREATE_ASSESSMENT_QUEUE_NAME)
    public void processCreateAssessmentJob(PublishAssessmentCreationJobDto publishAssessmentCreationJobDto) {
        Job job = jobRepository.findById(publishAssessmentCreationJobDto.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.RUNNING);
        jobRepository.save(job);

        try {
            // ChatResponse response;
            // Simulate long-running agent loop
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                output.append("Iteration ").append(i).append(" result for: ").append("Prompt text here...").append("\n");
                Thread.sleep(2000); // pretend tool calls
            }

            job.setStatus(JobStatus.COMPLETED);
            job.setResult(output.toString());
        } catch (InterruptedException e) {
            job.setStatus(JobStatus.FAILED);
            job.setResult(e.getMessage());
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setResult(e.getMessage());
        }

        jobRepository.save(job);
    }
}
