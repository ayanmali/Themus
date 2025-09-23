package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class BroadcastEmailRequestDto extends EmailRequestDto {
    private List<Long> candidateIds;

    public BroadcastEmailRequestDto() {
        super();
    }

    public BroadcastEmailRequestDto(EmailRequestDto emailRequest, List<Long> candidateIds) {
        super(emailRequest.getSubject(), emailRequest.getText(), emailRequest.getScheduledAt());
        this.candidateIds = candidateIds;
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
    
    public List<Long> getCandidateIds() {
        return candidateIds;
    }

    @Override
    public void setSubject(String subject) {
        super.setSubject(subject);
    }

    @Override
    public void setScheduledAt(LocalDateTime scheduledAt) {
        super.setScheduledAt(scheduledAt);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    public void setCandidateIds(List<Long> candidateIds) {
        this.candidateIds = candidateIds;
    }
}
