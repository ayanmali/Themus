package com.delphi.delphi.specifications;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.enums.AttemptStatus;

@Component
public class CandidateAttemptSpecifications {
    public static Specification<CandidateAttempt> belongsToUser(Long userId) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<CandidateAttempt> hasCandidateId(Long candidateId) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("candidate").get("id"), candidateId);
        };
    }

    public static Specification<CandidateAttempt> hasAssessmentId(Long assessmentId) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("assessment").get("id"), assessmentId);
        };
    }

    public static Specification<CandidateAttempt> hasStatus(AttemptStatus status) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<CandidateAttempt> hasAnyStatus(List<AttemptStatus> statuses) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.in(root.get("status")).value(statuses);
        };
    }

    public static Specification<CandidateAttempt> hasLanguageChoice(String languageChoice) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("languageChoice"), languageChoice);
        };
    }

    public static Specification<CandidateAttempt> startedAfter(LocalDateTime startedAfter) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.greaterThan(root.get("startedDate"), startedAfter);
        };
    }

    public static Specification<CandidateAttempt> startedBefore(LocalDateTime startedBefore) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("startedDate"), startedBefore);
        };
    }

    public static Specification<CandidateAttempt> completedAfter(LocalDateTime completedAfter) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.greaterThan(root.get("completedDate"), completedAfter);
        };
    }

    public static Specification<CandidateAttempt> completedBefore(LocalDateTime completedBefore) {
        return (root, _, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("completedDate"), completedBefore);
        };
    }
}
