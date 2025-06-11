package com.delphi.delphi.models;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Represents an evaluation of a candidate's attempt at an assessment.
 * 
 * An evaluation is created when a candidate completes an assessment, and has one candidate attempt associated with it.
 * 
 * An evaluation is tied to a single candidate attempt.
 */
@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
    
    // One-to-one relationship with CandidateAttempt
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_attempt_id", nullable = false)
    private CandidateAttempt candidateAttempt;
    
    // Additional evaluation fields can be added here as needed
}
