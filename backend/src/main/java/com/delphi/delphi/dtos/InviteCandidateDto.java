package com.delphi.delphi.dtos;

public class InviteCandidateDto {
    private Long candidateId;
    private Long assessmentId;

    public InviteCandidateDto(Long candidateId, Long assessmentId) {
        this.candidateId = candidateId;
        this.assessmentId = assessmentId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }
    
}
