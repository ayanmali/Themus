package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.Map;

import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Candidate;

public class FetchCandidateDto {
    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Map<String, String> metadata;

    public FetchCandidateDto() {
    }

    public FetchCandidateDto(Candidate candidate) {
        this.id = candidate.getId();
        this.fullName = candidate.getFullName();
        this.email = candidate.getEmail();
        this.createdDate = candidate.getCreatedDate();
        this.updatedDate = candidate.getUpdatedDate();
        this.metadata = candidate.getMetadata();
    }

    public FetchCandidateDto(CandidateCacheDto candidate) {
        this.id = candidate.getId();
        this.fullName = candidate.getFirstName() + " " + candidate.getLastName();
        this.email = candidate.getEmail();
        this.createdDate = candidate.getCreatedDate();
        this.updatedDate = candidate.getUpdatedDate();
        this.metadata = candidate.getMetadata();
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
    
}