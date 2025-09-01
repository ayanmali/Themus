package com.delphi.delphi.dtos;

import java.util.List;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.utils.enums.AssessmentStatus;

/**
 * Sent to client when they are previewing an assessment (i.e. before beginning an attempt)
 */
public class PreviewAssessmentDto {
    private Long id;
    private String name;
    private String employerName;
    private String description;
    private String details;
    private String role;
    private AssessmentStatus status;
    // private LocalDateTime startDate;
    // private LocalDateTime endDate;
    private Integer duration;
    // private String githubRepositoryLink;
    // private LocalDateTime createdDate;
    // private LocalDateTime updatedDate;
    private List<String> languageOptions;
    //public List<String> skills;

    public PreviewAssessmentDto(Assessment assessment) {
        this.id = assessment.getId();
        this.employerName = assessment.getUser().getOrganizationName();
        this.name = assessment.getName();
        this.description = assessment.getDescription();
        this.details = assessment.getDetails();
        this.role = assessment.getRole();
        this.duration = assessment.getDuration();
        this.languageOptions = assessment.getLanguageOptions();
        this.status = assessment.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
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
    
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<String> getLanguageOptions() {
        return languageOptions;
    }

    public void setLanguageOptions(List<String> languageOptions) {
        this.languageOptions = languageOptions;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }
}
