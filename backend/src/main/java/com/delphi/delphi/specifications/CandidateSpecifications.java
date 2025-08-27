package com.delphi.delphi.specifications;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.enums.AttemptStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

// Specifications class
@Component
public class CandidateSpecifications {

    public static Specification<Candidate> belongsToUser(Long userId) {
        return (root, _, criteriaBuilder) -> 
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

    public static Specification<Candidate> notInAssessment(Long assessmentId) {
        return (root, query, criteriaBuilder) -> {
            if (assessmentId == null) {
                return criteriaBuilder.conjunction();
            }
            
            // Create a subquery to find candidates who are in the specific assessment
            var subquery = query.subquery(Candidate.class);
            var subRoot = subquery.from(Candidate.class);
            var subJoin = subRoot.join("assessments", JoinType.INNER);
            
            subquery.select(subRoot).where(criteriaBuilder.equal(subJoin.get("id"), assessmentId));
            
            // Return candidates who are NOT in the subquery result
            return criteriaBuilder.not(root.in(subquery));
        };
    }

    /**
     * Alternative implementation using EXISTS subquery (better performance for large datasets)
     * 
     * @param statuses List of AttemptStatus values to filter by
     * @return Specification for filtering candidates
     */
    public static Specification<Candidate> hasAnyAttemptStatus(List<AttemptStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            // Create subquery
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<CandidateAttempt> attemptRoot = subquery.from(CandidateAttempt.class);
            
            subquery.select(attemptRoot.get("id"))
                   .where(
                       criteriaBuilder.and(
                           criteriaBuilder.equal(attemptRoot.get("candidate"), root),
                           attemptRoot.get("status").in(statuses)
                       )
                   );
            
            return criteriaBuilder.exists(subquery);
        };
    }

    /**
     * Filter candidates that have ALL of the provided attempt statuses
     * (i.e., candidate must have at least one attempt for each status in the list)
     * 
     * @param statuses List of AttemptStatus values - candidate must have attempts with ALL these statuses
     * @return Specification for filtering candidates
     */
    public static Specification<Candidate> hasAllAttemptStatuses(List<AttemptStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            Predicate[] predicates = statuses.stream()
                .map(status -> {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<CandidateAttempt> attemptRoot = subquery.from(CandidateAttempt.class);
                    
                    subquery.select(attemptRoot.get("id"))
                           .where(
                               criteriaBuilder.and(
                                   criteriaBuilder.equal(attemptRoot.get("candidate"), root),
                                   criteriaBuilder.equal(attemptRoot.get("status"), status)
                               )
                           );
                    
                    return criteriaBuilder.exists(subquery);
                })
                .toArray(Predicate[]::new);
            
            return criteriaBuilder.and(predicates);
        };
    }

    /**
     * Filter candidates that have ONLY the provided attempt statuses
     * (no attempts with statuses outside the provided list)
     * 
     * @param statuses List of allowed AttemptStatus values
     * @return Specification for filtering candidates
     */
    public static Specification<Candidate> hasOnlyAttemptStatuses(List<AttemptStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                // If no statuses provided, find candidates with no attempts
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<CandidateAttempt> attemptRoot = subquery.from(CandidateAttempt.class);
                
                subquery.select(attemptRoot.get("id"))
                       .where(criteriaBuilder.equal(attemptRoot.get("candidate"), root));
                
                return criteriaBuilder.not(criteriaBuilder.exists(subquery));
            }
            
            // Create subquery to check for attempts with statuses NOT in the provided list
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<CandidateAttempt> attemptRoot = subquery.from(CandidateAttempt.class);
            
            subquery.select(attemptRoot.get("id"))
                   .where(
                       criteriaBuilder.and(
                           criteriaBuilder.equal(attemptRoot.get("candidate"), root),
                           criteriaBuilder.not(attemptRoot.get("status").in(statuses))
                       )
                   );
            
            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }

    /**
     * Filter candidates by a single attempt status (convenience method)
     * 
     * @param status Single AttemptStatus to filter by
     * @return Specification for filtering candidates
     */
    public static Specification<Candidate> hasAttemptStatus(AttemptStatus status) {
        return hasAnyAttemptStatus(status != null ? List.of(status) : null);
    }

    /**
     * Filter candidates that do NOT have any attempts with the provided statuses
     * 
     * @param statuses List of AttemptStatus values to exclude
     * @return Specification for filtering candidates
     */
    public static Specification<Candidate> doesNotHaveAttemptStatuses(List<AttemptStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<CandidateAttempt> attemptRoot = subquery.from(CandidateAttempt.class);
            
            subquery.select(attemptRoot.get("id"))
                   .where(
                       criteriaBuilder.and(
                           criteriaBuilder.equal(attemptRoot.get("candidate"), root),
                           attemptRoot.get("status").in(statuses)
                       )
                   );
            
            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }

    public static Specification<Candidate> createdAfter(LocalDateTime createdAfter) {
        return (root, _, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), createdAfter);
        };
    }

    public static Specification<Candidate> createdBefore(LocalDateTime createdBefore) {
        return (root, _, criteriaBuilder) -> {
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

