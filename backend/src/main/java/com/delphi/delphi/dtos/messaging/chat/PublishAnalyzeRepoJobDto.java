package com.delphi.delphi.dtos.messaging.chat;

import java.util.Map;
import java.util.UUID;

import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;

public class PublishAnalyzeRepoJobDto {
    private UUID jobId;
    private AssessmentCacheDto assessment;
    private UserCacheDto user;
    private String model;
    private String baseRepoUrl;
    private Map<String, Object> userPromptVariables;

    public PublishAnalyzeRepoJobDto() {
    }

    public PublishAnalyzeRepoJobDto(UUID jobId, AssessmentCacheDto assessment, UserCacheDto user, String model, String baseRepoUrl, Map<String, Object> userPromptVariables) {
        this.jobId = jobId;
        this.assessment = assessment;
        this.user = user;
        this.model = model;
        this.baseRepoUrl = baseRepoUrl;
        this.userPromptVariables = userPromptVariables;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public AssessmentCacheDto getAssessment() {
        return assessment;
    }

    public void setAssessment(AssessmentCacheDto assessment) {
        this.assessment = assessment;
    }

    public UserCacheDto getUser() {
        return user;
    }

    public void setUser(UserCacheDto user) {
        this.user = user;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseRepoUrl() {
        return baseRepoUrl;
    }

    public void setBaseRepoUrl(String baseRepoUrl) {
        this.baseRepoUrl = baseRepoUrl;
    }

    public Map<String, Object> getUserPromptVariables() {
        return userPromptVariables;
    }

    public void setUserPromptVariables(Map<String, Object> userPromptVariables) {
        this.userPromptVariables = userPromptVariables;
    }
}