package com.delphi.delphi.configs;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatConfig {
    // @Bean
    // public ChatClient chatClient() {
    //     return ChatClient.builder(
    //         new OpenAiChatModel(
    //             new OpenAiChatModelProvider("anthropic/claude-sonnet-4")
    //             )
    //         )
    //     .build();
    // }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Map<String, String> author() {
        return Map.of("name", "themus-bot[bot]", "email", "220768808+delphi-assessments[bot]@users.noreply.github.com");
    }
}
