package com.delphi.delphi.components;

import org.springframework.stereotype.Component;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

// For sending emails
@Component
public class ResendService {
    private final Resend resend;

    public ResendService(Resend resend) {
        this.resend = resend;
    }

    public void sendEmail(String from, String to, String subject, String text) {
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
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println(data.getId());
        } catch (ResendException e) {
            System.out.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
