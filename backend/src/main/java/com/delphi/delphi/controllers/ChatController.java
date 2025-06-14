// package com.delphi.delphi.controllers;

// import java.util.List;

// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.chat.messages.SystemMessage;
// import org.springframework.ai.chat.messages.UserMessage;
// import org.springframework.ai.chat.model.ChatResponse;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.delphi.delphi.dtos.NewUserMessageDto;
// import com.delphi.delphi.entities.UserChatMessage;
// import com.delphi.delphi.services.UserChatService;
// import static com.delphi.delphi.utils.RepoGenSystemPrompt.REPO_GEN_SYSTEM_PROMPT;

// @RestController
// @RequestMapping("/api/chat")
// /*
//      * Controller for managing user chat history and messages.
//      * 
//      * This controller is used to create, get, update, and delete chat histories and messages.
//      * It is also used to add messages to a chat history.
//      * It is also used to get messages by chat history id.
//      * It is also used to get a message by id.
//      * It is also used to create a message.
//     */
// public class ChatController {

//     private final UserChatService chatService;
//     private final ChatClient llmClient;

//     private final SystemMessage SYSTEM_PROMPT = new SystemMessage(REPO_GEN_SYSTEM_PROMPT);
//     /*
//      * chatService - Service for managing chat histories and messages
//      * llmClient - Client for managing LLM API calls
//      */
//     public ChatController(UserChatService chatService, ChatClient.Builder chatClientBuilder) {
//         this.chatService = chatService;
//         this.llmClient = chatClientBuilder.build();
//     }

//     // User sending a request to the LLM API
//     @PostMapping("/messages")
//     public ResponseEntity<?> createMessage(@RequestBody NewUserMessageDto messageDto) {
//         try {
//             UserMessage userMessage = new UserMessage(messageDto.getMessage());
//             ChatResponse response = llmClient
//             .prompt(userMessage.getText())
//             .messages(List.of(SYSTEM_PROMPT, userMessage))
//             .call()
//             .chatResponse();

//             System.out.println(response.getMessages().get(0).getContent());

//             // storing message in DB
//             UserChatMessage createdMessage = chatService.addMessageToChatHistory(messageDto.getMessage(), messageDto.getChatHistoryId(), UserChatMessage.MessageSender.USER);
//             return ResponseEntity.ok(createdMessage);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body("Error creating message: " + e.getMessage());
//         }
//     }

//     // Get a message by message id
//     @GetMapping("/messages/{chatMessageId}")
//     public ResponseEntity<?> getMessage(@PathVariable Long chatMessageId) {
//         try {
//             UserChatMessage message = chatService.getMessageById(chatMessageId);
//             return ResponseEntity.ok(message);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body("Error getting message: " + e.getMessage());
//         }
//     }

//     // Get a message by chat history id
//     @GetMapping("/messages/chat-history/{chatHistoryId}")
//     public ResponseEntity<?> getMessagesByChatHistoryId(@PathVariable Long chatHistoryId) {
//         try {
//             List<UserChatMessage> messages = chatService.getMessagesByChatHistoryId(chatHistoryId);
//             return ResponseEntity.ok(messages);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body("Error getting messages: " + e.getMessage());
//         }
//     }
// }
