package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.utils.AssessmentStatus;
import com.delphi.delphi.utils.AssessmentType;

import lombok.Data;

@Data
public class FetchAssessmentDto {
    private Long id;
    private String name;
    private String description;
    private String roleName;
    private AssessmentStatus status;
    private AssessmentType assessmentType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer duration;
    private String githubRepositoryLink;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long userId;
    private List<String> skills;
    private List<String> languageOptions;
    private Map<String, String> metadata;

    public FetchAssessmentDto(Assessment assessment) {
        this.id = assessment.getId();
        this.name = assessment.getName();
        this.description = assessment.getDescription();
        this.roleName = assessment.getRoleName();
        this.status = assessment.getStatus();
        this.assessmentType = assessment.getAssessmentType();
        this.startDate = assessment.getStartDate();
        this.endDate = assessment.getEndDate();
        this.duration = assessment.getDuration();
        this.githubRepositoryLink = assessment.getGithubRepositoryLink();
        this.createdDate = assessment.getCreatedDate();
        this.updatedDate = assessment.getUpdatedDate();
        this.userId = assessment.getUser().getId();
        this.skills = assessment.getSkills();
        this.languageOptions = assessment.getLanguageOptions();
        this.metadata = assessment.getMetadata();
    }
}