package com.delphi.delphi.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.utils.enums.AssessmentStatus;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long>, JpaSpecificationExecutor<Assessment> {
       // Find assessments by user ID with pagination
       Page<Assessment> findByUserId(Long userId, Pageable pageable);

       // Find assessments by status with pagination
       Page<Assessment> findByStatus(AssessmentStatus status, Pageable pageable);

       // Find assessments by github repo name with pagination
       Page<Assessment> findByGithubRepoName(String repoName, Pageable pageable);

       // Find assessments by user and status
       Page<Assessment> findByUserIdAndStatus(Long userId, AssessmentStatus status, Pageable pageable);

       // Find assessments by name containing text (case-insensitive)
       Page<Assessment> findByNameContainingIgnoreCase(String name, Pageable pageable);

       // Find assessments by role name
       Page<Assessment> findByRoleContainingIgnoreCase(String role, Pageable pageable);

       // Find assessments within date range
       @Query("SELECT a FROM Assessment a WHERE a.startDate >= :startDate AND a.endDate <= :endDate")
       Page<Assessment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     Pageable pageable);

       // Find active assessments within current date
       @Query("SELECT a FROM Assessment a WHERE a.status = 'ACTIVE' AND a.startDate <= :currentDate AND a.endDate >= :currentDate")
       Page<Assessment> findActiveAssessmentsInDateRange(@Param("currentDate") LocalDateTime currentDate,
                     Pageable pageable);

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
       // @Query("SELECT a, COUNT(ca) as attemptCount FROM Assessment a LEFT JOIN
       // a.candidateAttempts ca GROUP BY a.id")
       // Page<Object[]> findAssessmentsWithAttemptCount(Pageable pageable);

       // Find assessments created by user in date range
       @Query("SELECT a FROM Assessment a WHERE a.user.id = :userId AND a.createdDate BETWEEN :startDate AND :endDate")
       Page<Assessment> findByUserIdAndCreatedDateBetween(@Param("userId") Long userId,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     Pageable pageable);

       // Count assessments by status for a user
       @Query("SELECT COUNT(a) FROM Assessment a WHERE a.user.id = :userId AND a.status = :status")
       Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AssessmentStatus status);

       // Find assessments with multiple optional filters
       // Use COALESCE to avoid untyped `? is null` SQL parameters that break on
       // PostgreSQL
       // @Query("SELECT a FROM Assessment a WHERE " +
       //               "a.status = COALESCE(:status, a.status) AND " +
       //               "a.createdDate >= COALESCE(:startDate, a.createdDate) AND " +
       //               "a.createdDate <= COALESCE(:endDate, a.createdDate)")
       // Page<Assessment> findWithFilters(@Param("status") AssessmentStatus status,
       //               @Param("startDate") LocalDateTime startDate,
       //               @Param("endDate") LocalDateTime endDate,
       //               Pageable pageable);

       // Find assessments with multiple optional filters for a specific user
       // Use COALESCE to preserve parameter types for databases like PostgreSQL
       // @Query("SELECT DISTINCT a FROM Assessment a " +
       //               "LEFT JOIN a.skills s " +
       //               "WHERE a.user.id = :userId AND " +
       //               "a.status = COALESCE(:status, a.status) AND " +
       //               "a.createdDate >= COALESCE(:startDate, a.createdDate) AND " +
       //               "a.createdDate <= COALESCE(:endDate, a.createdDate) AND " +
       //               "(:filterSkills = false OR s IN :skills)")
       // Page<Assessment> findWithFiltersForUser(@Param("userId") Long userId,
       //               @Param("status") AssessmentStatus status,
       //               @Param("startDate") LocalDateTime startDate,
       //               @Param("endDate") LocalDateTime endDate,
       //               @Param("filterSkills") boolean filterSkills,
       //               @Param("skills") List<String> skills,
       //               Pageable pageable);

       @Modifying
       @Query("UPDATE Assessment a SET a.status = 'INACTIVE' " +
                     "WHERE a.endDate < :currentDate AND a.status = 'ACTIVE'")
       int updateExpiredAssessments(@Param("currentDate") LocalDateTime currentDate);

       @Modifying
       @Query("UPDATE Assessment a SET a.instructions = :setupInstructions WHERE a.id = :id")
       int updateSetupInstructions(@Param("id") Long id, @Param("setupInstructions") String setupInstructions);

       @Modifying
       @Query("UPDATE Assessment a SET a.status = :status WHERE a.id = :id")
       int updateStatus(@Param("id") Long id, @Param("status") AssessmentStatus status);

}
