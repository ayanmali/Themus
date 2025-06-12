package com.delphi.delphi.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.delphi.delphi.dtos.NewAssessmentDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// @RestController
// @RequestMapping("/api/ai")
@Service
public class LLMService {

    private final ChatModel chatModel; // Autowired via constructor injection
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    public LLMService(ChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("LLMService initialized with Spring AI ChatModel, targeting OpenRouter.");
    }

    /**
     * Data Transfer Object for the chat request.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String message;
    }

    /**
     * Internal method to call the AI model.
     */
    private String getChatCompletion(String userMessage) {
        log.info("Sending prompt to OpenRouter model: '{}'", userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties
            return this.chatModel.call(userMessage);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    @PostMapping("/assessments/new")
    public ResponseEntity<String> createAssessment(@RequestBody NewAssessmentDto assessmentRequest) {
        log.info("Creating assessment: {}", assessmentRequest);
        String assessment = getChatCompletion(assessmentRequest.toString());
        log.info("Assessment created: {}", assessment);
        return ResponseEntity.ok("Assessment created successfully");
    }

    /**
     * REST API endpoint to interact with the AI model.
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithAi(@RequestBody ChatRequest chatRequest) {
        if (chatRequest == null || chatRequest.getMessage() == null || chatRequest.getMessage().isBlank()) {
            log.warn("Received empty or null chat request message.");
            return ResponseEntity.badRequest().body("Message content cannot be empty.");
        }

        log.info("Processing chat request for message: '{}'", chatRequest.getMessage().substring(0, Math.min(chatRequest.getMessage().length(), 100)) + "...");
        try {
            String response = getChatCompletion(chatRequest.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Error already logged in getChatCompletion
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sorry, an error occurred while communicating with the AI service:" + e.getMessage());
        }
    }
}
