package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.delphi.delphi.repositories.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.CandidateRepository;

@Service
@Transactional
// TODO: add cache annotations for other entity caches
public class CandidateService {

    private final UserRepository userRepository;
    
    private final CandidateRepository candidateRepository;
    
    public CandidateService(CandidateRepository candidateRepository, UserRepository userRepository) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
    }
    
    // Create a new candidate
    @CachePut(value = "candidates", key = "#result.id")
    public CandidateCacheDto createCandidate(Candidate candidate) {
        if (candidateRepository.existsByEmail(candidate.getEmail())) {
            throw new IllegalArgumentException("Candidate with email " + candidate.getEmail() + " already exists");
        }
        
        // Validate that the associated user exists
        if (candidate.getUser() != null && candidate.getUser().getId() != null) {
            User user = userRepository.findById(candidate.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + candidate.getUser().getId()));
            candidate.setUser(user);
        }
        
        return new CandidateCacheDto(candidateRepository.save(candidate));
    }

    @CachePut(value = "candidates", key = "#result.id")
    public CandidateCacheDto createCandidate(String firstName, String lastName, String email, User user) {
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
    public CandidateCacheDto getCandidateById(Long id) {
        return new CandidateCacheDto(candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id)));
    }

    // Get candidate by ID or throw exception
    @Cacheable(value = "candidates", key = "#id")
    @Transactional(readOnly = true)
    public CandidateCacheDto getCandidateByIdOrThrow(Long id) {
        return new CandidateCacheDto(candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id)));
    }
    
    // Get candidate by email
    @Cacheable(value = "candidates", key = "#email")
    @Transactional(readOnly = true)
    public CandidateCacheDto getCandidateByEmail(String email) {
        return new CandidateCacheDto(candidateRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with email: " + email)));
    }
    
    // Get all candidates with pagination
    @Cacheable(value = "candidates", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getAllCandidates(Pageable pageable) {
        return candidateRepository.findAll(pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }

    // Get candidates with multiple filters
    @Cacheable(value = "candidates", key = "#assessmentId + ':' + #attemptStatus + ':' + #emailDomain + ':' + #firstName + ':' + #lastName + ':' + #createdAfter + ':' + #createdBefore + ':' + #attemptCompletedAfter + ':' + #attemptCompletedBefore + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesWithFilters(Long assessmentId, com.delphi.delphi.utils.AttemptStatus attemptStatus, 
                                                   String emailDomain, String firstName, String lastName,
                                                   LocalDateTime createdAfter, LocalDateTime createdBefore,
                                                   LocalDateTime attemptCompletedAfter, LocalDateTime attemptCompletedBefore, 
                                                   Pageable pageable) {
        return candidateRepository.findWithFilters(assessmentId, attemptStatus, emailDomain, firstName, lastName,
                                                  createdAfter, createdBefore, attemptCompletedAfter, attemptCompletedBefore, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }

    // Get candidates with multiple filters for a specific user
    @Cacheable(value = "candidates", key = "#userId + ':' + #assessmentId + ':' + #attemptStatus + ':' + #emailDomain + ':' + #firstName + ':' + #lastName + ':' + #createdAfter + ':' + #createdBefore + ':' + #attemptCompletedAfter + ':' + #attemptCompletedBefore + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesWithFiltersForUser(Long userId, Long assessmentId, com.delphi.delphi.utils.AttemptStatus attemptStatus, 
                                                          String emailDomain, String firstName, String lastName,
                                                          LocalDateTime createdAfter, LocalDateTime createdBefore,
                                                          LocalDateTime attemptCompletedAfter, LocalDateTime attemptCompletedBefore, 
                                                          Pageable pageable) {
        return candidateRepository.findWithFiltersForUser(userId, assessmentId, attemptStatus, emailDomain, firstName, lastName,
                                                         createdAfter, createdBefore, attemptCompletedAfter, attemptCompletedBefore, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Update candidate
    @CachePut(value = "candidates", key = "#result.id")
    public CandidateCacheDto updateCandidate(Long id, Candidate candidateUpdates) {
        Candidate existingCandidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        
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
        
        return new CandidateCacheDto(candidateRepository.save(existingCandidate));
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
    @Cacheable(value = "candidates", key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesByUserId(Long userId, Pageable pageable) {
        return candidateRepository.findByUserId(userId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Search candidates by first name
    @Cacheable(value = "candidates", key = "#firstName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> searchCandidatesByFirstName(String firstName, Pageable pageable) {
        return candidateRepository.findByFirstNameContainingIgnoreCase(firstName, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Search candidates by last name
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> searchCandidatesByLastName(String lastName, Pageable pageable) {
        return candidateRepository.findByLastNameContainingIgnoreCase(lastName, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Search candidates by full name
    @Cacheable(value = "candidates", key = "#fullName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> searchCandidatesByFullName(String fullName, Pageable pageable) {
        return candidateRepository.findByFullNameContainingIgnoreCase(fullName, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Get candidates by email domain
    @Cacheable(value = "candidates", key = "#domain + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesByEmailDomain(String domain, Pageable pageable) {
        return candidateRepository.findByEmailDomain(domain, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Get candidates created within date range
    @Cacheable(value = "candidates", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return candidateRepository.findByCreatedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Get candidates by assessment ID
    @Cacheable(value = "candidates", key = "#assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesByAssessmentId(Long assessmentId, Pageable pageable) {
        return candidateRepository.findByAssessmentId(assessmentId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Get candidates with attempts for specific assessment
    @Cacheable(value = "candidates", key = "withAttempts + ':' + #assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesWithAttemptsForAssessment(Long assessmentId, Pageable pageable) {
        return candidateRepository.findCandidatesWithAttemptsForAssessment(assessmentId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Get candidates with no attempts
    @Cacheable(value = "candidates", key = "withNoAttempts + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesWithNoAttempts(Pageable pageable) {
        return candidateRepository.findCandidatesWithNoAttempts(pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Get candidates by user and assessment
    @Cacheable(value = "candidates", key = "#userId + ':' + #assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesByUserAndAssessment(Long userId, Long assessmentId, Pageable pageable) {
        return candidateRepository.findByUserIdAndAssessmentId(userId, assessmentId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Count candidates by user
    @Cacheable(value = "candidates", key = "count + ':' + #userId")
    @Transactional(readOnly = true)
    public Long countCandidatesByUser(Long userId) {
        return candidateRepository.countByUserId(userId);
    }
    
    // Get candidates with attempt count
    // @Cacheable(value = "candidates", key = "withAttemptCount + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<Object[]> getCandidatesWithAttemptCount(Pageable pageable) {
    //     return candidateRepository.findCandidatesWithAttemptCount(pageable).getContent();
    // }
    
    // Update candidate metadata
    @CachePut(value = "candidates", key = "#result.id")
    public CandidateCacheDto updateCandidateMetadata(Long id, Map<String, String> metadata) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        candidate.setMetadata(metadata);
        return new CandidateCacheDto(candidateRepository.save(candidate));
    }
    
    // Add metadata entry
    @CachePut(value = "candidates", key = "#result.id")
    public CandidateCacheDto addMetadata(Long id, String key, String value) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        if (candidate.getMetadata() == null) {
            candidate.setMetadata(Map.of(key, value));
        } else {
            candidate.getMetadata().put(key, value);
        }
        return new CandidateCacheDto(candidateRepository.save(candidate));
    }
    
    // Remove metadata entry
    @CachePut(value = "candidates", key = "#result.id")
    public CandidateCacheDto removeMetadata(Long id, String key) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        if (candidate.getMetadata() != null) {
            candidate.getMetadata().remove(key);
        }
        return new CandidateCacheDto(candidateRepository.save(candidate));
    }
}
