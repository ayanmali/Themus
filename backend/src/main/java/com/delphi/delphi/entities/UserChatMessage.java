package com.delphi.delphi.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_message")
public class UserChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "model", nullable = false)
    private String model;

    @ManyToOne
    @JoinColumn(name = "chat_history_id", nullable = false)
    private UserChatHistory chatHistory;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private MessageSender sender;

    public UserChatMessage() {
    }

    public UserChatMessage(String message, UserChatHistory chatHistory, MessageSender sender, String model) {
        this.message = message;
        this.chatHistory = chatHistory;
        this.sender = sender; 
        this.model = model;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserChatHistory getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(UserChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }

    public MessageSender getSender() {
        return sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public enum MessageSender {
        USER,
        AI
    }
    
}