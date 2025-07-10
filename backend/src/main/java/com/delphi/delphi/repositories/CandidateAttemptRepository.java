package com.delphi.delphi.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.utils.AttemptStatus;

@Repository
public interface CandidateAttemptRepository extends JpaRepository<CandidateAttempt, Long> {
    
    // Find attempts by candidate ID with pagination
    Page<CandidateAttempt> findByCandidateId(Long candidateId, Pageable pageable);
    
    // Find attempts by assessment ID with pagination
    Page<CandidateAttempt> findByAssessmentId(Long assessmentId, Pageable pageable);
    
    // Find attempts by status with pagination
    Page<CandidateAttempt> findByStatus(AttemptStatus status, Pageable pageable);
    
    // Find attempts by candidate and assessment
    Optional<CandidateAttempt> findByCandidateIdAndAssessmentId(Long candidateId, Long assessmentId);
    
    // Find attempts by candidate and status
    Page<CandidateAttempt> findByCandidateIdAndStatus(Long candidateId, AttemptStatus status, Pageable pageable);
    
    // Find attempts by assessment and status
    Page<CandidateAttempt> findByAssessmentIdAndStatus(Long assessmentId, AttemptStatus status, Pageable pageable);
    
    // Find attempts by language choice
    Page<CandidateAttempt> findByLanguageChoiceIgnoreCase(String languageChoice, Pageable pageable);
    
    // Find attempts created within date range
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.createdDate BETWEEN :startDate AND :endDate")
    Page<CandidateAttempt> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate, 
                                                  Pageable pageable);
    
    // Find attempts started within date range
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.startedDate BETWEEN :startDate AND :endDate")
    Page<CandidateAttempt> findByStartedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate, 
                                                  Pageable pageable);
    
    // Find attempts submitted within date range
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.completedDate BETWEEN :startDate AND :endDate")
    Page<CandidateAttempt> findByCompletedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate, 
                                                    Pageable pageable);
    
    // Find overdue attempts (started but not submitted within assessment duration)
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.status = 'STARTED' " +
           "AND ca.startedDate IS NOT NULL " +
           "AND FUNCTION('TIMESTAMPDIFF', MINUTE, ca.startedDate, :currentTime) > ca.assessment.duration")
    Page<CandidateAttempt> findOverdueAttempts(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);
    
    // Find attempts by user (through candidate relationship)
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.candidate.user.id = :userId")
    Page<CandidateAttempt> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find attempts with evaluation
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.evaluation IS NOT NULL")
    Page<CandidateAttempt> findAttemptsWithEvaluation(Pageable pageable);
    
    // Find attempts without evaluation but submitted
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.status = 'SUBMITTED' AND ca.evaluation IS NULL")
    Page<CandidateAttempt> findSubmittedAttemptsWithoutEvaluation(Pageable pageable);
    
    // Count attempts by status for an assessment
    @Query("SELECT COUNT(ca) FROM CandidateAttempt ca WHERE ca.assessment.id = :assessmentId AND ca.status = :status")
    Long countByAssessmentIdAndStatus(@Param("assessmentId") Long assessmentId, @Param("status") AttemptStatus status);
    
    // Count attempts by candidate
    Long countByCandidateId(Long candidateId);
    
    // Find attempts with candidate and assessment details
    @Query("SELECT ca FROM CandidateAttempt ca " +
           "JOIN FETCH ca.candidate c " +
           "JOIN FETCH ca.assessment a " +
           "WHERE ca.id = :attemptId")
    Optional<CandidateAttempt> findByIdWithDetails(@Param("attemptId") Long attemptId);
    
    // Find recent attempts for a user
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.candidate.user.id = :userId ORDER BY ca.createdDate DESC")
    Page<CandidateAttempt> findRecentAttemptsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find attempts by assessment user (assessments created by specific user)
    @Query("SELECT ca FROM CandidateAttempt ca WHERE ca.assessment.user.id = :userId")
    Page<CandidateAttempt> findByAssessmentUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find candidate attempts with multiple optional filters
    @Query("SELECT ca FROM CandidateAttempt ca WHERE " +
           "(:candidateId IS NULL OR ca.candidate.id = :candidateId) AND " +
           "(:assessmentId IS NULL OR ca.assessment.id = :assessmentId) AND " +
           "(:status IS NULL OR ca.status = :status) AND " +
           "(:startedAfter IS NULL OR ca.startedDate >= :startedAfter) AND " +
           "(:startedBefore IS NULL OR ca.startedDate <= :startedBefore) AND " +
           "(:completedAfter IS NULL OR ca.completedDate >= :completedAfter) AND " +
           "(:completedBefore IS NULL OR ca.completedDate <= :completedBefore)")
    Page<CandidateAttempt> findWithFilters(@Param("candidateId") Long candidateId,
                                         @Param("assessmentId") Long assessmentId,
                                         @Param("status") AttemptStatus status,
                                         @Param("startedAfter") LocalDateTime startedAfter,
                                         @Param("startedBefore") LocalDateTime startedBefore,
                                         @Param("completedAfter") LocalDateTime completedAfter,
                                         @Param("completedBefore") LocalDateTime completedBefore,
                                         Pageable pageable);
}
