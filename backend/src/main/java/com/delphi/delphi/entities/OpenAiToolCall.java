package com.delphi.delphi.entities;

import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "openai_tool_calls")
/*
 * Represents data about a request to invoke a tool made by an LLM via the OpenAI API.
 */
public class OpenAiToolCall {
    @Id
    private String id;

    @Column(name = "tool_name")
    private String name;    // tool name

    @Column(name = "arguments")
    private String arguments; // JSON string of the arguments

    @ManyToOne
    @JoinColumn(name = "message_id")
    @JsonIgnore
    private ChatMessage chatMessage;

    public OpenAiToolCall() {
    }

    public OpenAiToolCall(String name, String arguments, String id, ChatMessage chatMessage) {
        this.name = name;
        this.arguments = arguments;
        this.id = id;
        this.chatMessage = chatMessage;
    }

    public OpenAiToolCall(ToolCall toolCall, ChatMessage chatMessage) {
        this.name = toolCall.name();
        this.arguments = toolCall.arguments();
        this.id = toolCall.id();
        this.chatMessage = chatMessage;
    }

    public String getToolName() {
        return name;
    }

    public void setToolName(String name) {
        this.name = name;
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
