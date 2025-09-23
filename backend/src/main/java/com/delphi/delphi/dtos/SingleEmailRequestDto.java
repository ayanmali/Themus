package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleEmailRequestDto extends EmailRequestDto {
    private Long candidateId;

    public SingleEmailRequestDto() {
        super();
    }


    public SingleEmailRequestDto(Long candidateId, EmailRequestDto emailRequest) {
        super(emailRequest.getSubject(), emailRequest.getText(), emailRequest.getScheduledAt());
        this.candidateId = candidateId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    @Override
    public String getSubject() {
        return super.getSubject();
    }

    @Override
    public String getText() {
        return super.getText();
    }
    
    @Override
    public LocalDateTime getScheduledAt() {
        return super.getScheduledAt();
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    @Override
    public void setSubject(String subject) {
        super.setSubject(subject);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    @Override
    public void setScheduledAt(LocalDateTime scheduledAt) {
        super.setScheduledAt(scheduledAt);
    }
    
}
