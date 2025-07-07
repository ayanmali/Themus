package com.delphi.delphi.utils;

import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;

import com.delphi.delphi.entities.ChatMessage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OpenAiToolCall {
    @Id
    private String id;

    @Column(name = "tool_name")
    private String toolName;

    @Column(name = "arguments")
    private String arguments;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private ChatMessage chatMessage;

    public OpenAiToolCall() {
    }

    public OpenAiToolCall(String toolName, String arguments, String id) {
        this.toolName = toolName;
        this.arguments = arguments;
        this.id = id;
    }

    public OpenAiToolCall(ToolCall toolCall) {
        this.toolName = toolCall.name();
        this.arguments = toolCall.arguments();
        this.id = toolCall.id();
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
    
}
