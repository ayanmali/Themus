package com.delphi.delphi.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CancelEmailResponse;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

// For sending emails
@Component
public class ResendService {
    private final Resend resend;
    private final String defaultFrom;
    private final Logger log = LoggerFactory.getLogger(ResendService.class);

    public ResendService(Resend resend, @Value("${resend.default.from}") String defaultFrom) {
        this.resend = resend;
        this.defaultFrom = defaultFrom;
    }

    public CreateEmailResponse sendEmail(String from, String to, String subject, String text) {
        log.info("Sending email from: {}", from);
        log.info("Sending email to: {}", to);
        log.info("Email subject: {}", subject);
        log.info("Email text: {}", text);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                // .cc("carbon@example.com", "copy@example.com")
                // .bcc("blind@example.com", "carbon.copy@example.com")
                // .replyTo("reply@example.com", "to@example.com")
                .text(text)
                .subject(subject)
                .build();

        try {
            return resend.emails().send(params);
            // System.out.println(data.getId());
        } catch (ResendException e) {
            log.error("Error sending email: " + e.getMessage());
            return null;
        }
    }

    public CreateEmailResponse sendEmail(String to, String subject, String text) {
        log.info("Sending email from: {}", defaultFrom);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(defaultFrom)
                .to(to)
                // .cc("carbon@example.com", "copy@example.com")
                // .bcc("blind@example.com", "carbon.copy@example.com")
                // .replyTo("reply@example.com", "to@example.com")
                .text(text)
                .subject(subject)
                .build();

        try {
            return resend.emails().send(params);
            // System.out.println(data.getId());
        } catch (ResendException e) {
            log.error("Error sending email: " + e.getMessage());
            return null;
        }
    }

    // public CreateEmailResponse sendScheduledEmail(String from, String to, String subject, String text, LocalDateTime scheduledAt) {
    //     CreateEmailOptions params = CreateEmailOptions.builder()
    //             .from(from)
    //             .to(to)
    //             .subject(subject)
    //             .html(text)
    //             .scheduledAt(scheduledAt.toString())
    //             .build();
    //     try {
    //         return resend.emails().send(params);
    //     } catch (ResendException e) {
    //         log.error("Error sending email: " + e.getMessage());
    //         return null;
    //     }
    // }

    // public CreateEmailResponse sendScheduledEmail(String to, String subject, String text, LocalDateTime scheduledAt) {

    //     CreateEmailOptions params = CreateEmailOptions.builder()
    //             .from(defaultFrom)
    //             .to(to)
    //             .subject(subject)
    //             .html(text)
    //             .scheduledAt(scheduledAt.toString())
    //             .build();
    //     try {
    //         return resend.emails().send(params);
    //     } catch (ResendException e) {
    //         log.error("Error sending email: " + e.getMessage());
    //         return null;
    //     }
    // }

    public CancelEmailResponse cancelScheduledEmail(String emailId) {
        try {
            return resend.emails().cancel(emailId);
        } catch (ResendException e) {
            log.error("Error canceling email: " + e.getMessage());
            return null;
        }
    }
}