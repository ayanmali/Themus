package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class NewAssessmentDto {
    private String name; // title
    private String description; // description
    private String role; // role
    private LocalDateTime startDate; // start date
    private LocalDateTime endDate;
    private Integer duration; // estimated duration/time limit
    private List<String> skills;
    private List<String> languageOptions;
    private String model; // model to use for the chat completion
    private Map<String, String> metadata;

    public NewAssessmentDto() {
    }

    public NewAssessmentDto(String name, String description, String role, LocalDateTime startDate, LocalDateTime endDate, Integer duration, List<String> skills, List<String> languageOptions, String model, Map<String, String> metadata) {
        this.name = name;
        this.description = description;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.skills = skills;
        this.languageOptions = languageOptions;
        this.model = model;
        this.metadata = metadata;
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
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
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
    public String getModel() {
        return model;
    }   
    public void setModel(String model) {
        this.model = model;
    }

}