package com.delphi.delphi.dtos.messaging.chat;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import com.delphi.delphi.dtos.NewAssessmentDto;

public class PublishAssessmentCreationJobDto implements Serializable {
    private static final long serialVersionUID = 1L;
    // private String userPromptTemplate;
    private UUID jobId;
    private Map<String, Object> userPromptVariables;
    private String model;
    private String githubRepoUrl;

    // Constructors
    public PublishAssessmentCreationJobDto() {}

    // For simple user message
    public PublishAssessmentCreationJobDto(UUID jobId, NewAssessmentDto newAssessmentDto, String githubRepoUrl) {
        this.jobId = jobId;
        this.userPromptVariables = Map.of("ROLE", newAssessmentDto.getRole(), "DURATION", newAssessmentDto.getDuration(), "SKILLS", newAssessmentDto.getSkills(), "LANGUAGE_OPTIONS", newAssessmentDto.getLanguageOptions(), "OTHER_DETAILS", newAssessmentDto.getDetails());
        this.model = newAssessmentDto.getModel();
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

    public String getGithubRepoUrl() {
        return githubRepoUrl;
    }

    public void setGithubRepoUrl(String githubRepoUrl) {
        this.githubRepoUrl = githubRepoUrl;
    }

    
    
}