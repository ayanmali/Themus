package com.delphi.delphi.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import com.delphi.delphi.utils.OpenAiToolCall;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "model")
    private String model;
    
    @ManyToOne
    @JoinColumn(name = "chat_history_id", nullable = false)
    private ChatHistory chatHistory;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    // @ElementCollection
    // @CollectionTable(name = "message_metadata", joinColumns = @JoinColumn(name = "message_id"))
    // @MapKeyColumn(name = "metadata_key")
    // @Column(name = "metadata_value")
    // private Map<String, String> metadata;

    // @ElementCollection
    // @CollectionTable(name = "message_tool_calls", joinColumns = @JoinColumn(name = "message_id"))
    // @Column(name = "tool_call")
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpenAiToolCall> toolCalls;

    public ChatMessage() {
    }

    public ChatMessage(String text, List<OpenAiToolCall> toolCalls, ChatHistory chatHistory, MessageType messageType, String model) {
        this.text = text;
        this.chatHistory = chatHistory;
        this.model = model;
        this.messageType = messageType;
        if (!toolCalls.isEmpty()) {
            this.toolCalls = toolCalls;
        }
    }

    public ChatMessage(AssistantMessage message, ChatHistory chatHistory, String model) {
        this.text = message.getText();
        this.chatHistory = chatHistory;
        this.messageType = message.getMessageType();
        this.model = model;
        if (!message.getToolCalls().isEmpty()) {
            this.toolCalls = message.getToolCalls().stream().map(
                                toolCall -> new OpenAiToolCall(toolCall)
                             ).collect(Collectors.toList());
        }
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(ChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    // public Map<String, String> getMetadata() {
    //     return metadata;
    // }

    // public void setMetadata(Map<String, String> metadata) {
    //     this.metadata = metadata;
    // }

    public Message toMessage() {
        switch (this.getMessageType()) {
            case USER -> {
                return new UserMessage(this.getText());
            }
            case ASSISTANT -> {
                return new AssistantMessage(this.getText());
            }
            case SYSTEM -> {
                return new SystemMessage(this.getText());
            }
            default -> throw new IllegalArgumentException("Invalid message type: " + this.getMessageType());
        }
        // case TOOL:
        //     return new ToolResponseMessage(this.getText());
    }

    public List<OpenAiToolCall> getToolCalls() {
        return toolCalls;
    }

    public void addToolCall(String name, String arguments, String id) {
        this.toolCalls.add(new OpenAiToolCall(name, arguments, id));
    }

    public void addToolCall(OpenAiToolCall toolCall) {
        this.toolCalls.add(toolCall);
    }

    public void setToolCalls(List<OpenAiToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
}