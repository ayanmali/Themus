package com.delphi.delphi.dtos.cache;

import java.time.LocalDateTime;
import java.util.List;

import com.delphi.delphi.utils.git.GithubAccountType;

public class UserCacheDto {
    private Long id;
    private String name;
    private String email;
    private String encryptedPassword;
    private String organizationName;
    private String githubAccessToken;
    private String githubUsername;
    private GithubAccountType githubAccountType;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<Long> assessmentIds;
    private List<Long> candidateIds;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEncryptedPassword() {
        return encryptedPassword;
    }
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    public String getGithubAccessToken() {
        return githubAccessToken;
    }
    public void setGithubAccessToken(String githubAccessToken) {
        this.githubAccessToken = githubAccessToken;
    }
    public String getGithubUsername() {
        return githubUsername;
    }
    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }
    public GithubAccountType getGithubAccountType() {
        return githubAccountType;
    }
    public void setGithubAccountType(GithubAccountType githubAccountType) {
        this.githubAccountType = githubAccountType;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    public List<Long> getAssessmentIds() {
        return assessmentIds;
    }
    public void setAssessmentIds(List<Long> assessmentIds) {
        this.assessmentIds = assessmentIds;
    }
    public List<Long> getCandidateIds() {
        return candidateIds;
    }
    public void setCandidateIds(List<Long> candidateIds) {
        this.candidateIds = candidateIds;
    }

    
}
