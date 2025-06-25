package com.delphi.delphi.entities;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_history")
// complete context window for the chat history
public class ChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chatHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> messages;

    @OneToOne
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    public ChatHistory() {
    }

    public ChatHistory(List<ChatMessage> messages, Assessment assessment) {
        this.messages = messages;
        this.assessment = assessment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public List<Message> getMessagesAsSpringMessages() {
        return this.messages.stream().map(ChatMessage::toMessage).collect(Collectors.toList());
    }
}
