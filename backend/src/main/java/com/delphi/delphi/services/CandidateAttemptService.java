package com.delphi.delphi.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.AuthenticateCandidateDto;
import com.delphi.delphi.dtos.PaginatedResponseDto;
import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.specifications.CandidateAttemptSpecifications;
import com.delphi.delphi.utils.Constants;
import com.delphi.delphi.utils.enums.AttemptStatus;


@Service
@Transactional
// TODO: add cache annotations for other entity caches
/*
 * There are two different caches used here
 * 1. attempts - data about a given candidate attempt
 * 2. assessment_attempts - the list of candidate attempts for an assessment
 * 3. candidate_attempts - the list of candidate attempts for a candidate
 * 4. all_attempts - the list of all attempts for all candidates
 */
public class CandidateAttemptService {

    private final EncryptionService encryptionService;

    private final CandidateAttemptRepository candidateAttemptRepository;
    private final Logger log = LoggerFactory.getLogger(CandidateAttemptService.class);
    private final RedisService redisService;
    private final String candidateAttemptPasswordCacheKeyPrefix = "candidate_attempt_password:";
    private final String githubCacheKeyPrefix = "github_install_url_random_string:";
    private final String tokenCacheKeyPrefix = "candidate_github_token:";
    private final String usernameCacheKeyPrefix = "candidate_github_username:";
    private final GithubService githubService;
    private final String appInstallBaseUrl;

    public CandidateAttemptService(CandidateAttemptRepository candidateAttemptRepository, RedisService redisService, EncryptionService encryptionService, @Value("${github.app.name}") String githubAppName, GithubService githubService) {
        this.candidateAttemptRepository = candidateAttemptRepository;
        this.redisService = redisService;
        this.encryptionService = encryptionService;
        this.appInstallBaseUrl = String.format("https://github.com/apps/%s/installations/new", githubAppName);
        this.githubService = githubService;
    }

    // Invite a candidate to an assessment
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto inviteCandidate(CandidateAttempt candidateAttempt) {
        // Check if candidate already has an attempt for this assessment
        Optional<CandidateAttempt> existingAttempt = candidateAttemptRepository.findByCandidateIdAndAssessmentId(
                candidateAttempt.getCandidate().getId(),
                candidateAttempt.getAssessment().getId());
        
        if (existingAttempt.isPresent()) {
            throw new IllegalArgumentException("Candidate already has an attempt for this assessment");
        }
        
        // Update the many-to-many relationship between candidate and assessment
        // This is needed for the available candidates filtering to work correctly
        Candidate candidate = candidateAttempt.getCandidate();
        Assessment assessment = candidateAttempt.getAssessment();
        
        if (candidate.getAssessments() == null) {
            candidate.setAssessments(new ArrayList<>());
        }
        if (!candidate.getAssessments().contains(assessment)) {
            candidate.getAssessments().add(assessment);
        }
        
        if (assessment.getCandidates() == null) {
            assessment.setCandidates(new ArrayList<>());
        }
        if (!assessment.getCandidates().contains(candidate)) {
            assessment.getCandidates().add(candidate);
        }
        
        candidateAttempt.setCandidate(candidate);
        candidateAttempt.setAssessment(assessment);
        candidateAttempt.setStatus(AttemptStatus.INVITED);

        candidateAttempt.setStartedDate(null);
        candidateAttempt.setCompletedDate(null);
        candidateAttempt.setEvaluatedDate(null);
        candidateAttempt.setGithubRepositoryLink(null);
        candidateAttempt.setLanguageChoice(null);
        candidateAttempt.setEvaluation(null);
        //candidateAttempt.set
        CandidateAttemptCacheDto result = new CandidateAttemptCacheDto(candidateAttemptRepository.save(candidateAttempt));
        
        // Update cache: add to general caches and evict specific caches
        // create password for candidate attempt
        String password = UUID.randomUUID().toString().substring(0, 6);
        String encryptedPassword = null;
        try {
            encryptedPassword = encryptionService.encrypt(password);
        } catch (Exception e) {
            log.error("Error encrypting password: {}", e.getMessage());
            throw new RuntimeException("Error encrypting password: " + e.getMessage());
        }
        redisService.set("candidate_attempt_password:" + candidateAttempt.getId(), encryptedPassword); // 1 day
        updateCacheAfterAttemptCreation(candidateAttempt.getCandidate().getId(), candidateAttempt.getAssessment().getId(), result);
        
        log.info("CANDIDATE ATTEMPT PASSWORD: {}", password);
        return result;
    }

    //@Cacheable(value = "candidate_github_install_urls", key = "#candidateEmail")
    // for candidates to generate a github install url
    public String generateGitHubInstallUrl(String candidateEmail) {
        String randomString = UUID.randomUUID().toString();
        redisService.setWithExpiration(githubCacheKeyPrefix + candidateEmail, randomString, 10, TimeUnit.MINUTES);
        String installUrl = String.format("%s?state=%s_candidate_%s", appInstallBaseUrl, randomString, candidateEmail);
        return installUrl;
    }

    @Cacheable(value = "attempts", key = "#candidateEmail + ':' + #assessmentId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getCandidateAttemptByCandidateEmailAndAssessmentId(String candidateEmail, Long assessmentId) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findByCandidateEmailAndAssessmentId(candidateEmail, assessmentId).orElseThrow(() -> new IllegalArgumentException("Candidate does not have an attempt for this assessment")));
    }

    @Cacheable(value = "attempts", key = "#candidateEmail + ':' + #assessmentId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getCandidateAttemptByCandidateIdAndAssessmentId(Long candidateId, Long assessmentId) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findByCandidateIdAndAssessmentId(candidateId, assessmentId).orElseThrow(() -> new IllegalArgumentException("Candidate does not have an attempt for this assessment")));
    }

    public boolean isCandidateConnectedToGithub(String email) {
        // Check if the candidate has a github token and username
        Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + email);
        Object candidateGithubUsername = redisService.get(usernameCacheKeyPrefix + email);

        return !(candidateGithubToken == null || candidateGithubUsername == null
                || githubService.validateGithubCredentials(candidateGithubToken.toString()) == null);
    }

    public boolean authenticateCandidate(AuthenticateCandidateDto authenticateCandidateDto) {
        CandidateAttemptCacheDto existingAttempt = getCandidateAttemptByCandidateEmailAndAssessmentId(authenticateCandidateDto.getCandidateEmail(), authenticateCandidateDto.getAssessmentId());
        
        // Check if candidate already has an attempt for this assessment
        Object encryptedPassword = redisService.get(candidateAttemptPasswordCacheKeyPrefix + existingAttempt.getId());
        if (encryptedPassword == null) {
            throw new IllegalArgumentException("Candidate attempt password not found. Have you been invited to this assessment?");
        }

        // Decrypt the password and see if there is a match
        String actualPassword;
        try {
            actualPassword = encryptionService.decrypt(encryptedPassword.toString());
        } catch (Exception e) {
            log.error("Error decrypting password: {}", e.getMessage());
            throw new RuntimeException("Error decrypting password: " + e.getMessage());
        }

        return authenticateCandidateDto.getPlainTextPassword().equals(actualPassword);
    }

    /**
     * Start a new candidate attempt: change status to from INVITED to STARTED
     * Note: the candidate must already have an attempt for this assessment
     * 
     * @param candidate The candidate attempting the assessment
     * @param assessment The assessment being attempted
     * @param languageChoice The programming language chosen by the candidate
     * @param password The password for the candidate attempt
     * @param githubUsername The GitHub username of the candidate
     * @return The updated candidate attempt
     */
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto startAttempt(CandidateCacheDto candidate, AssessmentCacheDto assessment, String owner, String languageChoice) {
        if (!assessment.getLanguageOptions().isEmpty() && !assessment.getLanguageOptions().contains(languageChoice)) {
            throw new IllegalArgumentException("Language choice not supported for this assessment");
        }

        CandidateAttemptCacheDto existingAttempt = getAttemptByCandidateAndAssessment(candidate.getId(), assessment.getId());
        log.info("--------------------------------");
        log.info("Existing attempt ID: {}", existingAttempt.getId());
        log.info("--------------------------------");

        String repoName = "assessment-" + assessment.getId() + "-" + String.valueOf(Instant.now().toEpochMilli());
        Object candidateGithubUsername = redisService.get(usernameCacheKeyPrefix + candidate.getEmail());
        if (candidateGithubUsername == null) {
            throw new IllegalArgumentException("You are not connected to Github. Please connect your Github account to start the assessment.");
        }
        String fullGithubUrl = "https://github.com/" + Constants.THEMUS_USERNAME + "/" + repoName;

        candidateAttemptRepository.updateStatus(existingAttempt.getId(), AttemptStatus.STARTED);
        candidateAttemptRepository.updateStartedDate(existingAttempt.getId(), LocalDateTime.now());
        if (languageChoice != null) {
            candidateAttemptRepository.updateLanguageChoice(existingAttempt.getId(), languageChoice);
        }
        candidateAttemptRepository.updateGithubRepositoryLink(existingAttempt.getId(), fullGithubUrl);

        // Creating candidate github repo
        String templateRepoName = assessment.getGithubRepoName();        
        Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + candidate.getEmail());
        if (candidateGithubToken == null) {
            throw new IllegalArgumentException("You are not connected to Github. Please connect your Github account to start the assessment.");
        }

        try {
            // Extract repository name from the full URL for GitHub API call
            githubService.createCandidateRepo(templateRepoName, repoName);
            // add candidate as a contributor to the repo
            githubService.addContributorToCandidateRepo(repoName, candidateGithubUsername.toString());
        } catch (Exception e) {
            log.error("Error decrypting github access token and creating repo: {}", e.getMessage());
            throw new RuntimeException("Error decrypting github access token: " + e.getMessage());
        }

        // Update cache: update general caches and evict specific caches
        updateCacheAfterAttemptUpdate(candidate.getId(), assessment.getId(), existingAttempt);
        
        return existingAttempt;
    }

    public boolean hasValidGithubToken(String candidateEmail) {
        Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + candidateEmail);
        // get a new token if the candidate doesn't have one or if the token is invalid
        try {
            if (candidateGithubToken == null || githubService
            .validateGithubCredentials(encryptionService.decrypt(candidateGithubToken.toString())) == null) {
                return false;
            }
        } catch (Exception e) {
            log.error("Error decrypting Github token - returning false: {}", e.getMessage());
            return false;
        }
        return true;
    }

    // Create a new candidate attempt
    @CachePut(value = "attempts", key = "#result.id")
    public CandidateAttemptCacheDto createAttempt(CandidateAttempt candidateAttempt) {
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
        
        // Update cache: update general caches and evict specific caches
        updateCacheAfterAttemptUpdate(candidateAttempt.getCandidate().getId(), candidateAttempt.getAssessment().getId(), result);
        
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
     * @param statuses Filter by attempt status (applied in memory)
     * @param startedAfter Start date for date range filter (used in cache key)
     * @param startedBefore End date for date range filter (used in cache key)
     * @param completedAfter Filter by completion date after (applied in memory)
     * @param completedBefore Filter by completion date before (applied in memory)
     * @param pageable Pagination and sorting parameters (applied in memory)
     * @return List of filtered and paginated candidate attempts
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDto<CandidateAttemptCacheDto> getCandidateAttemptsWithFilters(Long candidateId, Long assessmentId, 
                                                                 List<AttemptStatus> statuses, LocalDateTime startedAfter, 
                                                                 LocalDateTime startedBefore, LocalDateTime completedAfter, 
                                                                 LocalDateTime completedBefore, Pageable pageable) {
        
        // Check if no filters are applied (only candidate or assessment filter)
        boolean hasFilters = (statuses != null && !statuses.isEmpty()) || 
                           startedAfter != null || startedBefore != null || 
                           completedAfter != null || completedBefore != null;
        
        if (!hasFilters) {
            // No filters - check general cache or fetch from DB
            if (candidateId != null && assessmentId == null) {
                return getGeneralCandidateAttempts(candidateId, pageable);
            } else if (assessmentId != null && candidateId == null) {
                return getGeneralAssessmentAttempts(assessmentId, pageable);
            } else if (candidateId != null && assessmentId != null) {
                // Both specified - treat as filtered query
                return getCandidateAttemptsWithFiltersInternal(candidateId, assessmentId, statuses, 
                        startedAfter, startedBefore, completedAfter, completedBefore, pageable);
            } else {
                // Neither specified - invalid query
                throw new IllegalArgumentException("Either candidateId or assessmentId must be specified");
            }
        }
        
        return getCandidateAttemptsWithFiltersInternal(candidateId, assessmentId, statuses, 
                startedAfter, startedBefore, completedAfter, completedBefore, pageable);
    }
    
    private PaginatedResponseDto<CandidateAttemptCacheDto> getCandidateAttemptsWithFiltersInternal(Long candidateId, Long assessmentId, 
                                                                 List<AttemptStatus> statuses, LocalDateTime startedAfter, 
                                                                 LocalDateTime startedBefore, LocalDateTime completedAfter, 
                                                                 LocalDateTime completedBefore, Pageable pageable) {
        
        // Generate specific cache key for filtered query
        String specificCacheKey = generateSpecificCacheKey(candidateId, assessmentId, statuses, 
                                                          startedAfter, startedBefore, completedAfter, completedBefore);
        
        // Try to get from specific cache first
        List<CandidateAttemptCacheDto> cachedSpecificResult = getCachedAttemptList(specificCacheKey);
        if (cachedSpecificResult != null) {
            return applyPaginationToList(cachedSpecificResult, pageable);
        }
        
        // Try to get from general cache and apply filters in memory
        List<CandidateAttemptCacheDto> cachedGeneralResult = null;
        
        // Determine which general cache to use based on parameters
        if (candidateId != null) {
            cachedGeneralResult = getCachedAttemptList(generateCandidateGeneralCacheKey(candidateId));
        } else if (assessmentId != null) {
            cachedGeneralResult = getCachedAttemptList(generateAssessmentGeneralCacheKey(assessmentId));
        }
        
        if (cachedGeneralResult != null) {
            List<CandidateAttemptCacheDto> filteredResult = applyFiltersInMemory(cachedGeneralResult, candidateId, 
                    assessmentId, statuses, startedAfter, startedBefore, completedAfter, completedBefore);
            
            // Cache the filtered result for future use
            cacheAttemptList(specificCacheKey, filteredResult);
            
            return applyPaginationToList(filteredResult, pageable);
        }
        
        // No cache hit - fetch from database with all filters
        return fetchFromDatabaseWithFilters(candidateId, assessmentId, statuses, 
                                          startedAfter, startedBefore, completedAfter, completedBefore, 
                                          pageable, specificCacheKey);
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
        
        // Update cache: update general caches and evict specific caches
        updateCacheAfterAttemptUpdate(existingAttempt.getCandidate().getId(), existingAttempt.getAssessment().getId(), result);
        
        return result;
    }

    // Delete candidate attempt
    @CacheEvict(value = "attempts", key = "#id")
    @Transactional
    public void deleteCandidateAttempt(Long id) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        // Get attempt DTO and IDs before deletion for cache removal
        CandidateAttemptCacheDto attemptDto = new CandidateAttemptCacheDto(attempt);
        Long candidateId = attemptDto.getCandidate().getId();
        Long assessmentId = attemptDto.getAssessment().getId();
        Long userId = attemptDto.getAssessment().getUserId();
        
        // TODO: see if this can be deleted
        // IMPORTANT: Clean up bidirectional relationships before deletion
        // Remove the attempt from the candidate's attempts list
        Candidate candidate = attempt.getCandidate();
        if (candidate.getCandidateAttempts() != null) {
            candidate.getCandidateAttempts().removeIf(att -> att.getId().equals(id));
        }
        
        // Remove the attempt from the assessment's attempts list
        Assessment assessment = attempt.getAssessment();
        if (assessment.getCandidateAttempts() != null) {
            assessment.getCandidateAttempts().removeIf(att -> att.getId().equals(id));
        }
        
        // Remove the candidate from the assessment's candidates list
        if (assessment.getCandidates() != null) {
            assessment.getCandidates().removeIf(c -> c.getId().equals(candidateId));
        }
        
        // Remove the assessment from the candidate's assessments list
        if (candidate.getAssessments() != null) {
            candidate.getAssessments().removeIf(a -> a.getId().equals(assessmentId));
        }
        ////////
        
        // Now delete the attempt
        candidateAttemptRepository.deleteById(id);
        
        // Update cache: remove from general caches and evict specific caches
        updateCacheAfterAttemptDeletion(candidateId, assessmentId, attemptDto);
        
        // IMPORTANT: Invalidate the available candidates cache for this assessment
        // This ensures the candidate appears in the available candidates list after deletion
        // We need to invalidate all user_candidates caches since we don't know which user is viewing the assessment
        redisService.evictCache("cache:user_candidates:" + userId + ":*");
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
    @Cacheable(value = "attempts", key = "'overdue' + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
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
    @Cacheable(value = "attempts", key = "'withEvaluation' + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
        public List<CandidateAttemptCacheDto> getAttemptsWithEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findAttemptsWithEvaluation(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get submitted attempts without evaluation
    @Cacheable(value = "attempts", key = "'submittedWithoutEvaluation' + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getSubmittedAttemptsWithoutEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findSubmittedAttemptsWithoutEvaluation(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Count attempts by assessment and status
    @Cacheable(value = "attempts", key = "'count' + ':' + #assessmentId + ':' + #status")
    @Transactional(readOnly = true)
    public Long countAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status) {
        return candidateAttemptRepository.countByAssessmentIdAndStatus(assessmentId, status);
    }

    // Count attempts by candidate
    @Cacheable(value = "attempts", key = "'count' + ':' + #candidateId")
    @Transactional(readOnly = true)
    public Long countAttemptsByCandidate(Long candidateId) {
        return candidateAttemptRepository.countByCandidateId(candidateId);
    }

    // Get attempt with details
    @Cacheable(value = "attempts", key = "'withDetails' + ':' + #attemptId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getAttemptWithDetails(Long attemptId) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + attemptId)));
    }

    // Get recent attempts by user
    @Cacheable(value = "attempts", key = "'recent' + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getRecentAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findRecentAttemptsByUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment user
    @Cacheable(value = "attempts", key = "'byAssessmentUser' + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
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
        
        // Update cache: update general caches and evict specific caches
        updateCacheAfterAttemptUpdate(attempt.getCandidate().getId(), attempt.getAssessment().getId(), result);
        
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
        
        // Update cache: update general caches and evict specific caches
        updateCacheAfterAttemptUpdate(attempt.getCandidate().getId(), attempt.getAssessment().getId(), result);
        
        return result;
    }

    // Check if attempt is overdue
    @Cacheable(value = "attempts", key = "'overdue' + ':' + #id")
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
     * Cache key generation methods
     */
    private String generateCandidateGeneralCacheKey(Long candidateId) {
        return "cache:candidate_attempts:" + candidateId;
    }
    
    private String generateAssessmentGeneralCacheKey(Long assessmentId) {
        return "cache:assessment_attempts:" + assessmentId;
    }
    
    private String generateSpecificCacheKey(Long candidateId, Long assessmentId, List<AttemptStatus> statuses,
                                          LocalDateTime startedAfter, LocalDateTime startedBefore,
                                          LocalDateTime completedAfter, LocalDateTime completedBefore) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("cache:all_attempts:");
        
        // Add assessment and candidate IDs
        keyBuilder.append(assessmentId != null ? assessmentId.toString() : "null").append(":");
        keyBuilder.append(candidateId != null ? candidateId.toString() : "null").append(":");
        
        // Add attempt statuses (sorted for consistent keys)
        if (statuses != null && !statuses.isEmpty()) {
            statuses.stream().sorted().forEach(status -> keyBuilder.append(status.toString()).append(","));
        } else {
            keyBuilder.append("null");
        }
        keyBuilder.append(":");
        
        // Add date filters
        keyBuilder.append(startedAfter != null ? startedAfter.toString() : "null").append(":");
        keyBuilder.append(startedBefore != null ? startedBefore.toString() : "null").append(":");
        keyBuilder.append(completedAfter != null ? completedAfter.toString() : "null").append(":");
        keyBuilder.append(completedBefore != null ? completedBefore.toString() : "null");
        
        return keyBuilder.toString();
    }
    
    /**
     * Cache operations helper methods
     */
    @SuppressWarnings("unchecked")
    private List<CandidateAttemptCacheDto> getCachedAttemptList(String cacheKey) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached instanceof List<?>) {
                return (List<CandidateAttemptCacheDto>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached attempt list for key: {}, error: {}", cacheKey, e.getMessage());
        }
        return null;
    }
    
    private void cacheAttemptList(String cacheKey, List<CandidateAttemptCacheDto> attempts) {
        try {
            // Cache for 10 minutes (matching the attempts cache configuration)
            redisService.setWithExpiration(cacheKey, attempts, 10, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache attempt list for key: {}, error: {}", cacheKey, e.getMessage());
        }
    }
    
    private PaginatedResponseDto<CandidateAttemptCacheDto> getGeneralCandidateAttempts(Long candidateId, Pageable pageable) {
        String generalCacheKey = generateCandidateGeneralCacheKey(candidateId);
        List<CandidateAttemptCacheDto> cachedResult = getCachedAttemptList(generalCacheKey);
        
        if (cachedResult != null) {
            return applyPaginationToList(cachedResult, pageable);
        }
        
        // Fetch from database with only candidate filter
        Specification<CandidateAttempt> spec = CandidateAttemptSpecifications.hasCandidateId(candidateId);
        List<CandidateAttempt> attempts = candidateAttemptRepository.findAll(spec);
        List<CandidateAttemptCacheDto> attemptDtos = attempts.stream()
                .map(CandidateAttemptCacheDto::new)
                .collect(Collectors.toList());
        
        // Cache the general result
        cacheAttemptList(generalCacheKey, attemptDtos);
        
        return applyPaginationToList(attemptDtos, pageable);
    }
    
    private PaginatedResponseDto<CandidateAttemptCacheDto> getGeneralAssessmentAttempts(Long assessmentId, Pageable pageable) {
        String generalCacheKey = generateAssessmentGeneralCacheKey(assessmentId);
        List<CandidateAttemptCacheDto> cachedResult = getCachedAttemptList(generalCacheKey);
        
        if (cachedResult != null) {
            return applyPaginationToList(cachedResult, pageable);
        }
        
        // Fetch from database with only assessment filter
        Specification<CandidateAttempt> spec = CandidateAttemptSpecifications.hasAssessmentId(assessmentId);
        List<CandidateAttempt> attempts = candidateAttemptRepository.findAll(spec);
        List<CandidateAttemptCacheDto> attemptDtos = attempts.stream()
                .map(CandidateAttemptCacheDto::new)
                .collect(Collectors.toList());
        
        // Cache the general result
        cacheAttemptList(generalCacheKey, attemptDtos);
        
        return applyPaginationToList(attemptDtos, pageable);
    }
    
    private PaginatedResponseDto<CandidateAttemptCacheDto> fetchFromDatabaseWithFilters(Long candidateId, Long assessmentId,
            List<AttemptStatus> statuses, LocalDateTime startedAfter, LocalDateTime startedBefore,
            LocalDateTime completedAfter, LocalDateTime completedBefore, Pageable pageable, String specificCacheKey) {
        
        // Build specification with all filters
        Specification<CandidateAttempt> spec = CandidateAttemptSpecifications.hasAssessmentId(assessmentId);
        
        if (candidateId != null) {
            spec = spec.and(CandidateAttemptSpecifications.hasCandidateId(candidateId));
        }
        // if (assessmentId != null) {
        //     spec = spec.and(CandidateAttemptSpecifications.hasAssessmentId(assessmentId));
        // }
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and(CandidateAttemptSpecifications.hasAnyStatus(statuses));
        }
        if (startedAfter != null) {
            spec = spec.and(CandidateAttemptSpecifications.startedAfter(startedAfter));
        }
        if (startedBefore != null) {
            spec = spec.and(CandidateAttemptSpecifications.startedBefore(startedBefore));
        }
        if (completedAfter != null) {
            spec = spec.and(CandidateAttemptSpecifications.completedAfter(completedAfter));
        }
        if (completedBefore != null) {
            spec = spec.and(CandidateAttemptSpecifications.completedBefore(completedBefore));
        }
        
        List<CandidateAttempt> attempts = candidateAttemptRepository.findAll(spec);
        List<CandidateAttemptCacheDto> attemptDtos = attempts.stream()
                .map(CandidateAttemptCacheDto::new)
                .collect(Collectors.toList());
        
        // Cache the specific filtered result
        cacheAttemptList(specificCacheKey, attemptDtos);
        
        return applyPaginationToList(attemptDtos, pageable);
    }
    
    /**
     * In-memory filtering and pagination helper methods
     */
    private List<CandidateAttemptCacheDto> applyFiltersInMemory(List<CandidateAttemptCacheDto> attempts, Long candidateId,
            Long assessmentId, List<AttemptStatus> statuses, LocalDateTime startedAfter, LocalDateTime startedBefore,
            LocalDateTime completedAfter, LocalDateTime completedBefore) {
        
        return attempts.stream()
                .filter(attempt -> candidateId == null || 
                        (attempt.getCandidate() != null && candidateId.equals(attempt.getCandidate().getId())))
                .filter(attempt -> assessmentId == null || 
                        (attempt.getAssessment() != null && assessmentId.equals(attempt.getAssessment().getId())))
                .filter(attempt -> statuses == null || statuses.isEmpty() || statuses.contains(attempt.getStatus()))
                .filter(attempt -> startedAfter == null || 
                        (attempt.getStartedDate() != null && attempt.getStartedDate().isAfter(startedAfter)))
                .filter(attempt -> startedBefore == null || 
                        (attempt.getStartedDate() != null && attempt.getStartedDate().isBefore(startedBefore)))
                .filter(attempt -> completedAfter == null || 
                        (attempt.getCompletedDate() != null && attempt.getCompletedDate().isAfter(completedAfter)))
                .filter(attempt -> completedBefore == null || 
                        (attempt.getCompletedDate() != null && attempt.getCompletedDate().isBefore(completedBefore)))
                .collect(Collectors.toList());
    }
    
    private PaginatedResponseDto<CandidateAttemptCacheDto> applyPaginationToList(List<CandidateAttemptCacheDto> attempts, Pageable pageable) {
        // Apply sorting
        List<CandidateAttemptCacheDto> sortedAttempts = new ArrayList<>(attempts);
        if (pageable.getSort().isSorted()) {
            sortedAttempts.sort((a1, a2) -> {
                for (Sort.Order order : pageable.getSort()) {
                    int comparison = compareAttemptsByField(a1, a2, order.getProperty());
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedAttempts.size());
        
        List<CandidateAttemptCacheDto> pageContent = start >= sortedAttempts.size() ? 
                List.of() : sortedAttempts.subList(start, end);
        
        return new PaginatedResponseDto<>(
                pageContent,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortedAttempts.size()
        );
    }
    
    private int compareAttemptsByField(CandidateAttemptCacheDto a1, CandidateAttemptCacheDto a2, String field) {
        return switch (field.toLowerCase()) {
            case "id" -> compareNullable(a1.getId(), a2.getId());
            case "status" -> compareNullable(a1.getStatus(), a2.getStatus());
            case "languagechoice" -> compareNullable(a1.getLanguageChoice(), a2.getLanguageChoice());
            case "createddate" -> compareNullable(a1.getCreatedDate(), a2.getCreatedDate());
            case "updateddate" -> compareNullable(a1.getUpdatedDate(), a2.getUpdatedDate());
            case "starteddate" -> compareNullable(a1.getStartedDate(), a2.getStartedDate());
            case "completeddate" -> compareNullable(a1.getCompletedDate(), a2.getCompletedDate());
            case "evaluateddate" -> compareNullable(a1.getEvaluatedDate(), a2.getEvaluatedDate());
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
    private void updateCacheAfterAttemptCreation(Long candidateId, Long assessmentId, CandidateAttemptCacheDto newAttempt) {
        // Add to candidate general cache if it exists
        String candidateGeneralCacheKey = generateCandidateGeneralCacheKey(candidateId);
        List<CandidateAttemptCacheDto> cachedCandidateAttempts = getCachedAttemptList(candidateGeneralCacheKey);
        if (cachedCandidateAttempts != null) {
            cachedCandidateAttempts.add(newAttempt);
            cacheAttemptList(candidateGeneralCacheKey, cachedCandidateAttempts);
        }
        
        // Add to assessment general cache if it exists
        String assessmentGeneralCacheKey = generateAssessmentGeneralCacheKey(assessmentId);
        List<CandidateAttemptCacheDto> cachedAssessmentAttempts = getCachedAttemptList(assessmentGeneralCacheKey);
        if (cachedAssessmentAttempts != null) {
            cachedAssessmentAttempts.add(newAttempt);
            cacheAttemptList(assessmentGeneralCacheKey, cachedAssessmentAttempts);
        }
        
        // Evict all specific filter caches
        evictSpecificAttemptsCache(assessmentId, candidateId);
        
        // Evict available candidates cache for this assessment
        evictAvailableCandidatesCache();
    }
    
    private void updateCacheAfterAttemptUpdate(Long candidateId, Long assessmentId, CandidateAttemptCacheDto updatedAttempt) {
        // Update in candidate general cache if it exists
        String candidateGeneralCacheKey = generateCandidateGeneralCacheKey(candidateId);
        List<CandidateAttemptCacheDto> cachedCandidateAttempts = getCachedAttemptList(candidateGeneralCacheKey);
        if (cachedCandidateAttempts != null) {
            // Find and replace the attempt
            for (int i = 0; i < cachedCandidateAttempts.size(); i++) {
                if (cachedCandidateAttempts.get(i).getId().equals(updatedAttempt.getId())) {
                    cachedCandidateAttempts.set(i, updatedAttempt);
                    break;
                }
            }
            cacheAttemptList(candidateGeneralCacheKey, cachedCandidateAttempts);
        }
        
        // Update in assessment general cache if it exists
        String assessmentGeneralCacheKey = generateAssessmentGeneralCacheKey(assessmentId);
        List<CandidateAttemptCacheDto> cachedAssessmentAttempts = getCachedAttemptList(assessmentGeneralCacheKey);
        if (cachedAssessmentAttempts != null) {
            // Find and replace the attempt
            for (int i = 0; i < cachedAssessmentAttempts.size(); i++) {
                if (cachedAssessmentAttempts.get(i).getId().equals(updatedAttempt.getId())) {
                    cachedAssessmentAttempts.set(i, updatedAttempt);
                    break;
                }
            }
            cacheAttemptList(assessmentGeneralCacheKey, cachedAssessmentAttempts);
        }
        
        // Evict all specific filter caches
        evictSpecificAttemptsCache(assessmentId, candidateId);
    }
    
    private void updateCacheAfterAttemptDeletion(Long candidateId, Long assessmentId, CandidateAttemptCacheDto deletedAttempt) {
        // Remove from candidate general cache if it exists
        String candidateGeneralCacheKey = generateCandidateGeneralCacheKey(candidateId);
        List<CandidateAttemptCacheDto> cachedCandidateAttempts = getCachedAttemptList(candidateGeneralCacheKey);
        if (cachedCandidateAttempts != null) {
            cachedCandidateAttempts.removeIf(attempt -> attempt.getId().equals(deletedAttempt.getId()));
            cacheAttemptList(candidateGeneralCacheKey, cachedCandidateAttempts);
        }
        
        // Remove from assessment general cache if it exists
        String assessmentGeneralCacheKey = generateAssessmentGeneralCacheKey(assessmentId);
        List<CandidateAttemptCacheDto> cachedAssessmentAttempts = getCachedAttemptList(assessmentGeneralCacheKey);
        if (cachedAssessmentAttempts != null) {
            cachedAssessmentAttempts.removeIf(attempt -> attempt.getId().equals(deletedAttempt.getId()));
            cacheAttemptList(assessmentGeneralCacheKey, cachedAssessmentAttempts);
        }
        
        // Evict all specific filter caches
        evictSpecificAttemptsCache(assessmentId, candidateId);
    }

    private void evictSpecificAttemptsCache(Long assessmentId, Long candidateId) {
        if (assessmentId != null && candidateId != null) {
            redisService.evictCache("cache:all_attempts:" + assessmentId + ":" + candidateId + ":*");
        }
        if (assessmentId != null) {
            redisService.evictCache("cache:assessment_attempts:" + assessmentId + ":*");
        }
    }
    
    private void evictAvailableCandidatesCache() {
        // Evict all available candidates cache entries for this assessment
        // TODO: evict the cache for the specific user
        redisService.evictCache("cache:user_candidates:*");
    }

}