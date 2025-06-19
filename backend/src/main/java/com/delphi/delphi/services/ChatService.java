package com.delphi.delphi.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.entities.UserChatHistory;
import com.delphi.delphi.repositories.ChatMessageRepository;
import com.delphi.delphi.repositories.UserChatHistoryRepository;

@Service
@Transactional
/*
 * Service for managing user chat history and messages.
 * 
 * This service is used to create, get, update, and delete chat histories and
 * messages.
 * It is also used to add messages to a chat history.
 * It is also used to get messages by chat history id.
 * It is also used to get a message by id.
 * It is also used to create a message.
 */
public class ChatService {

    private final UserChatHistoryRepository chatHistoryRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final ChatModel chatModel;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(UserChatHistoryRepository chatHistoryRepository,
            ChatMessageRepository chatMessageRepository, ChatModel chatModel) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
        log.info("ChatService initialized with Spring AI ChatModel, targeting OpenRouter.");
    }    

    /**
     * Spring AI Methods
     */

    /*
     * Get a chat completion from the AI model
     */
    public ChatResponse getChatCompletion(String userMessage, String model, Long chatHistoryId) {
        log.info("Sending prompt to OpenRouter model '{}':\nUSER MESSAGE: '{}'", model, userMessage);
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // Create a system message from a template and substitute the values
            // SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("Tell me a {adjective} joke about {topic}");
            // Message systemMessage = systemPromptTemplate.createMessage(Map.of("adjective", "offensive", "topic", "old people"));

            // get the chat history
            UserChatHistory chatHistory = getChatHistoryById(chatHistoryId);

            // get the messages from the chat history
            List<ChatMessage> messages = chatHistory.getMessages();
            // add the user message to the messages list
            messages.add(new ChatMessage(userMessage, chatHistory, MessageType.USER, model));

            // create a prompt message from template
            // PromptTemplate pt = new PromptTemplate("You are a helpful assistant. You are given a conversation history and a user message. You need to respond to the user message based on the conversation history. The conversation history is: {conversationHistory}. The user message is: {userMessage}. The response should be in the same language as the conversation history.");
            // Message systemMessage = pt.createMessage(Map.of("conversationHistory", messages, "userMessage", userMessage));

            // creating a prompt with a system message and a user message
            Prompt prompt = new Prompt(
                // convert the messages to Spring AI messages
                messages.stream().map(ChatMessage::toMessage).collect(Collectors.toList()),
                OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(0.75) // double between 0 and 1
                    .build()
            );

            // Create a new prompt with a user message
            // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
            //         .model(model)
            //         .build());

            // save the messages to the database
            chatMessageRepository.saveAll(messages);
            
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    /*
     * Get completion w/ a template system prompt string)
     * Surround variables with curly braces in the system prompt template ("{ }")
     */
    public ChatResponse getChatCompletion(String systemMessageTemplate, Map<String, Object> systemMessageVariables, String userMessage, String model) {
        log.info("Sending prompt to OpenRouter model '{}':\nSYSTEM PROMPT: '{}'\nUSER MESSAGE: '{}'",
                model,
                systemMessageTemplate.substring(0, Math.min(systemMessageTemplate.length(), 100)) + "...",
                userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // call a chat model with a string user message
            // ChatResponse response = chatModel.call(
            //         new Prompt(
            //                 userMessage,
            //                 OpenAiChatOptions.builder()
            //                         .model(model)
            //                         .build()));

            // Create a system message from a template and substitute the values
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemMessageTemplate);
            Message systemMessage = systemPromptTemplate.createMessage(systemMessageVariables);

            // creating a prompt with a system message and a user message
            Prompt prompt = new Prompt(
                List.of(
                    systemMessage,
                    new UserMessage(userMessage)),
                OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(0.75) // double between 0 and 1
                    .build()
            );

            // Create a new prompt with a user message
            // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
            //         .model(model)
            //         .build());
            
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }
    
    /*
     * Pass in a system prompt message (no template) and a user message
     */
    public ChatResponse getChatCompletion(String systemPromptMessage, String userMessage, String model) {
        log.info("Sending prompt to OpenRouter model '{}':\nSYSTEM PROMPT: '{}'\nUSER MESSAGE: '{}'",
                model,
                systemPromptMessage.substring(0, Math.min(systemPromptMessage.length(), 100)) + "...",
                userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // call a chat model with a string user message
            // ChatResponse response = chatModel.call(
            //         new Prompt(
            //                 userMessage,
            //                 OpenAiChatOptions.builder()
            //                         .model(model)
            //                         .build()));

            // Create a system message from a template and substitute the values

            // creating a prompt with a system message and a user message
            Prompt prompt = new Prompt(
                List.of(
                    new SystemMessage(systemPromptMessage),
                    new UserMessage(userMessage)),
                OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(0.75) // double between 0 and 1
                    .build()
            );

            // Create a new prompt with a user message
            // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
            //         .model(model)
            //         .build());
            
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    /*
     * Database Repository Methods
     */

    public UserChatHistory createChatHistory(UserChatHistory chatHistory, String systemMessage) throws Exception {
        // adding system prompt to chat history
        addMessageToChatHistory(systemMessage, chatHistory.getId(), MessageType.SYSTEM, "N/A", Map.of());
        // save chat history
        return chatHistoryRepository.save(chatHistory);
    }

    public UserChatHistory getChatHistoryById(Long id) throws Exception {
        try {
            return chatHistoryRepository.findById(id)
                    .orElseThrow(() -> new Exception("Chat history not found with id: " + id));
        } catch (Exception e) {
            throw new Exception("Chat history not found with id: " + id);
        }
    }

    public UserChatHistory updateChatHistory(Long id, UserChatHistory chatHistory) throws Exception {
        UserChatHistory existingChatHistory = getChatHistoryById(id);
        existingChatHistory.setAssessment(chatHistory.getAssessment());
        return chatHistoryRepository.save(existingChatHistory);
    }

    public void deleteChatHistory(Long id) throws Exception {
        UserChatHistory existingChatHistory = getChatHistoryById(id);
        chatHistoryRepository.delete(existingChatHistory);
    }

    public List<UserChatHistory> getAllChatHistories() {
        return chatHistoryRepository.findAll();
    }

    public void addMessageToChatHistory(ChatMessage message) throws Exception {
        UserChatHistory existingChatHistory = getChatHistoryById(message.getChatHistory().getId());
        existingChatHistory.getMessages().add(message);
        chatHistoryRepository.save(existingChatHistory);
    }

    public ChatMessage addMessageToChatHistory(String text, Long chatHistoryId, MessageType messageType, String model, Map<String, Object> metadata) throws Exception {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setText(text);
        chatMessage.setChatHistory(getChatHistoryById(chatHistoryId));
        chatMessage.setMessageType(messageType);
        chatMessage.setModel(model);
        chatMessage.setMetadata(metadata);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

    /* Get a message by chat history id */
    public List<ChatMessage> getMessagesByChatHistoryId(Long id) throws Exception {
        UserChatHistory existingChatHistory = getChatHistoryById(id);
        return existingChatHistory.getMessages();
    }

    /*
     * Get a message by message id
     */
    public ChatMessage getMessageById(Long id) throws Exception {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id));
    }

    // public ChatMessage createMessage(ChatMessage message) throws
    // Exception {
    // return chatMessageRepository.save(message);
    // }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessagesByChatId(Long chatHistoryId, Pageable pageable) {
        return chatMessageRepository.findByChatHistoryId(chatHistoryId, pageable);
    }

    public ChatMessage updateMessage(Long id, ChatMessage message) throws Exception {
        ChatMessage existingMessage = getMessageById(id);
        existingMessage.setText(message.getText());
        existingMessage.setMessageType(message.getMessageType());
        existingMessage.setMetadata(message.getMetadata());
        return chatMessageRepository.save(existingMessage);
    }

    public void deleteMessage(Long id) throws Exception {
        ChatMessage existingMessage = getMessageById(id);
        chatMessageRepository.delete(existingMessage);
    }

}
