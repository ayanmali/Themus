package com.delphi.delphi.utils;

import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;

public class OpenAiToolCall {
    private String toolName;
    private String arguments;
    private String id;

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
    
}
