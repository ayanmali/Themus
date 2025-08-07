package com.delphi.delphi.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.delphi.delphi.utils.AssessmentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/*
 * An Assessment represents the template repository that is used to create a new repository for each candidate.
 * 
 * An assessment is created by a user, and can be used to create multiple repositories for different candidates.
 * 
 * An assessment has a name, description, status, start date, end date, and a list of candidate repositories.
 * 
 * A CandidateRepository is created when a candidate starts an assessment, and has one candidate associated with it
 * 
 * An assessment can have multiple CandidateRepositories, but a CandidateRepository can only have one assessment.
 */
@Entity
@Table(name = "assessments")
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Assessment name is required")
    @Size(max = 200, message = "Assessment name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.DRAFT;
    
    // @Enumerated(EnumType.STRING)
    // @Column(name = "assessment_type", nullable = false)
    // private AssessmentType assessmentType;
    
    @Future(message = "Start date must be in the future")
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Future(message = "End date must be in the future")
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Positive(message = "Duration must be positive")
    @Column(nullable = false)
    private Integer duration; // in minutes
    
    //@NotBlank(message = "GitHub repository link is required")
    @Column(name = "github_repository_link", nullable = false, columnDefinition = "TEXT")
    private String githubRepositoryLink;

    @Column(name = "github_repo_name", columnDefinition = "TEXT")
    private String githubRepoName;
    
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
    
    // Skills as a list of strings
    @ElementCollection
    @CollectionTable(name = "assessment_skills", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "skill")
    private List<String> skills;
    
    // Language options as a list of strings
    @ElementCollection
    @CollectionTable(name = "assessment_language_options", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "language_option")
    private List<String> languageOptions;
    
    // Metadata as key-value pairs
    @ElementCollection
    @CollectionTable(name = "assessment_metadata", joinColumns = @JoinColumn(name = "assessment_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    // One-to-many relationship with CandidateAttempt
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CandidateAttempt> candidateAttempts;
    
    // Many-to-many relationship with Candidate
    @ManyToMany(mappedBy = "assessments", fetch = FetchType.LAZY)
    private List<Candidate> candidates;

    // One-to-one relationship with ChatHistory
    @OneToOne(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChatHistory chatHistory;

    public Assessment() {
        // Empty constructor for Hibernate
    }

    public Assessment(String name, String description, String roleName, LocalDateTime startDate, LocalDateTime endDate, Integer duration, String githubRepositoryLink, User user, List<String> skills, List<String> languageOptions, Map<String, String> metadata) {
        this.name = name;
        this.description = description;
        this.roleName = roleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.githubRepositoryLink = githubRepositoryLink;
        this.user = user;
        this.skills = skills;
        this.languageOptions = languageOptions;
        this.metadata = metadata;
        this.status = AssessmentStatus.DRAFT;
        this.githubRepoName = name.replace(' ', '-');
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getGithubRepositoryLink() {
        return githubRepositoryLink;
    }

    public void setGithubRepositoryLink(String githubRepositoryLink) {
        this.githubRepositoryLink = githubRepositoryLink;
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

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getLanguageOptions() {
        return languageOptions;
    }

    public void setLanguageOptions(List<String> languageOptions) {
        this.languageOptions = languageOptions;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<CandidateAttempt> getCandidateAttempts() {
        return candidateAttempts;
    }

    public void setCandidateAttempts(List<CandidateAttempt> candidateAttempts) {
        this.candidateAttempts = candidateAttempts;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public void addCandidate(Candidate candidate) {
        this.candidates.add(candidate);
    }

    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(ChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }

    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

    public String getGithubRepoName() {
        return githubRepoName;
    }

    public void setGithubRepoName(String githubRepoName) {
        this.githubRepoName = githubRepoName;
    }
}