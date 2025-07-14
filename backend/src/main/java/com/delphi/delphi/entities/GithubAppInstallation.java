package com.delphi.delphi.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a GitHub App installation linked to a user
 * This tracks which GitHub repositories/organizations have installed our GitHub App
 * and which user account they're associated with in our system
 */
@Entity
@Table(name = "github_app_installations")
public class GithubAppInstallation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "installation_id", nullable = false, unique = true)
    private Long installationId; // GitHub's installation ID
    
    @Column(name = "installation_token", columnDefinition = "TEXT")
    private String installationToken; // Encrypted installation access token
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "account_login", length = 100)
    private String accountLogin; // GitHub username/org name where app is installed
    
    @Column(name = "account_id")
    private Long accountId; // GitHub account ID
    
    @Column(name = "account_type", length = 20)
    private String accountType; // "User" or "Organization"
    
    @Column(name = "repository_selection", length = 20)
    private String repositorySelection; // "all" or "selected"
    
    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions; // JSON string of granted permissions
    
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
    
    // Many-to-one relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public GithubAppInstallation() {
    }

    public GithubAppInstallation(Long installationId, String accountLogin, Long accountId, String accountType, 
                                String repositorySelection, String permissions, User user) {
        this.installationId = installationId;
        this.accountLogin = accountLogin;
        this.accountId = accountId;
        this.accountType = accountType;
        this.repositorySelection = repositorySelection;
        this.permissions = permissions;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstallationId() {
        return installationId;
    }

    public void setInstallationId(Long installationId) {
        this.installationId = installationId;
    }

    public String getInstallationToken() {
        return installationToken;
    }

    public void setInstallationToken(String installationToken) {
        this.installationToken = installationToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getAccountLogin() {
        return accountLogin;
    }

    public void setAccountLogin(String accountLogin) {
        this.accountLogin = accountLogin;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getRepositorySelection() {
        return repositorySelection;
    }

    public void setRepositorySelection(String repositorySelection) {
        this.repositorySelection = repositorySelection;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }

    public void setSuspendedAt(LocalDateTime suspendedAt) {
        this.suspendedAt = suspendedAt;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Check if the installation token is expired
     */
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && tokenExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the installation is suspended
     */
    public boolean isSuspended() {
        return suspendedAt != null;
    }
} 