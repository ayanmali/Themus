package com.delphi.delphi.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.GithubAppInstallation;
import com.delphi.delphi.entities.User;

@Repository
public interface GithubAppInstallationRepository extends JpaRepository<GithubAppInstallation, Long> {
    
    /**
     * Find installation by GitHub's installation ID
     */
    Optional<GithubAppInstallation> findByInstallationId(Long installationId);
    
    /**
     * Find all installations for a specific user
     */
    List<GithubAppInstallation> findByUser(User user);
    
    /**
     * Find all installations for a user by user ID
     */
    List<GithubAppInstallation> findByUserId(Long userId);
    
    /**
     * Find installation by user and account login (GitHub username/org)
     */
    Optional<GithubAppInstallation> findByUserAndAccountLogin(User user, String accountLogin);
    
    /**
     * Find all non-suspended installations for a user
     */
    @Query("SELECT i FROM GithubAppInstallation i WHERE i.user = :user AND i.suspendedAt IS NULL")
    List<GithubAppInstallation> findActiveInstallationsByUser(@Param("user") User user);
    
    /**
     * Find all installations by account type (User or Organization)
     */
    List<GithubAppInstallation> findByAccountType(String accountType);
    
    /**
     * Check if an installation exists for a specific installation ID
     */
    boolean existsByInstallationId(Long installationId);
    
    /**
     * Check if user has any GitHub App installations
     */
    boolean existsByUser(User user);
    
    /**
     * Find installations with expired tokens
     */
    @Query("SELECT i FROM GithubAppInstallation i WHERE i.tokenExpiresAt < CURRENT_TIMESTAMP")
    List<GithubAppInstallation> findExpiredTokenInstallations();
    
    /**
     * Find installations that need token refresh (expire within next hour)
     */
    @Query("SELECT i FROM GithubAppInstallation i WHERE i.tokenExpiresAt BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL 1 HOUR")
    List<GithubAppInstallation> findInstallationsNeedingTokenRefresh();
    
    /**
     * Delete installation by installation ID
     */
    void deleteByInstallationId(Long installationId);
} 