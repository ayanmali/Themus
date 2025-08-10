package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.utils.AssessmentStatus;

public class FetchAssessmentDto {
    private Long id;
    private String name;
    private String description;
    private String details;
    private String role;
    private AssessmentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer duration;
    private String githubRepositoryLink;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<String> skills;
    private List<String> languageOptions;
    private Map<String, String> metadata;

    public FetchAssessmentDto(Assessment assessment) {
        this.id = assessment.getId();
        this.name = assessment.getName();
        this.description = assessment.getDescription();
        this.role = assessment.getRole();
        this.status = assessment.getStatus();
        this.startDate = assessment.getStartDate();
        this.endDate = assessment.getEndDate();
        this.duration = assessment.getDuration();
        this.githubRepositoryLink = assessment.getGithubRepositoryLink();
        this.createdDate = assessment.getCreatedDate();
        this.updatedDate = assessment.getUpdatedDate();
        this.skills = assessment.getSkills();
        this.languageOptions = assessment.getLanguageOptions();
        this.metadata = assessment.getMetadata();
    }

    public FetchAssessmentDto(AssessmentCacheDto assessment) {
        this.id = assessment.getId();
        this.name = assessment.getName();
        this.description = assessment.getDescription();
        this.details = assessment.getDetails();
        this.role = assessment.getRole();
        this.status = assessment.getStatus();
        this.startDate = assessment.getStartDate();
        this.endDate = assessment.getEndDate();
        this.duration = assessment.getDuration();
        this.githubRepositoryLink = assessment.getGithubRepositoryLink();
        this.createdDate = assessment.getCreatedDate();
        this.updatedDate = assessment.getUpdatedDate();
        this.skills = assessment.getSkills();
        this.languageOptions = assessment.getLanguageOptions();
        this.metadata = assessment.getMetadata();
    }

    public FetchAssessmentDto() {
    }

    public FetchAssessmentDto(LocalDateTime createdDate, String description, Integer duration, LocalDateTime endDate, String githubRepositoryLink, Long id, List<String> languageOptions, Map<String, String> metadata, String name, String role, List<String> skills, LocalDateTime startDate, AssessmentStatus status, LocalDateTime updatedDate) {
        this.createdDate = createdDate;
        this.description = description;
        this.duration = duration;
        this.endDate = endDate;
        this.githubRepositoryLink = githubRepositoryLink;
        this.id = id;
        this.languageOptions = languageOptions;
        this.metadata = metadata;
        this.name = name;
        this.role = role;
        this.skills = skills;
        this.startDate = startDate;
        this.status = status;
        this.updatedDate = updatedDate;
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

    public void setRoleName(String role) {
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

    
}