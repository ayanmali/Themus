package com.delphi.delphi.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.models.Evaluation;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    // Find evaluation by candidate attempt ID
    Optional<Evaluation> findByCandidateAttemptId(Long candidateAttemptId);
    
    // Find evaluations created within date range
    @Query("SELECT e FROM Evaluation e WHERE e.createdDate BETWEEN :startDate AND :endDate")
    Page<Evaluation> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate, 
                                            Pageable pageable);
    
    // Find evaluations by candidate ID (through candidate attempt)
    @Query("SELECT e FROM Evaluation e WHERE e.candidateAttempt.candidate.id = :candidateId")
    Page<Evaluation> findByCandidateId(@Param("candidateId") Long candidateId, Pageable pageable);
    
    // Find evaluations by assessment ID (through candidate attempt)
    @Query("SELECT e FROM Evaluation e WHERE e.candidateAttempt.assessment.id = :assessmentId")
    Page<Evaluation> findByAssessmentId(@Param("assessmentId") Long assessmentId, Pageable pageable);
    
    // Find evaluations by user ID (through assessment user)
    @Query("SELECT e FROM Evaluation e WHERE e.candidateAttempt.assessment.user.id = :userId")
    Page<Evaluation> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find evaluation with full details
    @Query("SELECT e FROM Evaluation e " +
           "JOIN FETCH e.candidateAttempt ca " +
           "JOIN FETCH ca.candidate c " +
           "JOIN FETCH ca.assessment a " +
           "WHERE e.id = :evaluationId")
    Optional<Evaluation> findByIdWithDetails(@Param("evaluationId") Long evaluationId);
    
    // Count evaluations by assessment
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.candidateAttempt.assessment.id = :assessmentId")
    Long countByAssessmentId(@Param("assessmentId") Long assessmentId);
    
    // Find recent evaluations for a user
    @Query("SELECT e FROM Evaluation e WHERE e.candidateAttempt.assessment.user.id = :userId ORDER BY e.createdDate DESC")
    Page<Evaluation> findRecentEvaluationsByUserId(@Param("userId") Long userId, Pageable pageable);
}
