package com.delphi.delphi.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
public class ResendConfig {
    
    @Bean
    public Resend resend(@Value("${resend.api.key}") String resendApiKey) {
        return new Resend(resendApiKey);
    }
}
