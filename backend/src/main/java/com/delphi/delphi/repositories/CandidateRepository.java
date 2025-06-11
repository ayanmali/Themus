package com.delphi.delphi.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    // Find candidate by email
    Optional<Candidate> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find candidates by user ID with pagination
    Page<Candidate> findByUserId(Long userId, Pageable pageable);
    
    // Find candidates by first name (case-insensitive)
    Page<Candidate> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    
    // Find candidates by last name (case-insensitive)
    Page<Candidate> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    
    // Find candidates by full name (case-insensitive)
    @Query("SELECT c FROM Candidate c WHERE LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<Candidate> findByFullNameContainingIgnoreCase(@Param("fullName") String fullName, Pageable pageable);
    
    // Find candidates by email domain
    @Query("SELECT c FROM Candidate c WHERE c.email LIKE CONCAT('%@', :domain)")
    Page<Candidate> findByEmailDomain(@Param("domain") String domain, Pageable pageable);
    
    // Find candidates created within date range
    @Query("SELECT c FROM Candidate c WHERE c.createdDate BETWEEN :startDate AND :endDate")
    Page<Candidate> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);
    
    // Find candidates by assessment ID
    @Query("SELECT c FROM Candidate c JOIN c.assessments a WHERE a.id = :assessmentId")
    Page<Candidate> findByAssessmentId(@Param("assessmentId") Long assessmentId, Pageable pageable);
    
    // Find candidates with attempts for specific assessment
    @Query("SELECT DISTINCT c FROM Candidate c JOIN c.candidateAttempts ca WHERE ca.assessment.id = :assessmentId")
    Page<Candidate> findCandidatesWithAttemptsForAssessment(@Param("assessmentId") Long assessmentId, Pageable pageable);
    
    // Find candidates with no attempts
    @Query("SELECT c FROM Candidate c WHERE c.candidateAttempts IS EMPTY")
    Page<Candidate> findCandidatesWithNoAttempts(Pageable pageable);
    
    // Find candidates by user and assessment
    @Query("SELECT c FROM Candidate c JOIN c.assessments a WHERE c.user.id = :userId AND a.id = :assessmentId")
    Page<Candidate> findByUserIdAndAssessmentId(@Param("userId") Long userId, 
                                              @Param("assessmentId") Long assessmentId, 
                                              Pageable pageable);
    
    // Count candidates by user
    Long countByUserId(Long userId);
    
    // Find candidates with their attempt counts
    @Query("SELECT c, COUNT(ca) as attemptCount FROM Candidate c LEFT JOIN c.candidateAttempts ca GROUP BY c.id")
    Page<Object[]> findCandidatesWithAttemptCount(Pageable pageable);
}