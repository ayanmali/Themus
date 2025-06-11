package com.delphi.delphi.dtos;

import java.time.LocalDateTime;
import java.util.Map;

import com.delphi.delphi.entities.Evaluation;

import lombok.Data;

@Data
public class FetchEvaluationDto {
    private Long id;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long candidateAttemptId;
    private Map<String, String> metadata;
    // private String evaluationStatus;
    // private String evaluationFeedback;
    // private String evaluationScore;
    // private String evaluationComments;
    // private String evaluationRecommendations;
    // private String evaluationImprovements;

    public FetchEvaluationDto(Evaluation evaluation) {
        this.id = evaluation.getId();
        this.createdDate = evaluation.getCreatedDate();
        this.updatedDate = evaluation.getUpdatedDate();
        this.candidateAttemptId = evaluation.getCandidateAttempt().getId();
        this.metadata = evaluation.getMetadata();
    }
}