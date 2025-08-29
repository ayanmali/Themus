package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.utils.enums.AttemptStatus;

public class FetchCandidateDto {
    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Map<String, String> metadata;
    private Map<AttemptStatus, List<Long>> assessmentStatuses; // maps attempt status to list of assessment ids

    public FetchCandidateDto() {
    }

    public FetchCandidateDto(Candidate candidate) {
        this.id = candidate.getId();
        this.fullName = candidate.getFullName();
        this.email = candidate.getEmail();
        this.createdDate = candidate.getCreatedDate();
        this.updatedDate = candidate.getUpdatedDate();
        this.metadata = candidate.getMetadata() != null ? new HashMap<>(candidate.getMetadata()) : null;
        // Note: For Candidate entity, we would need to populate attemptStatuses manually
        // This constructor is kept for backward compatibility
    }

    public FetchCandidateDto(CandidateCacheDto candidate) {
        this.id = candidate.getId();
        this.fullName = candidate.getFirstName() + " " + candidate.getLastName();
        this.email = candidate.getEmail();
        this.createdDate = candidate.getCreatedDate();
        this.updatedDate = candidate.getUpdatedDate();
        this.metadata = candidate.getMetadata() != null ? new HashMap<>(candidate.getMetadata()) : null;
        this.assessmentStatuses = candidate.getAssessmentStatuses() != null ? new HashMap<>(candidate.getAssessmentStatuses()) : null;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<AttemptStatus, List<Long>> getAssessmentStatuses() {
        return assessmentStatuses;
    }

    public void setAssessmentStatuses(Map<AttemptStatus, List<Long>> assessmentStatuses) {
        this.assessmentStatuses = assessmentStatuses;
    }
    
}