package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

public class ScheduledEmailRequestDto {
    // private String from;
    private String to;
    private String subject;
    private String text;
    private LocalDateTime scheduledAt;

    public ScheduledEmailRequestDto(String to, String subject, String text, LocalDateTime scheduledAt) {
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.scheduledAt = scheduledAt;
    }

    public String getTo() {
        return to;
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

    public void setTo(String to) {
        this.to = to;
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