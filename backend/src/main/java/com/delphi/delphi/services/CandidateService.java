package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.CandidateRepository;

@Service
@Transactional
// TODO: add cache annotations for other entity caches
public class CandidateService {
    
    private final CandidateRepository candidateRepository;
    
    private final UserService userService;

    public CandidateService(CandidateRepository candidateRepository, UserService userService) {
        this.candidateRepository = candidateRepository;
        this.userService = userService;
    }
    
    // Create a new candidate
    @CachePut(value = "candidates", key = "#result.id")
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

    @CachePut(value = "candidates", key = "#result.id")
    public Candidate createCandidate(String firstName, String lastName, String email, User user) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setEmail(email);
        candidate.setUser(user);
        return createCandidate(candidate);
    }

    // Get candidate by ID
    @Cacheable(value = "candidates", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Candidate> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }

    // Get candidate by ID or throw exception
    @Cacheable(value = "candidates", key = "#id")
    @Transactional(readOnly = true)
    public Candidate getCandidateByIdOrThrow(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
    }
    
    // Get candidate by email
    @Cacheable(value = "candidates", key = "#email")
    @Transactional(readOnly = true)
    public Optional<Candidate> getCandidateByEmail(String email) {
        return candidateRepository.findByEmail(email);
    }
    
    // Get all candidates with pagination
    @Cacheable(value = "candidates", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> getAllCandidates(Pageable pageable) {
        return candidateRepository.findAll(pageable);
    }
    
    // Update candidate
    @CachePut(value = "candidates", key = "#result.id")
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
    @CacheEvict(value = "candidates", key = "#id")
    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new IllegalArgumentException("Candidate not found with id: " + id);
        }
        candidateRepository.deleteById(id);
    }
    
    // Check if email exists
    @Cacheable(value = "candidates", key = "emailExists + ':' + #email")
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return candidateRepository.existsByEmail(email);
    }
    
    // Get candidates by user ID
    @Cacheable(value = "candidates", key = "#userId + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByUserId(Long userId, Pageable pageable) {
        return candidateRepository.findByUserId(userId, pageable);
    }
    
    // Search candidates by first name
    @Cacheable(value = "candidates", key = "#firstName + ':' + #pageable.pageNumber")
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
    @Cacheable(value = "candidates", key = "#fullName + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidatesByFullName(String fullName, Pageable pageable) {
        return candidateRepository.findByFullNameContainingIgnoreCase(fullName, pageable);
    }
    
    // Get candidates by email domain
    @Cacheable(value = "candidates", key = "#domain + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByEmailDomain(String domain, Pageable pageable) {
        return candidateRepository.findByEmailDomain(domain, pageable);
    }
    
    // Get candidates created within date range
    @Cacheable(value = "candidates", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return candidateRepository.findByCreatedDateBetween(startDate, endDate, pageable);
    }
    
    // Get candidates by assessment ID
    @Cacheable(value = "candidates", key = "#assessmentId + ':' + #pageable.pageNumber")
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
    @Cacheable(value = "candidates", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesWithNoAttempts(Pageable pageable) {
        return candidateRepository.findCandidatesWithNoAttempts(pageable);
    }
    
    // Get candidates by user and assessment
    @Cacheable(value = "candidates", key = "#userId + ':' + #assessmentId + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByUserAndAssessment(Long userId, Long assessmentId, Pageable pageable) {
        return candidateRepository.findByUserIdAndAssessmentId(userId, assessmentId, pageable);
    }
    
    // Count candidates by user
    @Cacheable(value = "candidates", key = "count + ':' + #userId")
    @Transactional(readOnly = true)
    public Long countCandidatesByUser(Long userId) {
        return candidateRepository.countByUserId(userId);
    }
    
    // Get candidates with attempt count
    @Cacheable(value = "candidates", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Object[]> getCandidatesWithAttemptCount(Pageable pageable) {
        return candidateRepository.findCandidatesWithAttemptCount(pageable);
    }
    
    // Update candidate metadata
    @CachePut(value = "candidates", key = "#result.id")
    public Candidate updateCandidateMetadata(Long id, Map<String, String> metadata) {
        Candidate candidate = getCandidateByIdOrThrow(id);
        candidate.setMetadata(metadata);
        return candidateRepository.save(candidate);
    }
    
    // Add metadata entry
    @CachePut(value = "candidates", key = "#result.id")
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
    @CachePut(value = "candidates", key = "#result.id")
    public Candidate removeMetadata(Long id, String key) {
        Candidate candidate = getCandidateByIdOrThrow(id);
        if (candidate.getMetadata() != null) {
            candidate.getMetadata().remove(key);
        }
        return candidateRepository.save(candidate);
    }
}
