package com.delphi.delphi.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.UserChatHistory;
import com.delphi.delphi.entities.UserChatMessage;
import com.delphi.delphi.entities.UserChatMessage.MessageSender;
import com.delphi.delphi.repositories.UserChatHistoryRepository;
import com.delphi.delphi.repositories.UserChatMessageRepository;

@Service
@Transactional
/*
 * Service for managing user chat history and messages.
 * 
 * This service is used to create, get, update, and delete chat histories and messages.
 * It is also used to add messages to a chat history.
 * It is also used to get messages by chat history id.
 * It is also used to get a message by id.
 * It is also used to create a message.
 */
public class UserChatService {

    private final UserChatHistoryRepository chatHistoryRepository;

    private final UserChatMessageRepository chatMessageRepository;

    private final ChatModel chatModel;

    private static final Logger log = LoggerFactory.getLogger(UserChatService.class);

    public UserChatService(UserChatHistoryRepository chatHistoryRepository, UserChatMessageRepository chatMessageRepository, ChatModel chatModel) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
        log.info("UserChatService initialized with Spring AI ChatModel, targeting OpenRouter.");
    }

    /**
     * Method to call the AI model.
     */
    
     public String getChatCompletion(String userMessage) {
        log.info("Sending prompt to OpenRouter model: '{}'", userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties
            return this.chatModel.call(userMessage);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    public UserChatHistory createChatHistory(UserChatHistory chatHistory) {
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

    public void addMessageToChatHistory(UserChatMessage message) throws Exception {
        UserChatHistory existingChatHistory = getChatHistoryById(message.getChatHistory().getId());
        existingChatHistory.getMessages().add(message);
        chatHistoryRepository.save(existingChatHistory);
    }

    public UserChatMessage addMessageToChatHistory(String message, Long chatHistoryId, MessageSender sender) throws Exception {
        UserChatMessage chatMessage = new UserChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setChatHistory(getChatHistoryById(chatHistoryId));
        chatMessage.setSender(sender);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

    /* Get a message by chat history id */
    public List<UserChatMessage> getMessagesByChatHistoryId(Long id) throws Exception {
        UserChatHistory existingChatHistory = getChatHistoryById(id);
        return existingChatHistory.getMessages();
    }

    /*
     * Get a message by message id
     */
    public UserChatMessage getMessageById(Long id) throws Exception {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id));
    }

    // public UserChatMessage createMessage(UserChatMessage message) throws Exception {
    //     return chatMessageRepository.save(message);
    // }

    public UserChatMessage updateMessage(Long id, UserChatMessage message) throws Exception {
        UserChatMessage existingMessage = getMessageById(id);
        existingMessage.setMessage(message.getMessage());
        return chatMessageRepository.save(existingMessage);
    }

    public void deleteMessage(Long id) throws Exception {
        UserChatMessage existingMessage = getMessageById(id);
        chatMessageRepository.delete(existingMessage);
    }

}
