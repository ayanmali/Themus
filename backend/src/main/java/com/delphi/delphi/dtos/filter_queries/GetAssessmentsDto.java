package com.delphi.delphi.dtos.filter_queries;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.delphi.delphi.utils.enums.AssessmentStatus;

/**
 * Contains filter query parameters for getting assessments
 */
public class GetAssessmentsDto {
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdDate";
    private String sortDirection = "desc";
    private AssessmentStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime assessmentStartDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime assessmentEndDate;
    private List<String> skills;
    private List<String> languageOptions;

    public GetAssessmentsDto() {
    }

    public GetAssessmentsDto(int page, int size, String sortBy, String sortDirection, AssessmentStatus status, LocalDateTime createdAfter, LocalDateTime createdBefore, LocalDateTime assessmentStartDate, LocalDateTime assessmentEndDate, List<String> skills, List<String> languageOptions) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.status = status;
        this.createdAfter = createdAfter;
        this.createdBefore = createdBefore;
        this.assessmentStartDate = assessmentStartDate;
        this.assessmentEndDate = assessmentEndDate;
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

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
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

    public LocalDateTime getAssessmentStartDate() {
        return assessmentStartDate;
    }

    public void setAssessmentStartDate(LocalDateTime assessmentStartDate) {
        this.assessmentStartDate = assessmentStartDate;
    }

    public LocalDateTime getAssessmentEndDate() {
        return assessmentEndDate;
    }

    public void setAssessmentEndDate(LocalDateTime assessmentEndDate) {
        this.assessmentEndDate = assessmentEndDate;
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
