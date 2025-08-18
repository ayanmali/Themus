package com.delphi.delphi.components.messaging.chat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.chat.PublishChatJobDto;
import com.delphi.delphi.entities.jobs.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.utils.JobStatus;

@Component
public class LLMChatWorker {

    private final JobRepository jobRepository;

    public LLMChatWorker(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }
    
    @RabbitListener(queues = TopicConfig.LLM_CHAT_QUEUE_NAME)
    public void processChatJob(PublishChatJobDto publishChatJobDto) {
        Job job = jobRepository.findById(publishChatJobDto.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.RUNNING);
        jobRepository.save(job);

        try {
            // Simulate long-running agent loop
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                output.append("Iteration ").append(i).append(" result for: ").append(publishChatJobDto.getMessageText()).append("\n");
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
