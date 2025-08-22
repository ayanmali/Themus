package com.delphi.delphi.entities;

import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents the result (output) of a tool call.
 * A ToolResponseMessage is created when the toolCallingManager has executed a tool call.
 * A ToolResponseMessage contains a List of ToolResponse objects.
 */
@Entity
@Table(name = "openai_tool_responses")
public class OpenAiToolResponse {
    @Id
    private String id;

    @Column(name = "name")
    private String name;   

    @Column(name = "response_data")
    private String responseData;

    @ManyToOne
    @JoinColumn(name = "message_id")
    @JsonIgnore
    private ChatMessage chatMessage;

    public OpenAiToolResponse() {
    }

    public OpenAiToolResponse(String name, String responseData, String id, ChatMessage chatMessage) {
        this.name = name;
        this.responseData = responseData;
        this.id = id;
        this.chatMessage = chatMessage;
    }

    public OpenAiToolResponse(ToolResponse toolResponse, ChatMessage chatMessage) {
        this.name = toolResponse.name();
        this.responseData = toolResponse.responseData();
        this.id = toolResponse.id();
        this.chatMessage = chatMessage;
    }

    public ToolResponse toToolResponse() {
        return new ToolResponse(this.id, this.name, this.responseData);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
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
