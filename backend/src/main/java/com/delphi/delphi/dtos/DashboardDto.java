package com.delphi.delphi.dtos;

import java.util.List;

public class DashboardDto {
    private List<FetchCandidateDto> candidates;
    private List<FetchAssessmentDto> assessments;

    public DashboardDto(List<FetchCandidateDto> candidates, List<FetchAssessmentDto> assessments) {
        this.candidates = candidates;
        this.assessments = assessments;
    }
}
