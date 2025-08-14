package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.specifications.CandidateAttemptSpecifications;
import com.delphi.delphi.utils.AttemptStatus;
import com.delphi.delphi.utils.CacheUtils;

@Service
@Transactional
// TODO: add cache annotations for other entity caches
/*
 * There are two different caches used here
 * 1. attempts - data about a given candidate attempt
 * 2. candidate_attempts - the list of candidate attempts for a candidate within a date range
 *    (filters like assessmentId, status, completedAfter, completedBefore are applied in memory)
 */
public class CandidateAttemptService {

    private final CandidateAttemptRepository candidateAttemptRepository;
    private final Logger log = LoggerFactory.getLogger(CandidateAttemptService.class);
    private final RedisService redisService;

    public CandidateAttemptService(CandidateAttemptRepository candidateAttemptRepository, RedisService redisService) {
        this.candidateAttemptRepository = candidateAttemptRepository;
        this.redisService = redisService;
    }

    // Invite a candidate to an assessment
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto inviteCandidate(CandidateAttempt candidateAttempt) {
        candidateAttempt.setCandidate(candidateAttempt.getCandidate());
        candidateAttempt.setAssessment(candidateAttempt.getAssessment());
        candidateAttempt.setStatus(AttemptStatus.INVITED);

        candidateAttempt.setStartedDate(null);
        candidateAttempt.setCompletedDate(null);
        candidateAttempt.setEvaluatedDate(null);
        candidateAttempt.setGithubRepositoryLink(null);
        candidateAttempt.setLanguageChoice(null);
        candidateAttempt.setEvaluation(null);
        //candidateAttempt.set
        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(candidateAttempt));
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(candidateAttempt.getCandidate().getId());
        
        return result;
    }

    /**
     * Start a new candidate attempt: change status to from INVITED to STARTED
     * Note: the candidate must already have an attempt for this assessment
     * 
     * @param candidateId
     * @param assessmentId
     * @param languageChoice
     * @param status
     * @param startedDate
     * @return
     */
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto startAttempt(Long candidateId, Long assessmentId, String languageChoice) {
        // Check if candidate already has an attempt for this assessment
        Optional<CandidateAttempt> existingAttempt = candidateAttemptRepository.findByCandidateIdAndAssessmentId(
                candidateId,
                assessmentId);

        if (!existingAttempt.isPresent()) {
            throw new IllegalArgumentException("Candidate does not have an attempt for this assessment");
        }

        existingAttempt.get().setStatus(AttemptStatus.STARTED);
        existingAttempt.get().setStartedDate(LocalDateTime.now());
        if (languageChoice != null) {
            existingAttempt.get().setLanguageChoice(languageChoice);
        }

        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(existingAttempt.get()));
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(candidateId);
        
        return result;
    }

    // Create a new candidate attempt
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto startAttempt(CandidateAttempt candidateAttempt) {
        if (!candidateAttemptRepository.existsById(candidateAttempt.getId())) {
            throw new IllegalArgumentException("Candidate attempt not found with id: " + candidateAttempt.getId());
        }

        // Set default status if not provided
        if (candidateAttempt.getStatus() == null || !candidateAttempt.getStatus().equals(AttemptStatus.INVITED)) {
            log.error("Candidate attempt status is not INVITED: {}", candidateAttempt.getStatus());
            throw new IllegalArgumentException("Candidate attempt status is not INVITED: " + candidateAttempt.getStatus());
        }

        if (candidateAttempt.getStartedDate() != null) {
            log.error("Candidate attempt started date is already set: {}", candidateAttempt.getStartedDate());
            throw new IllegalArgumentException("Candidate attempt started date is already set: " + candidateAttempt.getStartedDate());
        }

        candidateAttempt.setStatus(AttemptStatus.STARTED);
        candidateAttempt.setStartedDate(LocalDateTime.now());
        if (candidateAttempt.getLanguageChoice() != null) {
            candidateAttempt.setLanguageChoice(candidateAttempt.getLanguageChoice());
        }

        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(candidateAttempt));
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(candidateAttempt.getCandidate().getId());
        
        return result;
    }

    // Get candidate attempt by ID
    @Cacheable(value = "attempts", key = "#id")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getCandidateAttemptById(Long id) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id)));
    }

    // Get candidate attempt by ID or throw exception
    @Cacheable(value = "attempts", key = "#id")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getCandidateAttemptByIdOrThrow(Long id) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id)));
    }

    // Get all candidate attempts with pagination
    // @Cacheable(value = "attempts", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<CandidateAttemptCacheDto> getAllCandidateAttempts(Pageable pageable) {
    //     return candidateAttemptRepository.findAll(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    // }

    /**
     * Get candidate attempts with filters using a new caching strategy:
     * - Cache keys only contain candidate ID and date range (startedAfter, startedBefore)
     * - Other filters (assessmentId, status, completedAfter, completedBefore) are applied in memory
     * - This reduces cache key proliferation and improves cache hit rates
     * 
     * @param candidateId The candidate whose attempts to retrieve
     * @param assessmentId Filter by assessment ID (applied in memory)
     * @param status Filter by attempt status (applied in memory)
     * @param startedAfter Start date for date range filter (used in cache key)
     * @param startedBefore End date for date range filter (used in cache key)
     * @param completedAfter Filter by completion date after (applied in memory)
     * @param completedBefore Filter by completion date before (applied in memory)
     * @param pageable Pagination and sorting parameters (applied in memory)
     * @return List of filtered and paginated candidate attempts
     */
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getCandidateAttemptsWithFilters(Long candidateId, Long assessmentId, 
                                                                 AttemptStatus status, LocalDateTime startedAfter, 
                                                                 LocalDateTime startedBefore, LocalDateTime completedAfter, 
                                                                 LocalDateTime completedBefore, Pageable pageable) {
        // Generate cache key with only candidate ID and date range to reduce cache key proliferation
        String normalizedStartedAfter = CacheUtils.normalizeDateTime(startedAfter);
        String normalizedStartedBefore = CacheUtils.normalizeDateTime(startedBefore);
        String cacheKey = "cache:candidate_attempts:" + candidateId + ":" + normalizedStartedAfter + ":" + normalizedStartedBefore;
        
        // Check if cache exists
        List<CandidateAttemptCacheDto> cachedAttempts = null;
        if (redisService.hasKey(cacheKey)) {
            cachedAttempts = (List<CandidateAttemptCacheDto>) redisService.get(cacheKey);
        }
        
        // If cache doesn't exist, fetch from DB with only candidate and date filters
        if (cachedAttempts == null) {
            Specification<CandidateAttempt> spec = Specification.allOf(
                CandidateAttemptSpecifications.hasCandidateId(candidateId),
                CandidateAttemptSpecifications.startedAfter(startedAfter),
                CandidateAttemptSpecifications.startedBefore(startedBefore)
            );
            
            // Fetch all attempts for the candidate within date range (no pagination at DB level)
            cachedAttempts = candidateAttemptRepository.findAll(spec).stream()
                .map(CandidateAttemptCacheDto::new)
                .collect(Collectors.toList());
            
            // Store in cache for future requests
            redisService.set(cacheKey, cachedAttempts);
        }
        
        // Apply remaining filters in memory for better performance and flexibility
        List<CandidateAttemptCacheDto> filteredAttempts = cachedAttempts.stream()
            .filter(attempt -> {
                // Filter by assessment ID
                if (assessmentId != null && !assessmentId.equals(attempt.getAssessmentId())) {
                    return false;
                }
                
                // Filter by status
                if (status != null && attempt.getStatus() != status) {
                    return false;
                }
                
                // Filter by completion date after
                if (completedAfter != null) {
                    if (attempt.getCompletedDate() == null || 
                        attempt.getCompletedDate().isBefore(completedAfter)) {
                        return false;
                    }
                }
                
                // Filter by completion date before
                if (completedBefore != null) {
                    if (attempt.getCompletedDate() == null || 
                        attempt.getCompletedDate().isAfter(completedBefore)) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Apply sorting in memory based on Pageable sort criteria
        if (pageable.getSort().isSorted()) {
            filteredAttempts = filteredAttempts.stream()
                .sorted((a1, a2) -> {
                    for (Sort.Order order : pageable.getSort()) {
                        int comparison = 0;
                        comparison = switch (order.getProperty().toLowerCase()) {
                            case "id" -> a1.getId().compareTo(a2.getId());
                            case "status" -> (a1.getStatus() != null && a2.getStatus() != null)
                                    ? a1.getStatus().compareTo(a2.getStatus())
                                    : 0;
                            case "languagechoice" -> (a1.getLanguageChoice() != null && a2.getLanguageChoice() != null)
                                    ? a1.getLanguageChoice().compareTo(a2.getLanguageChoice())
                                    : 0;
                            case "createddate" -> (a1.getCreatedDate() != null && a2.getCreatedDate() != null)
                                    ? a1.getCreatedDate().compareTo(a2.getCreatedDate())
                                    : 0;
                            case "updateddate" -> (a1.getUpdatedDate() != null && a2.getUpdatedDate() != null)
                                    ? a1.getUpdatedDate().compareTo(a2.getUpdatedDate())
                                    : 0;
                            case "starteddate" -> (a1.getStartedDate() != null && a2.getStartedDate() != null)
                                    ? a1.getStartedDate().compareTo(a2.getStartedDate())
                                    : 0;
                            case "completeddate" -> (a1.getCompletedDate() != null && a2.getCompletedDate() != null)
                                    ? a1.getCompletedDate().compareTo(a2.getCompletedDate())
                                    : 0;
                            case "evaluateddate" -> (a1.getEvaluatedDate() != null && a2.getEvaluatedDate() != null)
                                    ? a1.getEvaluatedDate().compareTo(a2.getEvaluatedDate())
                                    : 0;
                            case "assessmentid" -> a1.getAssessmentId().compareTo(a2.getAssessmentId());
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
        int end = Math.min(start + pageable.getPageSize(), filteredAttempts.size());
        
        if (start >= filteredAttempts.size()) {
            return List.of();
        }
        
        return filteredAttempts.subList(start, end);
    }

    // Update candidate attempt
    @CachePut(value = "attempts", key = "#id")
    public CandidateAttemptCacheDto updateCandidateAttempt(Long id, CandidateAttempt attemptUpdates) {
        CandidateAttempt existingAttempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        // Update fields if provided
        if (attemptUpdates.getGithubRepositoryLink() != null) {
            existingAttempt.setGithubRepositoryLink(attemptUpdates.getGithubRepositoryLink());
        }
        if (attemptUpdates.getStatus() != null) {
            // Validate status transitions
            validateStatusTransition(existingAttempt.getStatus(), attemptUpdates.getStatus());
            existingAttempt.setStatus(attemptUpdates.getStatus());

            // Set timestamps based on status
            updateTimestampsForStatus(existingAttempt, attemptUpdates.getStatus());
        }
        if (attemptUpdates.getLanguageChoice() != null) {
            existingAttempt.setLanguageChoice(attemptUpdates.getLanguageChoice());
        }

        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(existingAttempt));
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(existingAttempt.getCandidate().getId());
        
        return result;
    }

    // Delete candidate attempt
    @CacheEvict(value = "attempts", key = "#id")
    public void deleteCandidateAttempt(Long id) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));
        
        // Get candidate ID before deletion for cache eviction
        Long candidateId = attempt.getCandidate().getId();
        
        candidateAttemptRepository.deleteById(id);
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(candidateId);
    }

    // Get attempts by candidate ID
    @Cacheable(value = "attempts", key = "#candidateId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByCandidateId(Long candidateId, Pageable pageable) {
        return candidateAttemptRepository.findByCandidateId(candidateId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment ID
    @Cacheable(value = "attempts", key = "#assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByAssessmentId(Long assessmentId, Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentId(assessmentId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by status
    @Cacheable(value = "attempts", key = "#status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByStatus(AttemptStatus status, Pageable pageable) {
        return candidateAttemptRepository.findByStatus(status, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempt by candidate and assessment
    @Cacheable(value = "attempts", key = "#candidateId + ':' + #assessmentId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getAttemptByCandidateAndAssessment(Long candidateId, Long assessmentId) {
        return candidateAttemptRepository.findByCandidateIdAndAssessmentId(candidateId, assessmentId)
                .map(CandidateAttemptCacheDto::new)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with candidate id: " + candidateId + " and assessment id: " + assessmentId));
    }

    // Get attempts by candidate and status
    @Cacheable(value = "attempts", key = "#candidateId + ':' + #status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByCandidateAndStatus(Long candidateId, AttemptStatus status,
            Pageable pageable) {
        return candidateAttemptRepository.findByCandidateIdAndStatus(candidateId, status, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment and status
    @Cacheable(value = "attempts", key = "#assessmentId + ':' + #status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status,
            Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentIdAndStatus(assessmentId, status, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by language choice
    @Cacheable(value = "attempts", key = "#languageChoice + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByLanguageChoice(String languageChoice, Pageable pageable) {
        return candidateAttemptRepository.findByLanguageChoiceIgnoreCase(languageChoice, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts created within date range
    @Cacheable(value = "attempts", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByCreatedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts started within date range
    @Cacheable(value = "attempts", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsStartedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByStartedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts submitted within date range
    @Cacheable(value = "attempts", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsSubmittedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByCompletedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get overdue attempts
    @Cacheable(value = "attempts", key = "overdue + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getOverdueAttempts(Pageable pageable) {
        return candidateAttemptRepository.findOverdueAttempts(LocalDateTime.now(), pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by user
    @Cacheable(value = "attempts", key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findByUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts with evaluation
    @Cacheable(value = "attempts", key = "withEvaluation + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
        public List<CandidateAttemptCacheDto> getAttemptsWithEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findAttemptsWithEvaluation(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get submitted attempts without evaluation
    @Cacheable(value = "attempts", key = "submittedWithoutEvaluation + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getSubmittedAttemptsWithoutEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findSubmittedAttemptsWithoutEvaluation(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Count attempts by assessment and status
    @Cacheable(value = "attempts", key = "count + ':' + #assessmentId + ':' + #status")
    @Transactional(readOnly = true)
    public Long countAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status) {
        return candidateAttemptRepository.countByAssessmentIdAndStatus(assessmentId, status);
    }

    // Count attempts by candidate
    @Cacheable(value = "attempts", key = "count + ':' + #candidateId")
    @Transactional(readOnly = true)
    public Long countAttemptsByCandidate(Long candidateId) {
        return candidateAttemptRepository.countByCandidateId(candidateId);
    }

    // Get attempt with details
    @Cacheable(value = "attempts", key = "withDetails + ':' + #attemptId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getAttemptWithDetails(Long attemptId) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + attemptId)));
    }

    // Get recent attempts by user
    @Cacheable(value = "attempts", key = "recent + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getRecentAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findRecentAttemptsByUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment user
    @Cacheable(value = "attempts", key = "byAssessmentUser + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByAssessmentUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Start attempt that is already created (i.e. status is INVITED)
    // public CandidateAttempt startAttempt(Long candidateAttemptId) {
    //     CandidateAttempt attempt = getCandidateAttemptByIdOrThrow(candidateAttemptId);

    //     if (attempt.getStatus() != AttemptStatus.INVITED) {
    //         throw new IllegalStateException("Only invited attempts can be started");
    //     }

    //     attempt.setStatus(AttemptStatus.STARTED);
    //     attempt.setStartedDate(LocalDateTime.now());

    //     return candidateAttemptRepository.save(attempt);
    // }

    // Submit attempt
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto submitAttempt(Long id, String githubRepositoryLink) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        if (attempt.getStatus() != AttemptStatus.STARTED) {
            throw new IllegalStateException("Only started attempts can be submitted");
        }

        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setCompletedDate(LocalDateTime.now());
        if (githubRepositoryLink != null) {
            attempt.setGithubRepositoryLink(githubRepositoryLink);
        }

        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(attempt));
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(attempt.getCandidate().getId());
        
        return result;
    }

    // Mark as evaluated
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto markAsEvaluated(Long id) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new IllegalStateException("Only completed attempts can be evaluated");
        }

        attempt.setStatus(AttemptStatus.EVALUATED);
        attempt.setEvaluatedDate(LocalDateTime.now());

        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(attempt));
        
        // Evict candidate attempts cache
        evictCandidateAttemptsCache(attempt.getCandidate().getId());
        
        return result;
    }

    // Check if attempt is overdue
    @Cacheable(value = "attempts", key = "overdue + ':' + #id")
    @Transactional(readOnly = true)
    public boolean isAttemptOverdue(Long id) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        if (attempt.getStatus() != AttemptStatus.STARTED || attempt.getStartedDate() == null) {
            return false;
        }

        LocalDateTime deadline = attempt.getStartedDate().plusMinutes(attempt.getAssessment().getDuration());
        return LocalDateTime.now().isAfter(deadline);
    }

    // Validate status transition
    private void validateStatusTransition(AttemptStatus currentStatus, AttemptStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No transition
        }

        switch (currentStatus) {
            case INVITED -> {
                if (newStatus != AttemptStatus.STARTED) {
                    throw new IllegalStateException("Can only transition from INVITED to STARTED");
                }
            }
            case STARTED -> {
                if (newStatus != AttemptStatus.COMPLETED) {
                    throw new IllegalStateException("Can only transition from STARTED to COMPLETED");
                }
            }
            case COMPLETED -> {
                if (newStatus != AttemptStatus.EVALUATED) {
                    throw new IllegalStateException("Can only transition from COMPLETED to EVALUATED");
                }
            }
            case EVALUATED -> throw new IllegalStateException("Cannot transition from EVALUATED status");
            default -> throw new IllegalStateException("Unknown status: " + currentStatus);
        }
    }

    // Update timestamps based on status
    private void updateTimestampsForStatus(CandidateAttempt attempt, AttemptStatus status) {
        
        LocalDateTime now = LocalDateTime.now();

        switch (status) {
            case STARTED -> {
                if (attempt.getStartedDate() == null) {
                    attempt.setStartedDate(now);
                }
            }
            case COMPLETED -> {
                if (attempt.getCompletedDate() == null) {
                    attempt.setCompletedDate(now);
                }
            }
            case EVALUATED -> {
                if (attempt.getEvaluatedDate() == null) {
                    attempt.setEvaluatedDate(now);
                }
            }
            default -> {
            }
        }
        // No timestamp update needed
    }

    // public void updateExpiredAttempts() {
    //     LocalDateTime now = LocalDateTime.now();
    //     candidateAttemptRepository.updateExpiredAttempts(now);
    // }

    public void updateAttemptsForInactiveAssessments() {
        candidateAttemptRepository.updateAttemptsForInactiveAssessments();
    }

    /**
     * Manual cache eviction for candidate attempts cache.
     * This method evicts all cache keys that start with "cache:candidate_attempts:{candidateId}*"
     * 
     * @param candidateId The candidate ID for which to evict attempt cache entries
     */
    private void evictCandidateAttemptsCache(Long candidateId) {
        redisService.evictCache("cache:candidate_attempts:" + candidateId + ":*");
    }

}