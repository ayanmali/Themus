package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailRequestDto {
    private String subject;
    private String text;
    private LocalDateTime scheduledAt;

    public EmailRequestDto() {
        // Default constructor required for JSON deserialization
    }

    public EmailRequestDto(String subject, String text, LocalDateTime scheduledAt) {
        this.subject = subject;
        this.text = text;
        this.scheduledAt = scheduledAt;
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
