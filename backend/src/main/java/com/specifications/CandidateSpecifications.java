package com.specifications;

import java.time.LocalDateTime;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.AttemptStatus;

import jakarta.persistence.criteria.JoinType;

// Specifications class
@Component
public class CandidateSpecifications {

    public static Specification<Candidate> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Candidate> hasAssessmentId(Long assessmentId) {
        return (root, query, criteriaBuilder) -> {
            if (assessmentId == null) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Candidate, Assessment> assessmentJoin = root.join("assessments", JoinType.LEFT);
            query.distinct(true);
            return criteriaBuilder.equal(assessmentJoin.get("id"), assessmentId);
        };
    }

    public static Specification<Candidate> hasAttemptStatus(AttemptStatus attemptStatus) {
        return (root, query, criteriaBuilder) -> {
            if (attemptStatus == null) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Candidate, CandidateAttempt> attemptJoin = root.join("candidateAttempts", JoinType.LEFT);
            query.distinct(true);
            return criteriaBuilder.equal(attemptJoin.get("status"), attemptStatus);
        };
    }

    public static Specification<Candidate> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), createdAfter);
        };
    }

    public static Specification<Candidate> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), createdBefore);
        };
    }

    public static Specification<Candidate> attemptCompletedAfter(LocalDateTime completedAfter) {
        return (root, query, criteriaBuilder) -> {
            if (completedAfter == null) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Candidate, CandidateAttempt> attemptJoin = root.join("candidateAttempts", JoinType.LEFT);
            query.distinct(true);
            return criteriaBuilder.greaterThanOrEqualTo(attemptJoin.get("completedDate"), completedAfter);
        };
    }

    public static Specification<Candidate> attemptCompletedBefore(LocalDateTime completedBefore) {
        return (root, query, criteriaBuilder) -> {
            if (completedBefore == null) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Candidate, CandidateAttempt> attemptJoin = root.join("candidateAttempts", JoinType.LEFT);
            query.distinct(true);
            return criteriaBuilder.lessThanOrEqualTo(attemptJoin.get("completedDate"), completedBefore);
        };
    }
}

