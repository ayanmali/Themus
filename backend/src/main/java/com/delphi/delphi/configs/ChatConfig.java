package com.delphi.delphi.configs;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public HttpHeaders githubClientHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.valueOf("application/vnd.github+json")));
        return headers;
    }
}
