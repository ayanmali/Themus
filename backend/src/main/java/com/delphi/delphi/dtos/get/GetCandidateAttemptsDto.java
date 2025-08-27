package com.delphi.delphi.dtos.get;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.delphi.delphi.utils.enums.AttemptStatus;

/**
 * Contains filter query parameters for getting candidate attempts
 */
public class GetCandidateAttemptsDto {
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";
    private Long candidateId;
    private Long assessmentId;
    private List<AttemptStatus> attemptStatuses;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime completedAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime completedBefore;

    public GetCandidateAttemptsDto() {
    }

    public GetCandidateAttemptsDto(int page, int size, String sortBy, String sortDirection, Long candidateId, Long assessmentId, List<AttemptStatus> attemptStatuses, LocalDateTime startedAfter, LocalDateTime startedBefore, LocalDateTime completedAfter, LocalDateTime completedBefore) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.candidateId = candidateId;
        this.assessmentId = assessmentId;
        this.attemptStatuses = attemptStatuses;
        this.startedAfter = startedAfter;
        this.startedBefore = startedBefore;
        this.completedAfter = completedAfter;
        this.completedBefore = completedBefore;
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

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
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

    public LocalDateTime getStartedAfter() {
        return startedAfter;
    }

    public void setStartedAfter(LocalDateTime startedAfter) {
        this.startedAfter = startedAfter;
    }

    public LocalDateTime getStartedBefore() {
        return startedBefore;
    }

    public void setStartedBefore(LocalDateTime startedBefore) {
        this.startedBefore = startedBefore;
    }

    public LocalDateTime getCompletedAfter() {
        return completedAfter;
    }

    public void setCompletedAfter(LocalDateTime completedAfter) {
        this.completedAfter = completedAfter;
    }

    public LocalDateTime getCompletedBefore() {
        return completedBefore;
    }

    public void setCompletedBefore(LocalDateTime completedBefore) {
        this.completedBefore = completedBefore;
    }

    
}
