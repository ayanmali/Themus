package com.delphi.delphi.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.delphi.delphi.utils.AttemptStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

/*
 * Represents a candidate's attempt at an assessment.
 * 
 * This is a many-to-many relationship between assessments and candidates.
 * 
 * A candidate can have only one repositoryfor a single assessment, but they can have multiple repositories across different assessments.
 * A CandidateRepository is created when a candidate starts an assessment, and has one candidate associated with it
 * 
 * An assessment can have multiple repositories.
 */
@Entity
@Table(name = "candidate_attempts")
public class CandidateAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "github_repository_link", columnDefinition = "TEXT")
    private String githubRepositoryLink;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.INVITED;
    
    // if the assessment doesn't support multiple languages, this will be null
    @Size(max = 100, message = "Language choice must not exceed 100 characters")
    @Column(name = "language_choice", length = 100)
    private String languageChoice;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
    
    @Column(name = "started_date")
    private LocalDateTime startedDate;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    @Column(name = "evaluated_date")
    private LocalDateTime evaluatedDate;
    
    // Many-to-one relationship with Candidate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    // Many-to-one relationship with Assessment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    // One-to-one relationship with Evaluation (placeholder)
    @OneToOne(mappedBy = "candidateAttempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Evaluation evaluation;

    public CandidateAttempt() {
    }

    public CandidateAttempt(String githubRepositoryLink, AttemptStatus status, String languageChoice, LocalDateTime createdDate, LocalDateTime updatedDate, LocalDateTime startedDate, LocalDateTime completedDate, LocalDateTime evaluatedDate, Candidate candidate, Assessment assessment, Evaluation evaluation) {
        this.githubRepositoryLink = githubRepositoryLink;
        this.status = status;
        this.languageChoice = languageChoice;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.startedDate = startedDate;
        this.completedDate = completedDate;
        this.evaluatedDate = evaluatedDate;
        this.candidate = candidate;
        this.assessment = assessment;
        this.evaluation = evaluation;
    }
    
    @AssertTrue(message = "Submitted date must be after started date")
    private boolean isCompletedDateAfterStartedDate() {
        return startedDate == null || completedDate == null || completedDate.isAfter(startedDate);
    }
    
    @AssertTrue(message = "Evaluated date must be after submitted date")
    private boolean isEvaluatedDateAfterCompletedDate() {
        return completedDate == null || evaluatedDate == null || evaluatedDate.isAfter(completedDate);
    }

    // @PrePersist
    // @PreUpdate
    // public void updateStatusIfExpired() {
    //     if (startedDate != null && startedDate.isAfter(LocalDateTime.now()) && 
    //         AttemptStatus.STARTED.equals(status)) {
    //         this.status = AttemptStatus.EXPIRED;
    //     }
    // }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGithubRepositoryLink() {
        return githubRepositoryLink;
    }

    public void setGithubRepositoryLink(String githubRepositoryLink) {
        this.githubRepositoryLink = githubRepositoryLink;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public String getLanguageChoice() {
        return languageChoice;
    }

    public void setLanguageChoice(String languageChoice) {
        this.languageChoice = languageChoice;
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

    public LocalDateTime getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public LocalDateTime getEvaluatedDate() {
        return evaluatedDate;
    }

    public void setEvaluatedDate(LocalDateTime evaluatedDate) {
        this.evaluatedDate = evaluatedDate;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    
}