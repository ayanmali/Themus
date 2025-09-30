package com.delphi.delphi.dtos.messaging.emails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.delphi.delphi.dtos.EmailRequestDto;
import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishSendEmailJobDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // private String from;
    private UUID jobId;
    private String toEmail;
    private String subject;
    private String text;
    private LocalDateTime scheduledAt;
    // private String requestId;
    // private Long retryCount;

    public PublishSendEmailJobDto() {
    }

    public PublishSendEmailJobDto(UUID jobId, CandidateCacheDto candidate, EmailRequestDto emailRequest) {
        this.jobId = jobId;
        this.toEmail = candidate.getEmail();
        this.subject = emailRequest.getSubject();
        this.text = emailRequest.getText();
        // optional field
        if (emailRequest.getScheduledAt() != null) {
            this.scheduledAt = emailRequest.getScheduledAt();
        }
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getToEmail() {
        return toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    // public String getRequestId() {
    //     return requestId;
    // }

    // public void setRequestId(String requestId) {
    //     this.requestId = requestId;
    // }
    
    // public Long getRetryCount() {
    //     return retryCount;
    // }

    // public void setRetryCount(Long retryCount) {
    //     this.retryCount = retryCount;
    // }

    // public void incrementRetryCount() {
    //     this.retryCount++;
    // }
}