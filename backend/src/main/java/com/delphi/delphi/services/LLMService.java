package com.delphi.delphi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.delphi.delphi.dtos.NewAssessmentDto;
import com.delphi.delphi.entities.UserChatMessage;

// @RestController
// @RequestMapping("/api/ai")
@Service
@Transactional
public class LLMService {
    private final UserChatService chatService;
    private final ChatModel chatModel; // Autowired via constructor injection
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    public LLMService(ChatModel chatModel, UserChatService chatService) {
        this.chatModel = chatModel;
        this.chatService = chatService;
        log.info("LLMService initialized with Spring AI ChatModel, targeting OpenRouter.");
    }

    /**
     * Data Transfer Object for the chat request.
     */
    public static class ChatRequest {
        private String message;

        public ChatRequest() {
        }

        public ChatRequest(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // sends a message to the user (to be used by the LLM as a tool)
    @Tool(description="Sends a message to the user after applying changes to the repository.")
    public String sendMessageToUser(String message, Long chatHistoryId) {
        try {
            chatService.addMessageToChatHistory(message, chatHistoryId, UserChatMessage.MessageSender.AI);
            return message;
        } catch (Exception e) {
            return "Error sending message: " + e.getMessage();
        }
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

    /* LLM Tools */

}
