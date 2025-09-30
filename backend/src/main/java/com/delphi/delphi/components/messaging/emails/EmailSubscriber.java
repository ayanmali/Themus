package com.delphi.delphi.components.messaging.emails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.ResendService;
import com.delphi.delphi.configs.kafka.KafkaTopicsConfig;
import com.delphi.delphi.dtos.messaging.emails.PublishSendEmailJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.utils.enums.JobStatus;

@Component
public class EmailSubscriber {
    private final Logger log = LoggerFactory.getLogger(EmailSubscriber.class);
    private final JobRepository jobRepository;
    private final ResendService resendService;

    public EmailSubscriber(JobRepository jobRepository, ResendService resendService) {
        this.jobRepository = jobRepository;
        this.resendService = resendService;
    }

    @KafkaListener(topics = KafkaTopicsConfig.EMAIL, containerFactory = "kafkaListenerContainerFactory")
    public void processEmail(PublishSendEmailJobDto publishSendEmailJobDto) {
        log.info("Processing email to: {}", publishSendEmailJobDto.getToEmail());
        log.info("Processing email subject: {}", publishSendEmailJobDto.getSubject());
        log.info("Processing email text: {}", publishSendEmailJobDto.getText());
        Job job = jobRepository.findById(publishSendEmailJobDto.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.RUNNING);
        jobRepository.save(job);
        resendService.sendEmail(publishSendEmailJobDto.getToEmail(), publishSendEmailJobDto.getSubject(), publishSendEmailJobDto.getText());
        job.setStatus(JobStatus.COMPLETED);
        job.setResult("Email sent to " + publishSendEmailJobDto.getToEmail() + " successfully: " + publishSendEmailJobDto.getSubject());
        jobRepository.save(job);
        log.info("Email sent successfully");
    }
}
