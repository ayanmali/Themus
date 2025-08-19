package com.delphi.delphi.dtos.messaging.chat;

import java.io.Serializable;
import java.util.UUID;

import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;

public class PublishChatJobDto implements Serializable {
    private static final long serialVersionUID = 1L;
    // private String userPromptTemplate;
    private UUID jobId;
    private String messageText;
    private String model;
    private Long assessmentId;
    private String githubRepoName;
    private String encryptedGithubToken;
    private String githubUsername;
    // Constructors
    public PublishChatJobDto() {}

    // For simple user message
    public PublishChatJobDto(UUID jobId, String messageText, AssessmentCacheDto assessment, UserCacheDto user, String model) {
        this.jobId = jobId;
        this.messageText = messageText;
        this.model = model;
        // To store chat messages into the assessment's chat history
        this.assessmentId = assessment.getId();
        // For making calls to the github api
        this.githubRepoName = assessment.getGithubRepoName();
        this.encryptedGithubToken = user.getGithubAccessToken();
        this.githubUsername = user.getGithubUsername();
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

    public String getGithubRepoName() {
        return githubRepoName;
    }

    public void setGithubRepoName(String githubRepoName) {
        this.githubRepoName = githubRepoName;
    }

    public String getEncryptedGithubToken() {
        return encryptedGithubToken;
    }

    public void setEncryptedGithubToken(String encryptedGithubToken) {
        this.encryptedGithubToken = encryptedGithubToken;
    }
    
    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }
}
