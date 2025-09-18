package com.delphi.delphi.configs;

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

}
