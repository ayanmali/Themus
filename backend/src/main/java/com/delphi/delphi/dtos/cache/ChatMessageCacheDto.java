package com.delphi.delphi.dtos.cache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.MessageType;

import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.utils.OpenAiToolCall;

public class ChatMessageCacheDto {
    private Long id;
    private String text;
    private String model;
    private LocalDateTime createdDate;
    private Long assessmentId;
    private List<String> toolCallIds;
    private MessageType messageType;

    public ChatMessageCacheDto(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.text = chatMessage.getText();
        this.model = chatMessage.getModel();
        this.createdDate = chatMessage.getCreatedAt();
        this.assessmentId = chatMessage.getAssessment().getId();
        this.messageType = chatMessage.getMessageType();
        if (chatMessage.getToolCalls() != null) {
            this.toolCallIds = chatMessage.getToolCalls().stream().map(OpenAiToolCall::getId).collect(Collectors.toList());
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
    public List<String> getToolCallIds() {
        return toolCallIds;
    }
    public void setToolCallIds(List<String> toolCallIds) {
        this.toolCallIds = toolCallIds;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    
}
