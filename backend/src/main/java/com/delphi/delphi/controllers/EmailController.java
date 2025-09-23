package com.delphi.delphi.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.BroadcastEmailRequestDto;
import com.delphi.delphi.dtos.SingleEmailRequestDto;
import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.dtos.messaging.emails.PublishSendEmailJobDto;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.services.CandidateService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.enums.JobStatus;
import com.delphi.delphi.utils.enums.JobType;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final JobRepository jobRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CandidateService candidateService;

    private final UserService userService;

    private final Logger log = LoggerFactory.getLogger(EmailController.class);

    public EmailController(UserService userService, CandidateService candidateService, JobRepository jobRepository,
            RabbitTemplate rabbitTemplate) {
        this.userService = userService;
        this.candidateService = candidateService;
        this.jobRepository = jobRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    private UserCacheDto getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    /**
     * Sends an email to a single candidate
     * @param emailRequest
     * @return
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody SingleEmailRequestDto emailRequest) {
        // TODO: implement custom from address
        // String fromEmail = getCurrentUser().getEmail();
        log.info("Sending email to candidate: {}", emailRequest.getCandidateId());
        log.info("Email subject: {}", emailRequest.getSubject());
        log.info("Email message: {}", emailRequest.getText());
        CandidateCacheDto candidate = candidateService.getCandidateById(emailRequest.getCandidateId());
        log.info("Passing email request to queue");
        // Long jobId = UUID.randomUUID().getMostSignificantBits();
        Job job = new Job(JobStatus.PENDING, JobType.SEND_EMAIL);
        job = jobRepository.save(job);

        log.info("Job created: {}", job.getId());

        PublishSendEmailJobDto publishSendEmailJobDto = new PublishSendEmailJobDto(job.getId(), candidate,
                emailRequest);
        rabbitTemplate.convertAndSend(TopicConfig.EMAIL_TOPIC_EXCHANGE_NAME, TopicConfig.EMAIL_ROUTING_KEY,
                publishSendEmailJobDto);
        log.info("Email job published to queue");
        // resendService.sendEmail(candidate.getEmail(), emailRequest.getSubject(),
        // emailRequest.getText());
        // resendService.sendEmail(fromEmail, emailRequest.getTo().getEmail(),
        // emailRequest.getSubject(), emailRequest.getText());
        return ResponseEntity
                .ok(Map.of("jobId", job.getId(), "candidateId", candidate.getId(), "message", "Email sent"));
    }

    /**
     * Broadcasts an email to a list of candidates
     * @param emailRequest
     * @return
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastEmail(@RequestBody BroadcastEmailRequestDto emailRequest) {
        for (Long candidateId : emailRequest.getCandidateIds()) {
            CandidateCacheDto candidate = candidateService.getCandidateById(candidateId);
            log.info("Passing email request to queue");
            // Long jobId = UUID.randomUUID().getMostSignificantBits();
            Job job = new Job(JobStatus.PENDING, JobType.SEND_EMAIL);
            job = jobRepository.save(job);

            log.info("Job created: {}", job.getId());

            PublishSendEmailJobDto publishSendEmailJobDto = new PublishSendEmailJobDto(job.getId(), candidate,
                    emailRequest);
            rabbitTemplate.convertAndSend(TopicConfig.EMAIL_TOPIC_EXCHANGE_NAME, TopicConfig.EMAIL_ROUTING_KEY,
                    publishSendEmailJobDto);
            log.info("Email job published to queue");
        }
        return ResponseEntity.ok(Map.of("message", "Emails broadcasted"));
    }

    // @PostMapping("/send-scheduled")
    // public ResponseEntity<ScheduledEmailRequestDto>
    // sendScheduledEmail(@RequestBody ScheduledEmailRequestDto emailRequest) {
    // // TODO: implement custom from address
    // //String fromEmail = getCurrentUser().getEmail();
    // resendService.sendScheduledEmail(emailRequest.getTo().getEmail(),
    // emailRequest.getSubject(), emailRequest.getText(),
    // emailRequest.getScheduledAt());
    // //resendService.sendScheduledEmail(fromEmail,
    // emailRequest.getTo().getEmail(), emailRequest.getSubject(),
    // emailRequest.getText(), emailRequest.getScheduledAt());
    // return ResponseEntity.ok(emailRequest);
    // }

}