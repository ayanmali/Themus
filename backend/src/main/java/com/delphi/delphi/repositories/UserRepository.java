package com.delphi.delphi.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find users by organization name with pagination
    Page<User> findByOrganizationNameContainingIgnoreCase(String organizationName, Pageable pageable);
    
    // Find users by name with pagination
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find users created within date range
    @Query("SELECT u FROM User u WHERE u.createdDate BETWEEN :startDate AND :endDate")
    Page<User> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);
    
    // Find users with active assessments
    @Query("SELECT DISTINCT u FROM User u JOIN u.assessments a WHERE a.status = 'ACTIVE'")
    Page<User> findUsersWithActiveAssessments(Pageable pageable);
    
    // Count users by organization
    @Query("SELECT COUNT(u) FROM User u WHERE u.organizationName = :organizationName")
    Long countByOrganizationName(@Param("organizationName") String organizationName);
    
    // Find users with assessments count
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.assessments WHERE u.id = :userId")
    Optional<User> findByIdWithAssessments(@Param("userId") Long userId);
}
