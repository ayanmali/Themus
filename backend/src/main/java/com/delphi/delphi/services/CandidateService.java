package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.CandidateRepository;

@Service
@Transactional
public class CandidateService {
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private UserService userService;
    
    // Create a new candidate
    public Candidate createCandidate(Candidate candidate) {
        if (candidateRepository.existsByEmail(candidate.getEmail())) {
            throw new IllegalArgumentException("Candidate with email " + candidate.getEmail() + " already exists");
        }
        
        // Validate that the associated user exists
        if (candidate.getUser() != null && candidate.getUser().getId() != null) {
            User user = userService.getUserByIdOrThrow(candidate.getUser().getId());
            candidate.setUser(user);
        }
        
        return candidateRepository.save(candidate);
    }
    
    // Get candidate by ID
    @Transactional(readOnly = true)
    public Optional<Candidate> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }
    
    // Get candidate by ID or throw exception
    @Transactional(readOnly = true)
    public Candidate getCandidateByIdOrThrow(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
    }
    
    // Get candidate by email
    @Transactional(readOnly = true)
    public Optional<Candidate> getCandidateByEmail(String email) {
        return candidateRepository.findByEmail(email);
    }
    
    // Get all candidates with pagination
    @Transactional(readOnly = true)
    public Page<Candidate> getAllCandidates(Pageable pageable) {
        return candidateRepository.findAll(pageable);
    }
    
    // Update candidate
    public Candidate updateCandidate(Long id, Candidate candidateUpdates) {
        Candidate existingCandidate = getCandidateByIdOrThrow(id);
        
        // Update fields if provided
        if (candidateUpdates.getFirstName() != null) {
            existingCandidate.setFirstName(candidateUpdates.getFirstName());
        }
        if (candidateUpdates.getLastName() != null) {
            existingCandidate.setLastName(candidateUpdates.getLastName());
        }
        if (candidateUpdates.getEmail() != null && !candidateUpdates.getEmail().equals(existingCandidate.getEmail())) {
            if (candidateRepository.existsByEmail(candidateUpdates.getEmail())) {
                throw new IllegalArgumentException("Email " + candidateUpdates.getEmail() + " is already in use");
            }
            existingCandidate.setEmail(candidateUpdates.getEmail());
        }
        if (candidateUpdates.getMetadata() != null) {
            existingCandidate.setMetadata(candidateUpdates.getMetadata());
        }
        
        return candidateRepository.save(existingCandidate);
    }
    
    // Delete candidate
    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new IllegalArgumentException("Candidate not found with id: " + id);
        }
        candidateRepository.deleteById(id);
    }
    
    // Check if email exists
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return candidateRepository.existsByEmail(email);
    }
    
    // Get candidates by user ID
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByUserId(Long userId, Pageable pageable) {
        return candidateRepository.findByUserId(userId, pageable);
    }
    
    // Search candidates by first name
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidatesByFirstName(String firstName, Pageable pageable) {
        return candidateRepository.findByFirstNameContainingIgnoreCase(firstName, pageable);
    }
    
    // Search candidates by last name
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidatesByLastName(String lastName, Pageable pageable) {
        return candidateRepository.findByLastNameContainingIgnoreCase(lastName, pageable);
    }
    
    // Search candidates by full name
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidatesByFullName(String fullName, Pageable pageable) {
        return candidateRepository.findByFullNameContainingIgnoreCase(fullName, pageable);
    }
    
    // Get candidates by email domain
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByEmailDomain(String domain, Pageable pageable) {
        return candidateRepository.findByEmailDomain(domain, pageable);
    }
    
    // Get candidates created within date range
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return candidateRepository.findByCreatedDateBetween(startDate, endDate, pageable);
    }
    
    // Get candidates by assessment ID
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByAssessmentId(Long assessmentId, Pageable pageable) {
        return candidateRepository.findByAssessmentId(assessmentId, pageable);
    }
    
    // Get candidates with attempts for specific assessment
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesWithAttemptsForAssessment(Long assessmentId, Pageable pageable) {
        return candidateRepository.findCandidatesWithAttemptsForAssessment(assessmentId, pageable);
    }
    
    // Get candidates with no attempts
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesWithNoAttempts(Pageable pageable) {
        return candidateRepository.findCandidatesWithNoAttempts(pageable);
    }
    
    // Get candidates by user and assessment
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByUserAndAssessment(Long userId, Long assessmentId, Pageable pageable) {
        return candidateRepository.findByUserIdAndAssessmentId(userId, assessmentId, pageable);
    }
    
    // Count candidates by user
    @Transactional(readOnly = true)
    public Long countCandidatesByUser(Long userId) {
        return candidateRepository.countByUserId(userId);
    }
    
    // Get candidates with attempt count
    @Transactional(readOnly = true)
    public Page<Object[]> getCandidatesWithAttemptCount(Pageable pageable) {
        return candidateRepository.findCandidatesWithAttemptCount(pageable);
    }
    
    // Update candidate metadata
    public Candidate updateCandidateMetadata(Long id, Map<String, String> metadata) {
        Candidate candidate = getCandidateByIdOrThrow(id);
        candidate.setMetadata(metadata);
        return candidateRepository.save(candidate);
    }
    
    // Add metadata entry
    public Candidate addMetadata(Long id, String key, String value) {
        Candidate candidate = getCandidateByIdOrThrow(id);
        if (candidate.getMetadata() == null) {
            candidate.setMetadata(Map.of(key, value));
        } else {
            candidate.getMetadata().put(key, value);
        }
        return candidateRepository.save(candidate);
    }
    
    // Remove metadata entry
    public Candidate removeMetadata(Long id, String key) {
        Candidate candidate = getCandidateByIdOrThrow(id);
        if (candidate.getMetadata() != null) {
            candidate.getMetadata().remove(key);
        }
        return candidateRepository.save(candidate);
    }
}
