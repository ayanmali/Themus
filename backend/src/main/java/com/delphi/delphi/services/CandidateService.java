package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.repositories.UserRepository;
import com.delphi.delphi.specifications.CandidateSpecifications;
import com.delphi.delphi.utils.CacheUtils;

@Service
@Transactional
// TODO: add cache annotations for other entity caches

/*
 * There are two different caches used here
 * 1. candidates - all candidates
 * 2. user_candidates - the list of candidates for a user within a date range
 *    (filters like assessmentId, attemptStatus, firstName, lastName, emailDomain are applied in memory)
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

        evictUserCandidatesCache(candidate.getUser().getId());
        
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

    /**
     * Get candidates with filters using a new caching strategy:
     * - Cache keys only contain user ID and date range (createdAfter, createdBefore)
     * - Other filters (assessmentId, attemptStatus, firstName, lastName, emailDomain, attemptCompletedAfter, attemptCompletedBefore) are applied in memory
     * - This reduces cache key proliferation and improves cache hit rates
     * 
     * @param userId The user whose candidates to retrieve
     * @param assessmentId Filter by assessment ID (applied in memory)
     * @param attemptStatus Filter by attempt status (applied in memory)
     * @param emailDomain Filter by email domain (applied in memory)
     * @param firstName Filter by first name (applied in memory)
     * @param lastName Filter by last name (applied in memory)
     * @param createdAfter Start date for date range filter (used in cache key)
     * @param createdBefore End date for date range filter (used in cache key)
     * @param attemptCompletedAfter Filter by attempt completion date after (applied in memory)
     * @param attemptCompletedBefore Filter by attempt completion date before (applied in memory)
     * @param pageable Pagination and sorting parameters (applied in memory)
     * @return List of filtered and paginated candidates
     */
    @Transactional(readOnly = true)
    public List<CandidateCacheDto> getCandidatesWithFiltersForUser(Long userId, Long assessmentId, 
                                                          String emailDomain, String firstName, String lastName,
                                                          LocalDateTime createdAfter, LocalDateTime createdBefore,
                                                          Pageable pageable) {
        log.info("getCandidatesWithFiltersForUser: userId={}, assessmentId={}, emailDomain={}, firstName={}, lastName={}, createdAfter={}, createdBefore={}, pageable={}", userId, assessmentId, emailDomain, firstName, lastName, createdAfter, createdBefore, pageable);

        // Generate cache key with only user ID and date range to reduce cache key proliferation
        String normalizedCreatedAfter = CacheUtils.normalizeDateTime(createdAfter);
        String normalizedCreatedBefore = CacheUtils.normalizeDateTime(createdBefore);
        String cacheKey = "cache:user_candidates:" + userId + ":" + normalizedCreatedAfter + ":" + normalizedCreatedBefore;
        
        // Check if cache exists
        List<CandidateCacheDto> cachedCandidates = null;
        if (redisService.hasKey(cacheKey)) {
            cachedCandidates = (List<CandidateCacheDto>) redisService.get(cacheKey);
        }
        
        // If cache doesn't exist, fetch from DB with only user and date filters
        if (cachedCandidates == null) {
            Specification<Candidate> spec = Specification.allOf(
                CandidateSpecifications.belongsToUser(userId),
                CandidateSpecifications.createdAfter(createdAfter),
                CandidateSpecifications.createdBefore(createdBefore)
            );
            
            // Fetch all candidates for the user within date range (no pagination at DB level)
            cachedCandidates = candidateRepository.findAll(spec).stream()
                .map(CandidateCacheDto::new)
                .collect(Collectors.toList());
            
            // Store in cache for future requests
            redisService.set(cacheKey, cachedCandidates);
        }
        
        // Apply remaining filters in memory for better performance and flexibility
        List<CandidateCacheDto> filteredCandidates = cachedCandidates.stream()
            .filter(candidate -> {
                // Filter by first name (case-insensitive contains)
                if (firstName != null && !firstName.trim().isEmpty()) {
                    if (candidate.getFirstName() == null || 
                        !candidate.getFirstName().toLowerCase().contains(firstName.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by last name (case-insensitive contains)
                if (lastName != null && !lastName.trim().isEmpty()) {
                    if (candidate.getLastName() == null || 
                        !candidate.getLastName().toLowerCase().contains(lastName.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by email domain
                if (emailDomain != null && !emailDomain.trim().isEmpty()) {
                    if (candidate.getEmail() == null || 
                        !candidate.getEmail().toLowerCase().contains("@" + emailDomain.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by assessment ID
                if (assessmentId != null) {
                    if (candidate.getAssessmentIds() == null || 
                        !candidate.getAssessmentIds().contains(assessmentId)) {
                        return false;
                    }
                }
                
                // For attempt-related filters, we need to fetch the actual candidate attempts
                // since CandidateCacheDto only contains IDs
                // if (attemptStatus != null || attemptCompletedAfter != null || attemptCompletedBefore != null) {
                //     // Fetch candidate attempts for this candidate to apply attempt-related filters
                //     // This is a limitation of the current CandidateCacheDto structure
                //     // In a production environment, you might want to enhance CandidateCacheDto
                //     // to include more detailed attempt information for in-memory filtering
                //     // Note: This approach may impact performance for large datasets as it requires
                //     // additional database queries. Consider enhancing CandidateCacheDto to include
                //     // attempt details for better in-memory filtering performance.
                //     try {
                //         Candidate fullCandidate = candidateRepository.findById(candidate.getId()).orElse(null);
                //         if (fullCandidate == null || fullCandidate.getCandidateAttempts() == null) {
                //             return false;
                //         }
                        
                //         // Check if any attempt matches the criteria
                //         boolean hasMatchingAttempt = fullCandidate.getCandidateAttempts().stream()
                //             .anyMatch(attempt -> {
                //                 // Filter by attempt status
                //                 if (attemptStatus != null && attempt.getStatus() != attemptStatus) {
                //                     return false;
                //                 }
                                
                //                 // Filter by attempt completion date after
                //                 if (attemptCompletedAfter != null && 
                //                     (attempt.getCompletedDate() == null || 
                //                      attempt.getCompletedDate().isBefore(attemptCompletedAfter))) {
                //                     return false;
                //                 }
                                
                //                 // Filter by attempt completion date before
                //                 if (attemptCompletedBefore != null && 
                //                     (attempt.getCompletedDate() == null || 
                //                      attempt.getCompletedDate().isAfter(attemptCompletedBefore))) {
                //                     return false;
                //                 }
                                
                //                 return true;
                //             });
                        
                //         if (!hasMatchingAttempt) {
                //             return false;
                //         }
                //     } catch (Exception e) {
                //         log.warn("Error fetching candidate attempts for candidate {}: {}", candidate.getId(), e.getMessage());
                //         return false;
                //     }
                // }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Apply sorting in memory based on Pageable sort criteria
        if (pageable.getSort().isSorted()) {
            filteredCandidates = filteredCandidates.stream()
                .sorted((c1, c2) -> {
                    for (Sort.Order order : pageable.getSort()) {
                        int comparison = 0;
                        comparison = switch (order.getProperty().toLowerCase()) {
                            case "id" -> c1.getId().compareTo(c2.getId());
                            case "firstname" -> (c1.getFirstName() != null && c2.getFirstName() != null)
                                    ? c1.getFirstName().compareTo(c2.getFirstName())
                                    : 0;
                            case "lastname" -> (c1.getLastName() != null && c2.getLastName() != null)
                                    ? c1.getLastName().compareTo(c2.getLastName())
                                    : 0;
                            case "email" -> (c1.getEmail() != null && c2.getEmail() != null)
                                    ? c1.getEmail().compareTo(c2.getEmail())
                                    : 0;
                            case "createddate" -> (c1.getCreatedDate() != null && c2.getCreatedDate() != null)
                                    ? c1.getCreatedDate().compareTo(c2.getCreatedDate())
                                    : 0;
                            case "updateddate" -> (c1.getUpdatedDate() != null && c2.getUpdatedDate() != null)
                                    ? c1.getUpdatedDate().compareTo(c2.getUpdatedDate())
                                    : 0;
                            default -> 0;
                        };
                        if (comparison != 0) {
                            return order.isAscending() ? comparison : -comparison;
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());
        }
        
        // Apply pagination in memory
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredCandidates.size());
        
        if (start >= filteredCandidates.size()) {
            return List.of();
        }
        
        return filteredCandidates.subList(start, end);
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
        redisService.evictCache("cache:user_candidates:" + userId + ":*");
    }

}
