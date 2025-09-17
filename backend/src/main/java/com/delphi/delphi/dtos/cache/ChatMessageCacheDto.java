package com.delphi.delphi.dtos.cache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

import com.delphi.delphi.dtos.FetchToolCallDto;
import com.delphi.delphi.dtos.FetchToolResponseDto;
import com.delphi.delphi.entities.ChatMessage;

public class ChatMessageCacheDto {
    private Long id;
    private String text;
    private String model;
    private LocalDateTime createdDate;
    private Long assessmentId;
    private List<FetchToolCallDto> toolCalls;
    private List<FetchToolResponseDto> toolResponses;
    private MessageType messageType;

    public ChatMessageCacheDto() {
    }

    public ChatMessageCacheDto(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.text = chatMessage.getText();
        this.model = chatMessage.getModel();
        this.createdDate = chatMessage.getCreatedAt();
        this.assessmentId = chatMessage.getAssessmentId();
        this.messageType = chatMessage.getMessageType();
        if (chatMessage.getToolCalls() != null) {
            this.toolCalls = chatMessage.getToolCalls().stream().map(FetchToolCallDto::new).collect(Collectors.toList());
        }
        if (chatMessage.getToolResponses() != null) {
            this.toolResponses = chatMessage.getToolResponses().stream().map(FetchToolResponseDto::new).collect(Collectors.toList());
        }
    }

    public Message toMessage() {
        switch (this.getMessageType()) {
            case USER -> {
                return new UserMessage(this.getText());
            }
            case ASSISTANT -> {
                // preventing null pointer exceptions
                if (this.toolCalls != null && !this.toolCalls.isEmpty()) {
                    return new AssistantMessage(
                        this.getText(), 
                        Map.of(), 
                        this.getToolCalls().stream().map(FetchToolCallDto::toToolCall).collect(Collectors.toList()));
                } else {
                    return new AssistantMessage(this.getText(), Map.of());
                }
            }
            case SYSTEM -> {
                return new SystemMessage(this.getText());
            }
            case TOOL -> {
                // For tool response messages, return a basic message
                // We'll handle tool responses separately in the ChatService
                return new ToolResponseMessage(
                    this.getToolResponses().stream().map(FetchToolResponseDto::toToolResponse).collect(Collectors.toList())
                );
            }
            default -> throw new IllegalArgumentException("Invalid message type: " + this.getMessageType());
        }
       
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    public Long getAssessmentId() {
        return assessmentId;
    }
    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }
    public List<FetchToolCallDto> getToolCalls() {
        return toolCalls;
    }
    public void setToolCalls(List<FetchToolCallDto> toolCalls) {
        this.toolCalls = toolCalls;
    }
    public List<FetchToolResponseDto> getToolResponses() {
        return toolResponses;
    }
    public void setToolResponses(List<FetchToolResponseDto> toolResponses) {
        this.toolResponses = toolResponses;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    
}
