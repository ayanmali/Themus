package com.delphi.delphi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.ResendService;
import com.delphi.delphi.dtos.messaging.emails.EmailRequestDto;
import com.delphi.delphi.dtos.messaging.emails.ScheduledEmailRequestDto;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final ResendService resendService;

    public EmailController(ResendService resendService) {
        this.resendService = resendService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<EmailRequestDto> sendEmail(@RequestBody EmailRequestDto emailRequest) {
        // TODO: implement custom from address
        resendService.sendEmail(emailRequest.getTo().getEmail(), emailRequest.getSubject(), emailRequest.getText());
        return ResponseEntity.ok(emailRequest);
    }

    @PostMapping("/send-scheduled")
    public ResponseEntity<ScheduledEmailRequestDto> sendScheduledEmail(@RequestBody ScheduledEmailRequestDto emailRequest) {
        // TODO: implement custom from address
        resendService.sendScheduledEmail(emailRequest.getTo().getEmail(), emailRequest.getSubject(), emailRequest.getText(), emailRequest.getScheduledAt());
        return ResponseEntity.ok(emailRequest);
    }
    
}