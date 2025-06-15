// package com.delphi.delphi.services.agent;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.ai.chat.model.ChatModel;
// import org.springframework.ai.tool.annotation.Tool;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.delphi.delphi.entities.UserChatMessage;
// import com.delphi.delphi.services.UserChatService;

// // @RestController
// // @RequestMapping("/api/ai")
// @Service
// @Transactional
// public class LLMService {
//     private final UserChatService chatService;
//     private final ChatModel chatModel; // Autowired via constructor injection
//     private static final Logger log = LoggerFactory.getLogger(LLMService.class);

//     public LLMService(ChatModel chatModel, UserChatService chatService) {
//         this.chatModel = chatModel;
//         this.chatService = chatService;
//         log.info("LLMService initialized with Spring AI ChatModel, targeting OpenRouter.");
//     }

//     /**
//      * Data Transfer Object for the chat request.
//      */
//     public static class ChatRequest {
//         private String message;

//         public ChatRequest() {
//         }

//         public ChatRequest(String message) {
//             this.message = message;
//         }

//         public String getMessage() {
//             return message;
//         }

//         public void setMessage(String message) {
//             this.message = message;
//         }
//     }

//     // private String agentLoop() {
//     //     boolean isRunning = true;
//     //     while (isRunning) {
//     //         String userMessage = getChatCompletion(userMessage);
//     //         String response = getChatCompletion(userMessage);
//     //         System.out.println(response);
//     //     }
//     //     return "Agent loop ended";
//     // }

//     /**
//      * REST API endpoint to interact with the AI model.
//      */
//     // @PostMapping("/chat")
//     // public ResponseEntity<String> chatWithAi(@RequestBody ChatRequest chatRequest) {
//     //     if (chatRequest == null || chatRequest.getMessage() == null || chatRequest.getMessage().isBlank()) {
//     //         log.warn("Received empty or null chat request message.");
//     //         return ResponseEntity.badRequest().body("Message content cannot be empty.");
//     //     }

//     //     log.info("Processing chat request for message: '{}'", chatRequest.getMessage().substring(0, Math.min(chatRequest.getMessage().length(), 100)) + "...");
//     //     try {
//     //         String response = getChatCompletion(chatRequest.getMessage());
//     //         return ResponseEntity.ok(response);
//     //     } catch (Exception e) {
//     //         // Error already logged in getChatCompletion
//     //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//     //                 .body("Sorry, an error occurred while communicating with the AI service:" + e.getMessage());
//     //     }
//     // }

//     /* LLM Tools */

// }
