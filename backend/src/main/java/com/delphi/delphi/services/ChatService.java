package com.delphi.delphi.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.ChatHistory;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.entities.ChatMessage.MessageSender;
import com.delphi.delphi.repositories.ChatHistoryRepository;
import com.delphi.delphi.repositories.ChatMessageRepository;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatHistory createChatHistory(ChatHistory chatHistory) {
        return chatHistoryRepository.save(chatHistory);
    }

    public ChatHistory getChatHistoryById(Long id) throws Exception {
        try {
            return chatHistoryRepository.findById(id)
                    .orElseThrow(() -> new Exception("Chat history not found with id: " + id));
        } catch (Exception e) {
            throw new Exception("Chat history not found with id: " + id);
        }
    }

    public ChatHistory updateChatHistory(Long id, ChatHistory chatHistory) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(id);
        existingChatHistory.setAssessment(chatHistory.getAssessment());
        return chatHistoryRepository.save(existingChatHistory);
    }

    public void deleteChatHistory(Long id) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(id);
        chatHistoryRepository.delete(existingChatHistory);
    }

    public List<ChatHistory> getAllChatHistories() {
        return chatHistoryRepository.findAll();
    }

    public void addMessageToChatHistory(ChatMessage message, MessageSender sender) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(message.getChatHistory().getId());
        existingChatHistory.getMessages().add(message);
        chatHistoryRepository.save(existingChatHistory);
    }

    public void addMessageToChatHistory(String message, Long chatHistoryId, MessageSender sender) throws Exception {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setChatHistory(getChatHistoryById(chatHistoryId));
        chatMessage.setSender(sender);
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessagesByChatHistoryId(Long id) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(id);
        return existingChatHistory.getMessages();
    }

    public ChatMessage getMessageById(Long id) throws Exception {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id));
    }

    public ChatMessage createMessage(ChatMessage message) throws Exception {
        return chatMessageRepository.save(message);
    }

    public ChatMessage updateMessage(Long id, ChatMessage message) throws Exception {
        ChatMessage existingMessage = getMessageById(id);
        existingMessage.setMessage(message.getMessage());
        return chatMessageRepository.save(existingMessage);
    }

    public void deleteMessage(Long id) throws Exception {
        ChatMessage existingMessage = getMessageById(id);
        chatMessageRepository.delete(existingMessage);
    }

}
