package com.delphi.delphi.dtos.messaging.chat;

import java.io.Serializable;
import java.util.UUID;

public class PublishChatJobDto implements Serializable {
    private static final long serialVersionUID = 1L;
    // private String userPromptTemplate;
    private UUID jobId;
    private String messageText;
    private String model;
    private String githubRepoUrl;

    // Constructors
    public PublishChatJobDto() {}

    // For simple user message
    public PublishChatJobDto(UUID jobId, String messageText, String model, String githubRepoUrl) {
        this.jobId = jobId;
        this.messageText = messageText;
        this.model = model;
        this.githubRepoUrl = githubRepoUrl;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getGithubRepoUrl() {
        return githubRepoUrl;
    }

    public void setGithubRepoUrl(String githubRepoUrl) {
        this.githubRepoUrl = githubRepoUrl;
    }

    
    
}
