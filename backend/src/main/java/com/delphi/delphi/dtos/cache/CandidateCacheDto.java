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
import com.delphi.delphi.utils.AttemptStatus;

public class CandidateCacheDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long userId;
    private List<Long> assessmentIds;
    private List<Long> candidateAttemptIds;
    private Map<AttemptStatus, List<Long>> attemptStatuses; // maps attempt status to list of candidate attempt ids
    private Map<String, String> metadata;

    // Default constructor for Jackson deserialization
    public CandidateCacheDto() {
    }

    public CandidateCacheDto(Candidate candidate) {
        this.id = candidate.getId();
        this.firstName = candidate.getFirstName();
        this.lastName = candidate.getLastName();
        this.email = candidate.getEmail();
        this.createdDate = candidate.getCreatedDate();
        this.updatedDate = candidate.getUpdatedDate();
        this.userId = candidate.getUser().getId();
        // this.userId = (candidate.getUser() != null) ? candidate.getUser().getId() : null;
        if (candidate.getAssessments() != null) {
            this.assessmentIds = candidate.getAssessments().stream().map(Assessment::getId).collect(Collectors.toList());
        }
        if (candidate.getCandidateAttempts() != null) {
            this.candidateAttemptIds = candidate.getCandidateAttempts().stream().map(CandidateAttempt::getId).collect(Collectors.toList());
            
            // Populate attemptStatuses map
            this.attemptStatuses = new HashMap<>();
            candidate.getCandidateAttempts().forEach(attempt -> {
                AttemptStatus status = attempt.getStatus();
                if (status != null) {
                    attemptStatuses.computeIfAbsent(status, k -> new ArrayList<>()).add(attempt.getId());
                }
            });
        }
        this.metadata = candidate.getMetadata() != null ? new HashMap<>(candidate.getMetadata()) : null;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public List<Long> getAssessmentIds() {
        return assessmentIds;
    }
    public void setAssessmentIds(List<Long> assessmentIds) {
        this.assessmentIds = assessmentIds;
    }
    public List<Long> getCandidateAttemptIds() {
        return candidateAttemptIds;
    }
    public void setCandidateAttemptIds(List<Long> candidateAttemptIds) {
        this.candidateAttemptIds = candidateAttemptIds;
    }
    public Map<String, String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<AttemptStatus, List<Long>> getAttemptStatuses() {
        return attemptStatuses;
    }

    public void setAttemptStatuses(Map<AttemptStatus, List<Long>> attemptStatuses) {
        this.attemptStatuses = attemptStatuses;
    }

    
}
