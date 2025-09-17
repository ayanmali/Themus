package com.delphi.delphi.dtos;

import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;

import com.delphi.delphi.entities.OpenAiToolCall;

public class FetchToolCallDto {
    private String id;
    private String name;
    private String arguments;

    public FetchToolCallDto() {
    }

    public FetchToolCallDto(String id, String name, String arguments) {
        this.id = id;
        this.name = name;
        this.arguments = arguments;
    }

    public FetchToolCallDto(OpenAiToolCall toolCall) {
        this.id = toolCall.getId();
        this.name = toolCall.getToolName();
        this.arguments = toolCall.getArguments();
    }

    public ToolCall toToolCall() {
        return new ToolCall(this.id, "FUNCTION", this.name, this.arguments);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    
}
