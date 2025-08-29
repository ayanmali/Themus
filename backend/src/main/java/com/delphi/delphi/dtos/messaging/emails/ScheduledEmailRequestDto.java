// package com.delphi.delphi.dtos.messaging.emails;

// import java.io.Serializable;
// import java.time.LocalDateTime;

// import com.delphi.delphi.entities.Candidate;

// public class ScheduledEmailRequestDto implements Serializable {
//     private static final long serialVersionUID = 1L;
    
//     // private String from;
//     private Candidate to;  
//     private String subject;
//     private String text;
//     private LocalDateTime scheduledAt;

//     public ScheduledEmailRequestDto(Candidate to, String subject, String text, LocalDateTime scheduledAt) {
//         this.to = to;
//         this.subject = subject;
//         this.text = text;
//         this.scheduledAt = scheduledAt;
//     }

//     public Candidate getTo() {
//         return to;
//     }

//     public String getSubject() {
//         return subject;
//     }

//     public String getText() {
//         return text;
//     }

//     public LocalDateTime getScheduledAt() {
//         return scheduledAt;
//     }

//     public void setTo(Candidate to) {
//         this.to = to;
//     }

//     public void setSubject(String subject) {
//         this.subject = subject;
//     }

//     public void setText(String text) {
//         this.text = text;
//     }

//     public void setScheduledAt(LocalDateTime scheduledAt) {
//         this.scheduledAt = scheduledAt;
//     }
    
// }