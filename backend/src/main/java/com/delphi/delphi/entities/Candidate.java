package com.delphi.delphi.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.delphi.delphi.dtos.NewCandidateDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * A Candidate is someone who is taking an assessment.
 * 
 * A Candidate has a name, email, and a list of candidate repositories that they have created across different assessments.
 * 
 * A CandidateRepository is created when a candidate starts an assessment, and has one candidate associated with it.
 * 
 * A Candidate can have multiple repositories, but a CandidateRepository can only have one candidate.
 *  
 */
@Entity
@Table(name = "candidates")
public class Candidate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String email;
    
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
    
    // One-to-many relationship with CandidateAttempt
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CandidateAttempt> candidateAttempts;
    
    // Many-to-many relationship with Assessment
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "candidate_assessments",
        joinColumns = @JoinColumn(name = "candidate_id"),
        inverseJoinColumns = @JoinColumn(name = "assessment_id")
    )
    @JsonIgnore
    private List<Assessment> assessments;

    // Metadata as key-value pairs
    @ElementCollection
    @CollectionTable(name = "candidate_metadata", joinColumns = @JoinColumn(name = "candidate_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;

    public Candidate() {
    }

    public Candidate(String firstName, String lastName, String email, User user, List<CandidateAttempt> candidateAttempts, List<Assessment> assessments, Map<String, String> metadata) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.user = user;
        this.candidateAttempts = candidateAttempts;
        this.assessments = assessments;
        this.metadata = metadata;
    }

    public Candidate(NewCandidateDto newCandidateDto) {
        this.firstName = newCandidateDto.getFirstName();
        this.lastName = newCandidateDto.getLastName();
        this.email = newCandidateDto.getEmail();
        this.metadata = newCandidateDto.getMetadata();
    }
    
    // Computed property for full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<CandidateAttempt> getCandidateAttempts() {
        return candidateAttempts;
    }

    public void setCandidateAttempts(List<CandidateAttempt> candidateAttempts) {
        this.candidateAttempts = candidateAttempts;
    }

    public List<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<Assessment> assessments) {
        this.assessments = assessments;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    
}
