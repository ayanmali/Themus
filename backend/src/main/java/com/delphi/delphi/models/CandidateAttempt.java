package com.delphi.delphi.models;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "github_repository_link", columnDefinition = "TEXT")
    private String githubRepositoryLink;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.INVITED;
    
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
    
    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;
    
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
    
    @AssertTrue(message = "Submitted date must be after started date")
    private boolean isSubmittedDateAfterStartedDate() {
        return startedDate == null || submittedDate == null || submittedDate.isAfter(startedDate);
    }
    
    @AssertTrue(message = "Evaluated date must be after submitted date")
    private boolean isEvaluatedDateAfterSubmittedDate() {
        return submittedDate == null || evaluatedDate == null || evaluatedDate.isAfter(submittedDate);
    }
}
