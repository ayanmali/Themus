package com.delphi.delphi.dtos;

public class NewEvaluationDto {
    private Long candidateAttemptId;

    public NewEvaluationDto() {
    }

    public NewEvaluationDto(Long candidateAttemptId) {
        this.candidateAttemptId = candidateAttemptId;
    }

    public Long getCandidateAttemptId() {
        return candidateAttemptId;
    }

    public void setCandidateAttemptId(Long candidateAttemptId) {
        this.candidateAttemptId = candidateAttemptId;
    }
}
