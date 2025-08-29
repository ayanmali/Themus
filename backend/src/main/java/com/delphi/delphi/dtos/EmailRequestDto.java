package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

public class EmailRequestDto {
    private Long candidateId;
    private String subject;
    private String text;
    private LocalDateTime scheduledAt;

    public EmailRequestDto(Long candidateId, String subject, String text, LocalDateTime scheduledAt) {
        this.candidateId = candidateId;
        this.subject = subject;
        this.text = text;
        this.scheduledAt = scheduledAt;
    }

    public Long getCandidateId() {
        return candidateId;
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

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
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
    
}
