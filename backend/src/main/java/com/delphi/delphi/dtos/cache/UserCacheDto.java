package com.delphi.delphi.dtos.cache;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.utils.git.GithubAccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserCacheDto implements UserDetails {
    private Long id;
    private String name;
    private String email;
    private String encryptedPassword;
    private String organizationName;
    private String githubAccessToken;
    private String githubUsername;
    private GithubAccountType githubAccountType;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<Long> assessmentIds;
    private List<Long> candidateIds;

    public UserCacheDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.encryptedPassword = user.getPassword();
        this.organizationName = user.getOrganizationName();
        this.githubAccessToken = user.getGithubAccessToken();
        this.githubUsername = user.getGithubUsername();
        this.githubAccountType = user.getGithubAccountType();
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
        this.assessmentIds = user.getAssessments().stream().map(Assessment::getId).collect(Collectors.toList());
        this.candidateIds = user.getCandidates().stream().map(Candidate::getId).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    public String getGithubAccessToken() {
        return githubAccessToken;
    }
    public void setGithubAccessToken(String githubAccessToken) {
        this.githubAccessToken = githubAccessToken;
    }
    public String getGithubUsername() {
        return githubUsername;
    }
    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }
    public GithubAccountType getGithubAccountType() {
        return githubAccountType;
    }
    public void setGithubAccountType(GithubAccountType githubAccountType) {
        this.githubAccountType = githubAccountType;
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
    public List<Long> getAssessmentIds() {
        return assessmentIds;
    }
    public void setAssessmentIds(List<Long> assessmentIds) {
        this.assessmentIds = assessmentIds;
    }
    public List<Long> getCandidateIds() {
        return candidateIds;
    }
    public void setCandidateIds(List<Long> candidateIds) {
        this.candidateIds = candidateIds;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return encryptedPassword;
    }

}
