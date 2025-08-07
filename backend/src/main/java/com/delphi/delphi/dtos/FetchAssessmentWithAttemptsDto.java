package com.delphi.delphi.dtos;

import java.util.List;
import java.util.stream.Collectors;

import com.delphi.delphi.entities.Assessment;

public class FetchAssessmentWithAttemptsDto extends FetchAssessmentDto {
    private List<FetchCandidateAttemptDto> candidateAttemptDtos;

    public FetchAssessmentWithAttemptsDto(Assessment assessment) {
        super(assessment);
        this.candidateAttemptDtos = assessment.getCandidateAttempts().stream().map(FetchCandidateAttemptDto::new).collect(Collectors.toList());
    }

    public List<FetchCandidateAttemptDto> getCandidateAttemptDtos() {
        return candidateAttemptDtos;
    }

    public void setCandidateAttemptDtos(List<FetchCandidateAttemptDto> candidateAttemptDtos) {
        this.candidateAttemptDtos = candidateAttemptDtos;
    }
}
