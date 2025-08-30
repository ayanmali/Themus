package com.delphi.delphi.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_assessment_created", columnList = "assessment_id, created_at")
})
/**
 * Assistant messages can contain ToolCalls (i.e. requests to invoke a tool).
 * ToolResponseMessages contain the result of a tool call.
 */
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
    /**
     * Represents an LLM's request to invoke a tool.
     */
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OpenAiToolCall> toolCalls;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OpenAiToolResponse> toolResponses;

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

    /**
     * Takes a Spring AI message and converts it to a ChatMessage entity.
     * @param message
     * @param assessment
     * @param model
     */
    public ChatMessage (Message message, Assessment assessment, String model) {
        this.text = message.getText();
        this.assessment = assessment;
        this.model = model;
        this.messageType = message.getMessageType();

        switch (message.getMessageType()) {
            case MessageType.ASSISTANT -> {
                // storing tool calls
                if (message instanceof AssistantMessage assistantMessage) {
                    if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                        this.toolCalls = assistantMessage.getToolCalls().stream().map(
                                        toolCall -> new OpenAiToolCall(toolCall, this)
                                     ).collect(Collectors.toList());
                    }
                }
                break;
            }
            case MessageType.USER -> {
                break;
            }
            case MessageType.SYSTEM -> {
                break;
            }
            case MessageType.TOOL -> {
                // storing tool responses
                if (message instanceof ToolResponseMessage toolResponseMessage) {
                    if (toolResponseMessage.getResponses() != null && !toolResponseMessage.getResponses().isEmpty()) {
                        this.toolResponses = toolResponseMessage.getResponses().stream().map(
                                        toolResponse -> new OpenAiToolResponse(toolResponse, this)
                                     ).collect(Collectors.toList());
                    }
                }
                break;
            }
            default -> {
                throw new IllegalArgumentException("Invalid message type: " + message.getMessageType());
            }
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
                return new AssistantMessage(this.getText(), Map.of(), this.getToolCalls().stream().map(OpenAiToolCall::toToolCall).collect(Collectors.toList()));
            }
            case SYSTEM -> {
                return new SystemMessage(this.getText());
            }
            case TOOL -> {
                // For tool response messages, return a basic message
                // We'll handle tool responses separately in the ChatService
                return new ToolResponseMessage(this.getToolResponses().stream().map(OpenAiToolResponse::toToolResponse).collect(Collectors.toList()));
            }
            default -> throw new IllegalArgumentException("Invalid message type: " + this.getMessageType());
        }
       
    }

    public List<OpenAiToolCall> getToolCalls() {
        return toolCalls;
    }

    public void addToolCall(String name, String arguments, String id) {
        this.toolCalls.add(new OpenAiToolCall(name, arguments, id, this));
    }

    public void addToolCall(OpenAiToolCall toolCall) {
        this.toolCalls.add(toolCall);
    }

    public void setToolCalls(List<OpenAiToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public List<OpenAiToolResponse> getToolResponses() {
        return toolResponses;
    }

    public void addToolResponse(String name, String responseData, String id) {
        this.toolResponses.add(new OpenAiToolResponse(name, responseData, id, this));
    }

    public void addToolResponse(OpenAiToolResponse toolResponse) {
        this.toolResponses.add(toolResponse);
    }

    public void setToolResponses(List<OpenAiToolResponse> toolResponses) {
        this.toolResponses = toolResponses;
    }
}