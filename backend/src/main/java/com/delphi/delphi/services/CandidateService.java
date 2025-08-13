package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.delphi.delphi.repositories.UserRepository;
import com.delphi.delphi.specifications.CandidateSpecifications;
import com.delphi.delphi.utils.AttemptStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.components.RedisService;

@Service
@Transactional
// TODO: add cache annotations for other entity caches

/*
 * There are two different caches used here
 * 1. candidates - all candidates
 * 2. user_candidates - the list of candidates for a user
 */
public class CandidateService {

    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(CandidateService.class);
    private final CandidateRepository candidateRepository;

    private final RedisService redisService;
    
    public CandidateService(CandidateRepository candidateRepository, UserRepository userRepository, RedisService redisService) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.redisService = redisService;
    }
    
    // Create a new candidate
    @Caching(put = {
        @CachePut(value = "candidates", key = "#result.id"),
        @CachePut(value = "candidates", key = "#result.email")
    })
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

        addCandidateToUserCandidatesCache(candidate.getUser().getId(), candidate);
        
        return new CandidateCacheDto(candidateRepository.save(candidate));
    }

    // @Caching(put = {
    //     @CachePut(value = "candidates", key = "#result.id"),
    //     @CachePut(value = "candidates", key = "#result.email")
    // })
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
    
    // Get candidate by email
    @Cacheable(value = "candidates", key = "#email")
    @Transactional(readOnly = true)
    public CandidateCacheDto getCandidateByEmail(String email) {
        return new CandidateCacheDto(candidateRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with email: " + email)));
    }
    
    // Get all candidates with pagination
    // @Cacheable(value = "user_candidates", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getAllCandidates(Pageable pageable) {
    //     return candidateRepository.findAll(pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Get candidates with multiple filters
    // @Cacheable(value = "candidates", key = "#assessmentId + ':' + #attemptStatus + ':' + #emailDomain + ':' + #firstName + ':' + #lastName + ':' + #createdAfter + ':' + #createdBefore + ':' + #attemptCompletedAfter + ':' + #attemptCompletedBefore + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesWithFilters(Long assessmentId, AttemptStatus attemptStatus, 
    //                                                String emailDomain, String firstName, String lastName,
    //                                                LocalDateTime createdAfter, LocalDateTime createdBefore,
    //                                                LocalDateTime attemptCompletedAfter, LocalDateTime attemptCompletedBefore, 
    //                                                Pageable pageable) {
    //     return candidateRepository.findWithFilters(assessmentId, attemptStatus, emailDomain, firstName, lastName,
    //                                               createdAfter, createdBefore, attemptCompletedAfter, attemptCompletedBefore, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Get candidates with multiple filters for a specific user
    @Cacheable(value = "user_candidates", key = "#userId + ':' + #assessmentId + ':' + #attemptStatus + ':' + #emailDomain + ':' + #firstName + ':' + #lastName + ':' + #createdAfter + ':' + #createdBefore + ':' + #attemptCompletedAfter + ':' + #attemptCompletedBefore + ':' + #pageable.pageNumber + ':' + #pageable.pageSize", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesWithFiltersForUser(Long userId, Long assessmentId, AttemptStatus attemptStatus, 
                                                          String emailDomain, String firstName, String lastName,
                                                          LocalDateTime createdAfter, LocalDateTime createdBefore,
                                                          LocalDateTime attemptCompletedAfter, LocalDateTime attemptCompletedBefore, 
                                                          Pageable pageable) {
        log.info("getCandidatesWithFiltersForUser: userId={}, assessmentId={}, attemptStatus={}, emailDomain={}, firstName={}, lastName={}, createdAfter={}, createdBefore={}, attemptCompletedAfter={}, attemptCompletedBefore={}, pageable={}", userId, assessmentId, attemptStatus, emailDomain, firstName, lastName, createdAfter, createdBefore, attemptCompletedAfter, attemptCompletedBefore, pageable);

        Specification<Candidate> spec = Specification.allOf(
            CandidateSpecifications.belongsToUser(userId),
            CandidateSpecifications.hasAssessmentId(assessmentId),
            CandidateSpecifications.hasAttemptStatus(attemptStatus),
            CandidateSpecifications.createdAfter(createdAfter),
            CandidateSpecifications.createdBefore(createdBefore)
        );

        // Get candidates with non-string filters first
        List<Candidate> candidates = candidateRepository.findAll(spec, pageable).getContent();
        
        return candidates.stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    }
    
    // Update candidate
    @Caching(put = {
        @CachePut(value = "candidates", key = "#result.id"),
        @CachePut(value = "candidates", key = "#result.email")
    })
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

        // evict user_candidates cache manually
        evictUserCandidatesCache(existingCandidate.getUser().getId());
        
        return new CandidateCacheDto(candidateRepository.save(existingCandidate));
    }
    
    // Delete candidate
    @Caching(evict = {
        @CacheEvict(value = "candidates", key = "#id")
    })
    public void deleteCandidate(Long id) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        
        // Get user ID before deletion for cache eviction
        Long userId = null;
        if (candidate.getUser() != null && candidate.getUser().getId() != null) {
            userId = candidate.getUser().getId();
        }
        
        candidateRepository.deleteById(id);
        
        // evict user_candidates cache manually if user was associated
        if (userId != null) {
            evictUserCandidatesCache(userId);
        }
    }
    
    // Check if email exists
    @Cacheable(value = "candidates", key = "emailExists + ':' + #email")
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return candidateRepository.existsByEmail(email);
    }
    
    // Get candidates by user ID
    // @Cacheable(value = "candidates", key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByUserId(Long userId, Pageable pageable) {
    //     return candidateRepository.findByUserId(userId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // Search candidates by first name
    // @Cacheable(value = "candidates", key = "#firstName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> searchCandidatesByFirstName(String firstName, Pageable pageable) {
    //     return candidateRepository.findByFirstNameContainingIgnoreCase(firstName, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // // Search candidates by last name
    // @Cacheable(value = "candidates", key = "#lastName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> searchCandidatesByLastName(String lastName, Pageable pageable) {
    //     return candidateRepository.findByLastNameContainingIgnoreCase(lastName, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // // Search candidates by full name
    // @Cacheable(value = "candidates", key = "#fullName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> searchCandidatesByFullName(String fullName, Pageable pageable) {
    //     return candidateRepository.findByFullNameContainingIgnoreCase(fullName, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // Get candidates by email domain
    // @Cacheable(value = "candidates", key = "#domain + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByEmailDomain(String domain, Pageable pageable) {
    //     return candidateRepository.findByEmailDomain(domain, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // // Get candidates created within date range
    // @Cacheable(value = "candidates", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    //     return candidateRepository.findByCreatedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // // Get candidates by assessment ID
    // @Cacheable(value = "candidates", key = "#assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByAssessmentId(Long assessmentId, Pageable pageable) {
    //     return candidateRepository.findByAssessmentId(assessmentId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // Get candidates with attempts for specific assessment
    // @Cacheable(value = "candidates", key = "withAttempts + ':' + #assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesWithAttemptsForAssessment(Long assessmentId, Pageable pageable) {
    //     return candidateRepository.findCandidatesWithAttemptsForAssessment(assessmentId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // Get candidates with no attempts
    // @Cacheable(value = "candidates", key = "withNoAttempts + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesWithNoAttempts(Pageable pageable) {
    //     return candidateRepository.findCandidatesWithNoAttempts(pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
    // // Get candidates by user and assessment
    // @Cacheable(value = "candidates", key = "#userId + ':' + #assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByUserAndAssessment(Long userId, Long assessmentId, Pageable pageable) {
    //     return candidateRepository.findByUserIdAndAssessmentId(userId, assessmentId, pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }
    
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
    @Caching(put = {
        @CachePut(value = "candidates", key = "#result.id"),
        @CachePut(value = "candidates", key = "#result.email")
    })
    public CandidateCacheDto updateCandidateMetadata(Long id, Map<String, String> metadata) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        candidate.setMetadata(metadata);

        // evict user_candidates cache manually
        evictUserCandidatesCache(candidate.getUser().getId());
        
        return new CandidateCacheDto(candidateRepository.save(candidate));
    }
    
    // Add metadata entry
    @Caching(put = {
        @CachePut(value = "candidates", key = "#result.id"),
        @CachePut(value = "candidates", key = "#result.email")
    })
    public CandidateCacheDto addMetadata(Long id, String key, String value) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        if (candidate.getMetadata() == null) {
            candidate.setMetadata(Map.of(key, value));
        } else {
            candidate.getMetadata().put(key, value);
        }

        // evict user_candidates cache manually
        evictUserCandidatesCache(candidate.getUser().getId());

        return new CandidateCacheDto(candidateRepository.save(candidate));
    }
    
    // Remove metadata entry
    @Caching(put = {
        @CachePut(value = "candidates", key = "#result.id"),
        @CachePut(value = "candidates", key = "#result.email")
    })
    public CandidateCacheDto removeMetadata(Long id, String key) {
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));
        if (candidate.getMetadata() != null) {
            candidate.getMetadata().remove(key);
        }
        
        // evict user_candidates cache manually
        evictUserCandidatesCache(candidate.getUser().getId());

        return new CandidateCacheDto(candidateRepository.save(candidate));
    }
    
    /**
     * Manual cache eviction for user candidates cache.
     * This method evicts all cache keys that start with "cache:user_candidates:{userId}*"
     * 
     * @param userId The user ID for which to evict candidate cache entries
     */
    private void evictUserCandidatesCache(Long userId) {
        redisService.evictCache("cache:user_candidates:" + userId + "*");
    }

    /**
     * Add a candidate to the list corresponding to the user's candidates cache key(s)
     * 
     * @param userId The user ID for which to add the candidate to the cache
     * @param candidate The candidate to add to the cache
     */
    private void addCandidateToUserCandidatesCache(Long userId, Candidate candidate) {
        CandidateCacheDto candidateCacheDto = new CandidateCacheDto(candidate);
        redisService.rightPush("cache:user_candidates:" + userId + "*", candidateCacheDto);
    }

}
