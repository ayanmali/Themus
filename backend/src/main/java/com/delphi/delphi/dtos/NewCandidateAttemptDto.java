package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.delphi.delphi.utils.AttemptStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @AllArgsConstructor
public class NewCandidateAttemptDto {
    private String githubRepositoryLink;
    private String languageChoice;
    private Long candidateId;
    private Long assessmentId;
    private AttemptStatus status;
    private LocalDateTime startedDate;

    public NewCandidateAttemptDto(String githubRepositoryLink, String languageChoice, Long candidateId, Long assessmentId) {
        this.githubRepositoryLink = githubRepositoryLink;
        this.languageChoice = languageChoice;
        this.candidateId = candidateId;
        this.assessmentId = assessmentId;
        this.status = AttemptStatus.STARTED;
        this.startedDate = LocalDateTime.now();
    }
}
