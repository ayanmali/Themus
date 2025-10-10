package com.delphi.delphi.dtos.cache;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.utils.enums.AssessmentStatus;

public class AssessmentCacheDto {
    private Long id;
    private String name;
    private String description;
    private String details;
    private String rules;
    private String instructions;
    private String role;
    private AssessmentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer duration;
    private String githubRepositoryLink;
    private String githubRepoName;
    private String baseRepoUrl;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long userId;
    private List<String> skills;
    private List<String> languageOptions;
    private Map<String, String> metadata;
    private List<Long> candidateAttemptIds;
    private List<Long> candidateIds;
    private List<Long> chatMessageIds;

    // Default constructor for JSON deserialization
    public AssessmentCacheDto() {
    }

    public AssessmentCacheDto(Assessment assessment) {
        this.id = assessment.getId();
        this.name = assessment.getName();
        this.description = assessment.getDescription();
        this.details = assessment.getDetails();
        this.rules = assessment.getRules();
        this.instructions = assessment.getInstructions();
        this.role = assessment.getRole();
        this.status = assessment.getStatus();
        this.startDate = assessment.getStartDate();
        this.endDate = assessment.getEndDate();
        this.duration = assessment.getDuration();
        this.githubRepositoryLink = assessment.getGithubRepositoryLink();
        this.githubRepoName = assessment.getGithubRepoName();
        this.baseRepoUrl = assessment.getBaseRepoUrl();
        this.createdDate = assessment.getCreatedDate();
        this.updatedDate = assessment.getUpdatedDate();
        this.userId = assessment.getUser().getId();
        this.skills = assessment.getSkills() != null ? new ArrayList<>(assessment.getSkills()) : null;
        this.languageOptions = assessment.getLanguageOptions() != null ? new ArrayList<>(assessment.getLanguageOptions()) : null;
        this.metadata = assessment.getMetadata() != null ? new HashMap<>(assessment.getMetadata()) : null;
        if (assessment.getCandidateAttempts() != null) {
            this.candidateAttemptIds = assessment.getCandidateAttempts().stream().map(CandidateAttempt::getId).collect(Collectors.toList());
        }
        if (assessment.getCandidates() != null) {
            this.candidateIds = assessment.getCandidates().stream().map(Candidate::getId).collect(Collectors.toList());
        }
        if (assessment.getChatMessages() != null) {
            this.chatMessageIds = assessment.getChatMessages().stream().map(ChatMessage::getId).collect(Collectors.toList());
        }
    }

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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public AssessmentStatus getStatus() {
        return status;
    }
    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    public String getGithubRepositoryLink() {
        return githubRepositoryLink;
    }
    public void setGithubRepositoryLink(String githubRepositoryLink) {
        this.githubRepositoryLink = githubRepositoryLink;
    }
    public String getGithubRepoName() {
        return githubRepoName;
    }
    public void setGithubRepoName(String githubRepoName) {
        this.githubRepoName = githubRepoName;
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
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public List<String> getSkills() {
        return skills;
    }
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
    public List<String> getLanguageOptions() {
        return languageOptions;
    }
    public void setLanguageOptions(List<String> languageOptions) {
        this.languageOptions = languageOptions;
    }
    public Map<String, String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    public List<Long> getCandidateAttemptIds() {
        return candidateAttemptIds;
    }
    public void setCandidateAttemptIds(List<Long> candidateAttemptIds) {
        this.candidateAttemptIds = candidateAttemptIds;
    }
    public List<Long> getCandidateIds() {
        return candidateIds;
    }
    public void setCandidateIds(List<Long> candidateIds) {
        this.candidateIds = candidateIds;
    }
    public List<Long> getChatMessageIds() {
        return chatMessageIds;
    }
    public void setChatMessageIds(List<Long> chatMessageIds) {
        this.chatMessageIds = chatMessageIds;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getBaseRepoUrl() {
        return baseRepoUrl;
    }

    public void setBaseRepoUrl(String baseRepoUrl) {
        this.baseRepoUrl = baseRepoUrl;
    }

    
}
