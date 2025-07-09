package com.delphi.delphi.dtos.messaging.emails;

import java.io.Serializable;

import com.delphi.delphi.entities.Candidate;

public class EmailRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // private String from;
    private Candidate to;
    private String subject;
    private String text;
    // private String requestId;
    // private Long retryCount;

    public EmailRequestDto(Candidate to, String subject, String text) {
        this.to = to;
        this.subject = subject;
        this.text = text;
    }

    public Candidate getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public void setTo(Candidate to) {
        this.to = to;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
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