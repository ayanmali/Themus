package com.delphi.delphi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.ResendService;
import com.delphi.delphi.dtos.messaging.emails.EmailRequestDto;
import com.delphi.delphi.dtos.messaging.emails.ScheduledEmailRequestDto;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.UserService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final ResendService resendService;
    private final UserService userService;

    public EmailController(ResendService resendService, UserService userService) {
        this.resendService = resendService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
    
    @PostMapping("/send")
    public ResponseEntity<EmailRequestDto> sendEmail(@RequestBody EmailRequestDto emailRequest) {
        // TODO: implement custom from address
        //String fromEmail = getCurrentUser().getEmail();
        resendService.sendEmail(emailRequest.getTo().getEmail(), emailRequest.getSubject(), emailRequest.getText());
        // resendService.sendEmail(fromEmail, emailRequest.getTo().getEmail(), emailRequest.getSubject(), emailRequest.getText());
        return ResponseEntity.ok(emailRequest);
    }

    @PostMapping("/send-scheduled")
    public ResponseEntity<ScheduledEmailRequestDto> sendScheduledEmail(@RequestBody ScheduledEmailRequestDto emailRequest) {
        // TODO: implement custom from address
        //String fromEmail = getCurrentUser().getEmail();
        resendService.sendScheduledEmail(emailRequest.getTo().getEmail(), emailRequest.getSubject(), emailRequest.getText(), emailRequest.getScheduledAt());
        //resendService.sendScheduledEmail(fromEmail, emailRequest.getTo().getEmail(), emailRequest.getSubject(), emailRequest.getText(), emailRequest.getScheduledAt());
        return ResponseEntity.ok(emailRequest);
    }
    
}