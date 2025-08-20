package com.delphi.delphi.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // use a TEXT column type instead of VARCHAR to bypass the 255 character limit
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "model")
    private String model;
    
    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    @JsonIgnore
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "message_type", nullable = false, columnDefinition = "themus.message_type DEFAULT 'USER'")
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
    @JsonIgnore
    private List<OpenAiToolCall> toolCalls;

    public ChatMessage() {
    }

    public ChatMessage(String text, List<OpenAiToolCall> toolCalls, Assessment assessment, MessageType messageType, String model) {
        this.text = text;
        this.assessment = assessment;
        this.model = model;
        this.messageType = messageType;
        if (!toolCalls.isEmpty()) {
            this.toolCalls = toolCalls;
        }
    }

    public ChatMessage(AssistantMessage message, Assessment assessment, String model) {
        this.text = message.getText();
        this.assessment = assessment;
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

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
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