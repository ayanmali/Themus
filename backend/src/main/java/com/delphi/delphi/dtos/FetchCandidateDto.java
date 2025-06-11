package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.Map;

import com.delphi.delphi.entities.Candidate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchCandidateDto {
    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Map<String, String> metadata;
    private Long userId;

    public FetchCandidateDto(Candidate candidate) {
        this.id = candidate.getId();
        this.fullName = candidate.getFullName();
        this.email = candidate.getEmail();
        this.createdDate = candidate.getCreatedDate();
        this.updatedDate = candidate.getUpdatedDate();
        this.metadata = candidate.getMetadata();
        this.userId = candidate.getUser().getId();
    }
}