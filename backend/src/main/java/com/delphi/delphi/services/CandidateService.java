package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.delphi.delphi.dtos.PaginatedResponseDto;
import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.repositories.UserRepository;
import com.delphi.delphi.specifications.CandidateSpecifications;
import com.delphi.delphi.utils.CacheUtils;
import com.delphi.delphi.utils.enums.AttemptStatus;
import com.delphi.delphi.utils.exceptions.CandidateNotFoundException;

@Service
@Transactional
// TODO: add cache annotations for other entity caches

/*
 * There are two different caches used here
 * 1. candidates - all candidates
 * 2. user_candidates - the list of candidates for a user
 * 3. assessment_candidates - the list of candidates for an assessment
 * (filters like assessmentId, attemptStatus, firstName, lastName, emailDomain
 * are applied in memory)
 */
public class CandidateService {

    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(CandidateService.class);
    private final CandidateRepository candidateRepository;

    private final RedisService redisService;

    public CandidateService(CandidateRepository candidateRepository, UserRepository userRepository,
            RedisService redisService) {
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
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found with id: " + candidate.getUser().getId()));
            candidate.setUser(user);
        }

        CandidateCacheDto savedCandidate = new CandidateCacheDto(candidateRepository.save(candidate));
        
        // Update cache: add to general cache and evict specific caches
        updateCacheAfterCandidateCreation(candidate.getUser().getId(), savedCandidate);
        
        return savedCandidate;
    }

    // @Caching(put = {
    // @CachePut(value = "candidates", key = "#result.id"),
    // @CachePut(value = "candidates", key = "#result.email")
    // })
    public CandidateCacheDto createCandidate(String firstName, String lastName, String email, User user, Map<String, String> metadata) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setEmail(email);
        candidate.setUser(user);
        candidate.setMetadata(metadata);
        return createCandidate(candidate);
    }

    // Get candidate by ID
    @Cacheable(value = "candidates", key = "#id")
    @Transactional(readOnly = true)
    public CandidateCacheDto getCandidateById(Long id) {
        return new CandidateCacheDto(candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with id: " + id)));
    }

    // Get candidate by email
    @Cacheable(value = "candidates", key = "#email")
    @Transactional(readOnly = true)
    public CandidateCacheDto getCandidateByEmail(String email) {
        return new CandidateCacheDto(candidateRepository.findByEmail(email)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found with email: " + email)));
    }

    // Get all candidates with pagination
    // @Cacheable(value = "user_candidates", key = "#pageable.pageNumber + ':' +
    // #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getAllCandidates(Pageable pageable) {
    // return
    // candidateRepository.findAll(pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Get candidates with multiple filters
    // @Cacheable(value = "candidates", key = "#assessmentId + ':' + #attemptStatus
    // + ':' + #emailDomain + ':' + #firstName + ':' + #lastName + ':' +
    // #createdAfter + ':' + #createdBefore + ':' + #attemptCompletedAfter + ':' +
    // #attemptCompletedBefore + ':' + #pageable.pageNumber + ':' +
    // #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesWithFilters(Long assessmentId,
    // AttemptStatus attemptStatus,
    // String emailDomain, String firstName, String lastName,
    // LocalDateTime createdAfter, LocalDateTime createdBefore,
    // LocalDateTime attemptCompletedAfter, LocalDateTime attemptCompletedBefore,
    // Pageable pageable) {
    // return candidateRepository.findWithFilters(assessmentId, attemptStatus,
    // emailDomain, firstName, lastName,
    // createdAfter, createdBefore, attemptCompletedAfter, attemptCompletedBefore,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    /**
     * Get candidates with filters using a new caching strategy:
     * - Cache keys only contain user ID and date range (createdAfter,
     * createdBefore)
     * - Other filters (assessmentId, attemptStatuses, attemptCompletedAfter, attemptCompletedBefore) are applied in
     * memory
     * - This reduces cache key proliferation and improves cache hit rates
     * 
     * @param userId                 The user whose candidates to retrieve
     * @param assessmentId           Filter by assessment ID
     * @param attemptStatuses          Filter by attempt status
     * @param createdAfter           Start date for date range filter
     * @param createdBefore          End date for date range filter 
     * @param attemptCompletedAfter  Filter by attempt completion date after
     * @param attemptCompletedBefore Filter by attempt completion date before
     * @param pageable               Pagination and sorting parameters
     * @return PaginatedResponseDto containing filtered candidates and pagination
     *         metadata
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDto<CandidateCacheDto> getCandidatesWithFiltersForUser(Long userId, Long assessmentId,
            List<AttemptStatus> attemptStatuses,
            LocalDateTime createdAfter, LocalDateTime createdBefore,
            Pageable pageable) {
        
        // Check if no filters are applied (only user filter)
        boolean hasFilters = assessmentId != null || (attemptStatuses != null && !attemptStatuses.isEmpty()) || 
                           createdAfter != null || createdBefore != null;
        
        if (!hasFilters) {
            // No filters - check general cache or fetch from DB
            return getGeneralUserCandidates(userId, pageable);
        }
        
        // Generate specific cache key for filtered query
        String specificCacheKey = generateSpecificCacheKey(userId, assessmentId, attemptStatuses, 
                                                          createdAfter, createdBefore);
        
        // Try to get from specific cache first
        List<CandidateCacheDto> cachedSpecificResult = getCachedCandidateList(specificCacheKey);
        if (cachedSpecificResult != null) {
            return applyPaginationToList(cachedSpecificResult, pageable);
        }
        
        // Try to get from general cache and apply filters in memory
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<CandidateCacheDto> cachedGeneralResult = getCachedCandidateList(generalCacheKey);
        if (cachedGeneralResult != null) {
            List<CandidateCacheDto> filteredResult = applyFiltersInMemory(cachedGeneralResult, assessmentId, 
                    attemptStatuses, createdAfter, createdBefore);
            
            // Cache the filtered result for future use
            cacheCandidateList(specificCacheKey, filteredResult);
            
            return applyPaginationToList(filteredResult, pageable);
        }
        
        // No cache hit - fetch from database with all filters
        return fetchFromDatabaseWithFilters(userId, assessmentId, attemptStatuses, 
                                          createdAfter, createdBefore, pageable, specificCacheKey);
    }

    /**
     * Get candidates who are NOT in a specific assessment (available candidates for adding to assessment)
     * 
     * @param userId                 The user whose candidates to retrieve
     * @param excludeAssessmentId    Assessment ID to exclude (candidates NOT in this assessment)
     * @param createdAfter           Start date for date range filter (used in cache key)
     * @param createdBefore          End date for date range filter (used in cache key)
     * @param pageable               Pagination and sorting parameters (applied in memory)
     * @return PaginatedResponseDto containing filtered candidates and pagination metadata
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDto<CandidateCacheDto> getAvailableCandidatesForAssessment(Long userId, Long excludeAssessmentId,
            LocalDateTime createdAfter, LocalDateTime createdBefore,
            Pageable pageable) {
        log.info(
                "getAvailableCandidatesForAssessment: userId={}, excludeAssessmentId={}, createdAfter={}, createdBefore={}, pageable={}",
                userId, excludeAssessmentId, createdAfter, createdBefore, pageable);

        // Generate cache key with only user ID and date range to reduce cache key proliferation
        String normalizedCreatedAfter = CacheUtils.normalizeDateTime(createdAfter);
        String normalizedCreatedBefore = CacheUtils.normalizeDateTime(createdBefore);
        String cacheKey = "cache:user_candidates:" + userId + ":" + normalizedCreatedAfter + ":"
                + normalizedCreatedBefore;

        // Check if cache exists
        List<CandidateCacheDto> cachedCandidates = getCachedCandidateList(cacheKey);

        // If cache doesn't exist, fetch from DB with only user and date filters
        if (cachedCandidates == null) {
            Specification<Candidate> spec = Specification.allOf(
                    CandidateSpecifications.belongsToUser(userId),
                    CandidateSpecifications.createdAfter(createdAfter),
                    CandidateSpecifications.createdBefore(createdBefore));

            // Fetch all candidates for the user within date range (no pagination at DB level)
            cachedCandidates = candidateRepository.findAll(spec).stream()
                    .map(CandidateCacheDto::new)
                    .collect(Collectors.toList());

            // Store in cache for future requests
            cacheCandidateList(cacheKey, cachedCandidates);
        }

        // Apply exclude assessment filter in memory
        List<CandidateCacheDto> filteredCandidates = cachedCandidates.stream()
                .filter(candidate -> {
                    // Filter out candidates who are already in the assessment
                    if (excludeAssessmentId != null) {
                        if (candidate.getAssessmentIds() != null &&
                                candidate.getAssessmentIds().contains(excludeAssessmentId)) {
                            return false;
                        }
                    }
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

        // Calculate pagination metadata
        long totalElements = filteredCandidates.size();

        // Apply pagination in memory
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredCandidates.size());

        List<CandidateCacheDto> paginatedCandidates;
        if (start >= filteredCandidates.size()) {
            paginatedCandidates = List.of();
        } else {
            paginatedCandidates = filteredCandidates.subList(start, end);
        }

        // Return paginated response with metadata
        return new PaginatedResponseDto<>(
                paginatedCandidates,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                totalElements);
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

        CandidateCacheDto updatedCandidate = new CandidateCacheDto(candidateRepository.save(existingCandidate));
        
        // Update cache: update general cache and evict specific caches
        updateCacheAfterCandidateUpdate(existingCandidate.getUser().getId(), updatedCandidate);
        
        return updatedCandidate;
    }

    // Delete candidate
    @Caching(evict = {
            @CacheEvict(value = "candidates", key = "#id")
    })
    public void deleteCandidate(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + id));

        // Get candidate DTO and user ID before deletion for cache removal
        CandidateCacheDto candidateDto = new CandidateCacheDto(candidate);
        Long userId = null;
        if (candidate.getUser() != null && candidate.getUser().getId() != null) {
            userId = candidate.getUser().getId();
        }

        candidateRepository.deleteById(id);

        // Update cache: remove from general cache and evict specific caches
        if (userId != null) {
            updateCacheAfterCandidateDeletion(userId, candidateDto);
        }
    }

    // Check if email exists
    @Cacheable(value = "candidates", key = "'emailExists' + ':' + #email")
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return candidateRepository.existsByEmail(email);
    }

    // Get candidates by user ID
    // @Cacheable(value = "candidates", key = "#userId + ':' + #pageable.pageNumber
    // + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByUserId(Long userId, Pageable
    // pageable) {
    // return candidateRepository.findByUserId(userId,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Search candidates by first name
    // @Cacheable(value = "candidates", key = "#firstName + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> searchCandidatesByFirstName(String firstName,
    // Pageable pageable) {
    // return candidateRepository.findByFirstNameContainingIgnoreCase(firstName,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // // Search candidates by last name
    // @Cacheable(value = "candidates", key = "#lastName + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> searchCandidatesByLastName(String lastName,
    // Pageable pageable) {
    // return candidateRepository.findByLastNameContainingIgnoreCase(lastName,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // // Search candidates by full name
    // @Cacheable(value = "candidates", key = "#fullName + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> searchCandidatesByFullName(String fullName,
    // Pageable pageable) {
    // return candidateRepository.findByFullNameContainingIgnoreCase(fullName,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Get candidates by email domain
    // @Cacheable(value = "candidates", key = "#domain + ':' + #pageable.pageNumber
    // + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByEmailDomain(String domain,
    // Pageable pageable) {
    // return candidateRepository.findByEmailDomain(domain,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // // Get candidates created within date range
    // @Cacheable(value = "candidates", key = "#startDate + ':' + #endDate + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesCreatedBetween(LocalDateTime
    // startDate, LocalDateTime endDate, Pageable pageable) {
    // return candidateRepository.findByCreatedDateBetween(startDate, endDate,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // // Get candidates by assessment ID
    // @Cacheable(value = "candidates", key = "#assessmentId + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByAssessmentId(Long assessmentId,
    // Pageable pageable) {
    // return candidateRepository.findByAssessmentId(assessmentId,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Get candidates with attempts for specific assessment
    // @Cacheable(value = "candidates", key = "withAttempts + ':' + #assessmentId +
    // ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesWithAttemptsForAssessment(Long
    // assessmentId, Pageable pageable) {
    // return
    // candidateRepository.findCandidatesWithAttemptsForAssessment(assessmentId,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Get candidates with no attempts
    // @Cacheable(value = "candidates", key = "withNoAttempts + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesWithNoAttempts(Pageable pageable)
    // {
    // return
    // candidateRepository.findCandidatesWithNoAttempts(pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // // Get candidates by user and assessment
    // @Cacheable(value = "candidates", key = "#userId + ':' + #assessmentId + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateCacheDto> getCandidatesByUserAndAssessment(Long userId,
    // Long assessmentId, Pageable pageable) {
    // return candidateRepository.findByUserIdAndAssessmentId(userId, assessmentId,
    // pageable).getContent().stream().map(CandidateCacheDto::new).collect(Collectors.toList());
    // }

    // Count candidates by user
    @Cacheable(value = "candidates", key = "'count' + ':' + #userId")
    @Transactional(readOnly = true)
    public Long countCandidatesByUser(Long userId) {
        return candidateRepository.countByUserId(userId);
    }

    // Get candidates with attempt count
    // @Cacheable(value = "candidates", key = "withAttemptCount + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<Object[]> getCandidatesWithAttemptCount(Pageable pageable) {
    // return
    // candidateRepository.findCandidatesWithAttemptCount(pageable).getContent();
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

        CandidateCacheDto updatedCandidate = new CandidateCacheDto(candidateRepository.save(candidate));
        
        // Update cache: update general cache and evict specific caches
        updateCacheAfterCandidateUpdate(candidate.getUser().getId(), updatedCandidate);
        
        return updatedCandidate;
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

        CandidateCacheDto updatedCandidate = new CandidateCacheDto(candidateRepository.save(candidate));
        
        // Update cache: update general cache and evict specific caches
        updateCacheAfterCandidateUpdate(candidate.getUser().getId(), updatedCandidate);
        
        return updatedCandidate;
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

        CandidateCacheDto updatedCandidate = new CandidateCacheDto(candidateRepository.save(candidate));
        
        // Update cache: update general cache and evict specific caches
        updateCacheAfterCandidateUpdate(candidate.getUser().getId(), updatedCandidate);
        
        return updatedCandidate;
    }

    /**
     * Cache key generation methods
     */
    private String generateGeneralCacheKey(Long userId) {
        return "cache:user_candidates:" + userId;
    }
    
    private String generateSpecificCacheKey(Long userId, Long assessmentId, List<AttemptStatus> attemptStatuses,
                                          LocalDateTime createdAfter, LocalDateTime createdBefore) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("cache:user_candidates:").append(userId).append(":");
        
        // Add assessment ID
        keyBuilder.append(assessmentId != null ? assessmentId.toString() : "null").append(":");
        
        // Add attempt statuses (sorted for consistent keys)
        if (attemptStatuses != null && !attemptStatuses.isEmpty()) {
            attemptStatuses.stream().sorted().forEach(status -> keyBuilder.append(status.toString()).append(","));
        } else {
            keyBuilder.append("null");
        }
        keyBuilder.append(":");
        
        // Add date filters
        keyBuilder.append(createdAfter != null ? createdAfter.toString() : "null").append(":");
        keyBuilder.append(createdBefore != null ? createdBefore.toString() : "null");
        
        return keyBuilder.toString();
    }
    
    /**
     * Cache operations helper methods
     */
    @SuppressWarnings("unchecked")
    private List<CandidateCacheDto> getCachedCandidateList(String cacheKey) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached instanceof List<?>) {
                return (List<CandidateCacheDto>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached candidate list for key: {}, error: {}", cacheKey, e.getMessage());
        }
        return null;
    }
    
    private void cacheCandidateList(String cacheKey, List<CandidateCacheDto> candidates) {
        try {
            // Cache for 30 minutes (matching the candidates cache configuration)
            redisService.setWithExpiration(cacheKey, candidates, 30, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache candidate list for key: {}, error: {}", cacheKey, e.getMessage());
        }
    }
    
    private PaginatedResponseDto<CandidateCacheDto> getGeneralUserCandidates(Long userId, Pageable pageable) {
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<CandidateCacheDto> cachedResult = getCachedCandidateList(generalCacheKey);
        
        if (cachedResult != null) {
            return applyPaginationToList(cachedResult, pageable);
        }
        
        // Fetch from database with only user filter
        Specification<Candidate> spec = CandidateSpecifications.belongsToUser(userId);
        List<Candidate> candidates = candidateRepository.findAll(spec);
        List<CandidateCacheDto> candidateDtos = candidates.stream()
                .map(CandidateCacheDto::new)
                .collect(Collectors.toList());
        
        // Cache the general result
        cacheCandidateList(generalCacheKey, candidateDtos);
        
        return applyPaginationToList(candidateDtos, pageable);
    }
    
    private PaginatedResponseDto<CandidateCacheDto> fetchFromDatabaseWithFilters(Long userId, Long assessmentId,
            List<AttemptStatus> attemptStatuses, LocalDateTime createdAfter, LocalDateTime createdBefore,
            Pageable pageable, String specificCacheKey) {
        
        // Build specification with all filters
        Specification<Candidate> spec = CandidateSpecifications.belongsToUser(userId);
        
        if (assessmentId != null) {
            spec = spec.and(CandidateSpecifications.hasAssessmentId(assessmentId));
        }
        if (attemptStatuses != null && !attemptStatuses.isEmpty()) {
            spec = spec.and(CandidateSpecifications.hasAnyAttemptStatus(attemptStatuses));
        }
        if (createdAfter != null) {
            spec = spec.and(CandidateSpecifications.createdAfter(createdAfter));
        }
        if (createdBefore != null) {
            spec = spec.and(CandidateSpecifications.createdBefore(createdBefore));
        }
        // Note: attemptStatuses filtering would need to be added to CandidateSpecifications if needed
        
        List<Candidate> candidates = candidateRepository.findAll(spec);
        List<CandidateCacheDto> candidateDtos = candidates.stream()
                .map(CandidateCacheDto::new)
                .collect(Collectors.toList());
        
        // Apply attempt status filtering in memory since it's complex to do in JPA
        if (attemptStatuses != null && !attemptStatuses.isEmpty()) {
            candidateDtos = candidateDtos.stream()
                    .filter(candidate -> candidate.getAssessmentStatuses() != null && 
                           attemptStatuses.stream().anyMatch(status -> 
                               candidate.getAssessmentStatuses().containsKey(status)))
                    .collect(Collectors.toList());
        }
        
        // Cache the specific filtered result
        cacheCandidateList(specificCacheKey, candidateDtos);
        
        return applyPaginationToList(candidateDtos, pageable);
    }
    
    /**
     * In-memory filtering and pagination helper methods
     */
    private List<CandidateCacheDto> applyFiltersInMemory(List<CandidateCacheDto> candidates, Long assessmentId,
            List<AttemptStatus> attemptStatuses, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        
        return candidates.stream()
                .filter(candidate -> assessmentId == null || 
                        (candidate.getAssessmentIds() != null && candidate.getAssessmentIds().contains(assessmentId)))
                .filter(candidate -> attemptStatuses == null || attemptStatuses.isEmpty() ||
                        (candidate.getAssessmentStatuses() != null && 
                         attemptStatuses.stream().anyMatch(status -> candidate.getAssessmentStatuses().containsKey(status))))
                .filter(candidate -> createdAfter == null || 
                        (candidate.getCreatedDate() != null && candidate.getCreatedDate().isAfter(createdAfter)))
                .filter(candidate -> createdBefore == null || 
                        (candidate.getCreatedDate() != null && candidate.getCreatedDate().isBefore(createdBefore)))
                .collect(Collectors.toList());
    }
    
    private PaginatedResponseDto<CandidateCacheDto> applyPaginationToList(List<CandidateCacheDto> candidates, Pageable pageable) {
        // Apply sorting
        List<CandidateCacheDto> sortedCandidates = new ArrayList<>(candidates);
        if (pageable.getSort().isSorted()) {
            sortedCandidates.sort((c1, c2) -> {
                for (Sort.Order order : pageable.getSort()) {
                    int comparison = compareCandidatesByField(c1, c2, order.getProperty());
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedCandidates.size());
        
        List<CandidateCacheDto> pageContent = start >= sortedCandidates.size() ? 
                List.of() : sortedCandidates.subList(start, end);
        
        return new PaginatedResponseDto<>(
                pageContent,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortedCandidates.size()
        );
    }
    
    private int compareCandidatesByField(CandidateCacheDto c1, CandidateCacheDto c2, String field) {
        return switch (field.toLowerCase()) {
            case "id" -> compareNullable(c1.getId(), c2.getId());
            case "firstname" -> compareNullable(c1.getFirstName(), c2.getFirstName());
            case "lastname" -> compareNullable(c1.getLastName(), c2.getLastName());
            case "email" -> compareNullable(c1.getEmail(), c2.getEmail());
            case "createddate" -> compareNullable(c1.getCreatedDate(), c2.getCreatedDate());
            case "updateddate" -> compareNullable(c1.getUpdatedDate(), c2.getUpdatedDate());
            default -> 0;
        };
    }
    
    //@SuppressWarnings("unchecked")
    private <T extends Comparable<T>> int compareNullable(T a, T b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }
    
    /**
     * Cache update methods for CRUD operations
     */
    private void updateCacheAfterCandidateCreation(Long userId, CandidateCacheDto newCandidate) {
        // Add to general cache if it exists
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<CandidateCacheDto> cachedCandidates = getCachedCandidateList(generalCacheKey);
        if (cachedCandidates != null) {
            cachedCandidates.add(newCandidate);
            cacheCandidateList(generalCacheKey, cachedCandidates);
        }
        
        // Evict all specific filter caches for this user
        evictUserCandidatesSpecificCaches(userId);
    }
    
    private void updateCacheAfterCandidateUpdate(Long userId, CandidateCacheDto updatedCandidate) {
        // Update in general cache if it exists
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<CandidateCacheDto> cachedCandidates = getCachedCandidateList(generalCacheKey);
        if (cachedCandidates != null) {
            // Find and replace the candidate
            for (int i = 0; i < cachedCandidates.size(); i++) {
                if (cachedCandidates.get(i).getId().equals(updatedCandidate.getId())) {
                    cachedCandidates.set(i, updatedCandidate);
                    break;
                }
            }
            cacheCandidateList(generalCacheKey, cachedCandidates);
        }
        
        // Evict all specific filter caches for this user
        evictUserCandidatesSpecificCaches(userId);
    }
    
    private void updateCacheAfterCandidateDeletion(Long userId, CandidateCacheDto deletedCandidate) {
        // Remove from general cache if it exists
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<CandidateCacheDto> cachedCandidates = getCachedCandidateList(generalCacheKey);
        if (cachedCandidates != null) {
            cachedCandidates.removeIf(candidate -> candidate.getId().equals(deletedCandidate.getId()));
            cacheCandidateList(generalCacheKey, cachedCandidates);
        }
        
        // Evict all specific filter caches for this user
        evictUserCandidatesSpecificCaches(userId);
    }

    private void evictUserCandidatesSpecificCaches(Long userId) {
        // Evict all specific filter caches but keep the general cache
        redisService.evictCache("cache:user_candidates:" + userId + ":*:*");
    }

}
