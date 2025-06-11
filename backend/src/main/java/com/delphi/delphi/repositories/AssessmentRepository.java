package com.delphi.delphi.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.utils.AssessmentStatus;
import com.delphi.delphi.utils.AssessmentType;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    // Find assessments by user ID with pagination
    Page<Assessment> findByUserId(Long userId, Pageable pageable);
    
    // Find assessments by status with pagination
    Page<Assessment> findByStatus(AssessmentStatus status, Pageable pageable);
    
    // Find assessments by type with pagination
    Page<Assessment> findByAssessmentType(AssessmentType assessmentType, Pageable pageable);
    
    // Find assessments by user and status
    Page<Assessment> findByUserIdAndStatus(Long userId, AssessmentStatus status, Pageable pageable);
    
    // Find assessments by name containing text (case-insensitive)
    Page<Assessment> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find assessments by role name
    Page<Assessment> findByRoleNameContainingIgnoreCase(String roleName, Pageable pageable);
    
    // Find assessments within date range
    @Query("SELECT a FROM Assessment a WHERE a.startDate >= :startDate AND a.endDate <= :endDate")
    Page<Assessment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate, 
                                   Pageable pageable);
    
    // Find active assessments within current date
    @Query("SELECT a FROM Assessment a WHERE a.status = 'ACTIVE' AND a.startDate <= :currentDate AND a.endDate >= :currentDate")
    Page<Assessment> findActiveAssessmentsInDateRange(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);
    
    // Find assessments by duration range
    @Query("SELECT a FROM Assessment a WHERE a.duration BETWEEN :minDuration AND :maxDuration")
    Page<Assessment> findByDurationBetween(@Param("minDuration") Integer minDuration, 
                                         @Param("minDuration") Integer maxDuration, 
                                         Pageable pageable);
    
    // Find assessments by skill
    @Query("SELECT a FROM Assessment a JOIN a.skills s WHERE s = :skill")
    Page<Assessment> findBySkill(@Param("skill") String skill, Pageable pageable);
    
    // Find assessments by language option
    @Query("SELECT a FROM Assessment a JOIN a.languageOptions l WHERE l = :language")
    Page<Assessment> findByLanguageOption(@Param("language") String language, Pageable pageable);
    
    // Find assessments with candidate attempts count
    @Query("SELECT a, COUNT(ca) as attemptCount FROM Assessment a LEFT JOIN a.candidateAttempts ca GROUP BY a.id")
    Page<Object[]> findAssessmentsWithAttemptCount(Pageable pageable);
    
    // Find assessments created by user in date range
    @Query("SELECT a FROM Assessment a WHERE a.user.id = :userId AND a.createdDate BETWEEN :startDate AND :endDate")
    Page<Assessment> findByUserIdAndCreatedDateBetween(@Param("userId") Long userId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate, 
                                                     Pageable pageable);
    
    // Count assessments by status for a user
    @Query("SELECT COUNT(a) FROM Assessment a WHERE a.user.id = :userId AND a.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AssessmentStatus status);
}
