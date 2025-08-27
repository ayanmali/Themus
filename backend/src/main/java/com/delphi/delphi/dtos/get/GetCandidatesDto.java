package com.delphi.delphi.dtos.get;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.delphi.delphi.utils.enums.AttemptStatus;

/**
 * Contains filter query parameters for getting candidates
 */
public class GetCandidatesDto {
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";
    private Long assessmentId;
    private List<AttemptStatus> attemptStatuses;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;
    private List<String> skills;
    private List<String> languageOptions;

    public GetCandidatesDto() {
    }

    public GetCandidatesDto(int page, int size, String sortBy, String sortDirection, Long assessmentId, List<AttemptStatus> attemptStatuses, LocalDateTime createdAfter, LocalDateTime createdBefore, List<String> skills, List<String> languageOptions) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.assessmentId = assessmentId;
        this.attemptStatuses = attemptStatuses;
        this.createdAfter = createdAfter;
        this.createdBefore = createdBefore;
        this.skills = skills;
        this.languageOptions = languageOptions;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<AttemptStatus> getAttemptStatuses() {
        return attemptStatuses;
    }

    public void setAttemptStatuses(List<AttemptStatus> attemptStatuses) {
        this.attemptStatuses = attemptStatuses;
    }

    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
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


    
}
