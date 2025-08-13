package com.delphi.delphi.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.utils.AttemptStatus;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

       // Find candidate by email
       Optional<Candidate> findByEmail(String email);

       // Check if email exists
       boolean existsByEmail(String email);

       // Find candidates by user ID with pagination
       // Page<Candidate> findByUserId(Long userId, Pageable pageable);

       // // Find candidates by first name (case-insensitive)
       // Page<Candidate> findByFirstNameContainingIgnoreCase(String firstName,
       // Pageable pageable);

       // // Find candidates by last name (case-insensitive)
       // Page<Candidate> findByLastNameContainingIgnoreCase(String lastName, Pageable
       // pageable);

       // // Find candidates by full name (case-insensitive)
       // @Query("SELECT c FROM Candidate c WHERE LOWER(CONCAT(c.firstName, ' ',
       // c.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
       // Page<Candidate> findByFullNameContainingIgnoreCase(@Param("fullName") String
       // fullName, Pageable pageable);

       // // Find candidates by email domain
       // @Query("SELECT c FROM Candidate c WHERE c.email LIKE CONCAT('%@', :domain)")
       // Page<Candidate> findByEmailDomain(@Param("domain") String domain, Pageable
       // pageable);

       // // Find candidates created within date range
       // @Query("SELECT c FROM Candidate c WHERE c.createdDate BETWEEN :startDate AND
       // :endDate")
       // Page<Candidate> findByCreatedDateBetween(@Param("startDate") LocalDateTime
       // startDate,
       // @Param("endDate") LocalDateTime endDate,
       // Pageable pageable);

       // // Find candidates by assessment ID
       // @Query("SELECT c FROM Candidate c JOIN c.assessments a WHERE a.id =
       // :assessmentId")
       // Page<Candidate> findByAssessmentId(@Param("assessmentId") Long assessmentId,
       // Pageable pageable);

       // // Find candidates with attempts for specific assessment
       // @Query("SELECT DISTINCT c FROM Candidate c JOIN c.candidateAttempts ca WHERE
       // ca.assessment.id = :assessmentId")
       // Page<Candidate>
       // findCandidatesWithAttemptsForAssessment(@Param("assessmentId") Long
       // assessmentId, Pageable pageable);

       // // Find candidates with no attempts
       // @Query("SELECT c FROM Candidate c WHERE c.candidateAttempts IS EMPTY")
       // Page<Candidate> findCandidatesWithNoAttempts(Pageable pageable);

       // // Find candidates by user and assessment
       // @Query("SELECT c FROM Candidate c JOIN c.assessments a WHERE c.user.id =
       // :userId AND a.id = :assessmentId")
       // Page<Candidate> findByUserIdAndAssessmentId(@Param("userId") Long userId,
       // @Param("assessmentId") Long assessmentId,
       // Pageable pageable);

       // Count candidates by user
       Long countByUserId(Long userId);

       // Find candidates with their attempt counts
       @Query("SELECT c, COUNT(ca) as attemptCount FROM Candidate c LEFT JOIN c.candidateAttempts ca GROUP BY c.id")
       Page<Object[]> findCandidatesWithAttemptCount(Pageable pageable);

       // Find candidates with multiple optional filters including attempt-related
       // filters
       // @Query("SELECT DISTINCT c FROM Candidate c " +
       // "LEFT JOIN c.assessments a " +
       // "LEFT JOIN c.candidateAttempts ca " +
       // "WHERE (:assessmentId IS NULL OR a.id = :assessmentId) AND " +
       // "(:attemptStatus IS NULL OR ca.status = :attemptStatus) AND " +
       // "(:emailDomain IS NULL OR c.email LIKE CONCAT('%@', :emailDomain)) AND " +
       // "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName,
       // '%'))) AND " +
       // "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName,
       // '%'))) AND " +
       // "(:createdAfter IS NULL OR c.createdDate >= :createdAfter) AND " +
       // "(:createdBefore IS NULL OR c.createdDate <= :createdBefore) AND " +
       // "(:attemptCompletedAfter IS NULL OR ca.completedDate >=
       // :attemptCompletedAfter) AND " +
       // "(:attemptCompletedBefore IS NULL OR ca.completedDate <=
       // :attemptCompletedBefore)")
       // Page<Candidate> findWithFilters(@Param("assessmentId") Long assessmentId,
       // @Param("attemptStatus") com.delphi.delphi.utils.AttemptStatus attemptStatus,
       // @Param("emailDomain") String emailDomain,
       // @Param("firstName") String firstName,
       // @Param("lastName") String lastName,
       // @Param("createdAfter") LocalDateTime createdAfter,
       // @Param("createdBefore") LocalDateTime createdBefore,
       // @Param("attemptCompletedAfter") LocalDateTime attemptCompletedAfter,
       // @Param("attemptCompletedBefore") LocalDateTime attemptCompletedBefore,
       // Pageable pageable);

       // Find candidates with multiple optional filters for a specific user
       // @Query("SELECT DISTINCT c FROM Candidate c " +
       //               "LEFT JOIN c.assessments a " +
       //               "LEFT JOIN c.candidateAttempts ca " +
       //               "WHERE c.user.id = :userId AND " +
       //               "(:assessmentId IS NULL OR a.id = :assessmentId) AND " +
       //               "(:attemptStatus IS NULL OR ca.status = :attemptStatus) AND " +
       //               "(:createdAfter IS NULL OR c.createdDate >= :createdAfter) AND " +
       //               "(:createdBefore IS NULL OR c.createdDate <= :createdBefore) AND " +
       //               "(:attemptCompletedAfter IS NULL OR ca.completedDate >= :attemptCompletedAfter) AND " +
       //               "(:attemptCompletedBefore IS NULL OR ca.completedDate <= :attemptCompletedBefore)")
       // Page<Candidate> findWithFiltersForUser(@Param("userId") Long userId,
       //               @Param("assessmentId") Long assessmentId,
       //               @Param("attemptStatus") AttemptStatus attemptStatus,
       //               @Param("createdAfter") LocalDateTime createdAfter,
       //               @Param("createdBefore") LocalDateTime createdBefore,
       //               @Param("attemptCompletedAfter") LocalDateTime attemptCompletedAfter,
       //               @Param("attemptCompletedBefore") LocalDateTime attemptCompletedBefore,
       //               Pageable pageable);

       /**
        * Find candidates who have attempts with a specific status for a specific
        * assessment
        */
       @Query("SELECT DISTINCT c FROM Candidate c " +
                     "JOIN c.candidateAttempts ca " +
                     "WHERE ca.assessment.id = :assessmentId " +
                     "AND ca.status = :attemptStatus")
       List<Candidate> findCandidatesByAssessmentAndStatus(
                     @Param("assessmentId") Long assessmentId,
                     @Param("attemptStatus") AttemptStatus attemptStatus);

       /**
        * Find candidates created within a date range
        */
       @Query("SELECT c FROM Candidate c " +
                     "WHERE c.createdDate >= :startDate AND c.createdDate <= :endDate")
       List<Candidate> findCandidatesByCreatedDateBetween(
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Find candidates with any attempt in a specific assessment
        */
       @Query("SELECT DISTINCT c FROM Candidate c " +
                     "JOIN c.candidateAttempts ca " +
                     "WHERE ca.assessment.id = :assessmentId")
       List<Candidate> findCandidatesByAssessment(@Param("assessmentId") Long assessmentId);

       /**
        * Count candidates with specific filters (useful for pagination metadata)
        */
       // @Query("SELECT COUNT(DISTINCT c) FROM Candidate c " +
       //               "LEFT JOIN c.candidateAttempts ca " +
       //               "LEFT JOIN ca.assessment a " +
       //               "WHERE (:attemptStatus IS NULL OR ca.status = :attemptStatus) " +
       //               "AND (:assessmentId IS NULL OR a.id = :assessmentId) " +
       //               "AND (:createdDateFrom IS NULL OR c.createdDate >= :createdDateFrom) " +
       //               "AND (:createdDateTo IS NULL OR c.createdDate <= :createdDateTo)")
       // Long countCandidatesWithFilters(
       //               @Param("attemptStatus") AttemptStatus attemptStatus,
       //               @Param("assessmentId") Long assessmentId,
       //               @Param("createdDateFrom") LocalDateTime createdDateFrom,
       //               @Param("createdDateTo") LocalDateTime createdDateTo);
}