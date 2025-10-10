package com.delphi.delphi.dtos.messaging.chat;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishAssessmentCreationJobDto implements Serializable {
    private static final long serialVersionUID = 1L;
    // private String userPromptTemplate;
    private UUID jobId;
    private Map<String, Object> userPromptVariables;
    private String model;
    // To store chat messages into the assessment's chat history
    private Long assessmentId;
    // For making calls to the github api
    private String githubRepoName;
    private String baseRepoUrl;
    private String encryptedGithubToken;
    private String githubUsername;
    // Constructors
    public PublishAssessmentCreationJobDto() {}

    // For simple user message
    public PublishAssessmentCreationJobDto(UUID jobId, AssessmentCacheDto assessment, UserCacheDto user, String model) {
        this.jobId = jobId;
        this.userPromptVariables = Map.of(
            "ROLE", assessment.getRole(), 
            "DURATION", String.format("%d minutes", assessment.getDuration()), 
            "SKILLS", assessment.getSkills() != null ? String.join(", ", assessment.getSkills()) : "", 
            "DETAILS", assessment.getDetails() != null ? assessment.getDetails() : "");

        if (assessment.getBaseRepoUrl() != null) userPromptVariables.put("BASE_REPO_URL", assessment.getBaseRepoUrl());
        this.model = model;
        this.assessmentId = assessment.getId();
        this.githubRepoName = assessment.getGithubRepoName();
        this.baseRepoUrl = assessment.getBaseRepoUrl();
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

    public Map<String, Object> getUserPromptVariables() {
        return userPromptVariables;
    }

    public void setUserPromptVariables(Map<String, Object> userPromptVariables) {
        this.userPromptVariables = userPromptVariables;
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

    public String getBaseRepoUrl() {
        return baseRepoUrl;
    }

    public void setBaseRepoUrl(String baseRepoUrl) {
        this.baseRepoUrl = baseRepoUrl;
    }
}