package com.delphi.delphi.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
import com.delphi.delphi.components.messaging.candidates.CandidateInvitationPublisher;
import com.delphi.delphi.dtos.NewAssessmentDto;
import com.delphi.delphi.dtos.PaginatedResponseDto;
import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.ChatMessageCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.AssessmentRepository;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.repositories.UserRepository;
import com.delphi.delphi.specifications.AssessmentSpecifications;
import com.delphi.delphi.utils.Constants;
import com.delphi.delphi.utils.enums.AssessmentStatus;
import com.delphi.delphi.utils.enums.AttemptStatus;
import com.delphi.delphi.utils.exceptions.AssessmentNotFoundException;
import com.delphi.delphi.utils.git.GithubAccountType;

@Service
@Transactional
// TODO: add cache annotations for other entity caches
/*
 * There are two different caches used here
 * 1. assessments - data about a given assessment
 * 2. user_assessments - the list of assessments for a user
 * (filters like status, skills, languageOptions are applied in memory)
 */
public class AssessmentService {

    private final UserRepository userRepository;
    private final CandidateAttemptRepository candidateAttemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final GithubService githubService;
    private final CandidateInvitationPublisher candidateInvitationPublisher;
    private final CandidateRepository candidateRepository;
    private final Logger log = LoggerFactory.getLogger(AssessmentService.class);
    private final RedisService redisService;

    public AssessmentService(AssessmentRepository assessmentRepository, GithubService githubService,
            CandidateAttemptRepository candidateAttemptRepository,
            CandidateInvitationPublisher candidateInvitationPublisher, UserRepository userRepository,
            CandidateRepository candidateRepository, RedisService redisService) {
        this.assessmentRepository = assessmentRepository;
        this.githubService = githubService;
        this.candidateAttemptRepository = candidateAttemptRepository;
        this.candidateInvitationPublisher = candidateInvitationPublisher;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.redisService = redisService;
    }

    // Create a new assessment
    @CachePut(value = "assessments", key = "#result.id")
    public AssessmentCacheDto createAssessment(Assessment assessment) {
        // Validate that the associated user exists
        if (assessment.getUser() != null && assessment.getUser().getId() != null) {
            User user = userRepository.findById(assessment.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found with id: " + assessment.getUser().getId()));
            assessment.setUser(user);
        }

        // Set default status if not provided
        if (assessment.getStatus() == null) {
            assessment.setStatus(AssessmentStatus.DRAFT);
        }

        // Validate date logic
        if (assessment.getStartDate() != null && assessment.getEndDate() != null) {
            if (!assessment.getEndDate().isAfter(assessment.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }

        if (assessment.getChatMessages() == null) {
            assessment.setChatMessages(List.of());
        }

        AssessmentCacheDto savedAssessment = new AssessmentCacheDto(assessmentRepository.save(assessment));
        
        // Update cache: add to general cache and evict specific caches
        updateCacheAfterAssessmentCreation(assessment.getUser().getId(), savedAssessment);
        
        return savedAssessment;
    }

    @CachePut(value = "assessments", key = "#result.id")
    public AssessmentCacheDto createAssessment(NewAssessmentDto newAssessmentDto, UserCacheDto user) throws Exception {
        Assessment assessment = new Assessment();
        assessment.setName(newAssessmentDto.getName());
        assessment.setDescription(newAssessmentDto.getDescription());
        assessment.setRole(newAssessmentDto.getRole());
        assessment.setDuration(newAssessmentDto.getDuration());
        assessment.setSkills(newAssessmentDto.getSkills());
        assessment.setDetails(newAssessmentDto.getDetails());
        assessment.setRules(Constants.DEFAULT_RULES_GUIDELINES);
        assessment.setLanguageOptions(newAssessmentDto.getLanguageOptions());
        // TODO: replace w/ something else?
        assessment.setGithubRepoName(newAssessmentDto.getName().toLowerCase().replaceAll("[^a-zA-Z0-9]","-") + "-"
                + String.valueOf(Instant.now().getEpochSecond()));
        assessment.setUser(userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId())));
        assessment.setStatus(AssessmentStatus.DRAFT);

        // create github repo for the assessment
        if (user.getGithubAccountType() == GithubAccountType.USER) {
            githubService.createPersonalRepo(user.getGithubAccessToken(), user.getGithubUsername(), assessment.getGithubRepoName());
        } else {
            githubService.createOrgRepo(user.getGithubAccessToken(), user.getGithubUsername(),
                    assessment.getGithubRepoName());
        }
        log.info("adding themus assessments as contributor to the repo...");

        log.info("repo created and themus assessments added as contributor, setting github repository link for assessment: {}", assessment);
        assessment.setGithubRepositoryLink(
                "https://github.com/" + user.getGithubUsername().toLowerCase() + "/" + assessment.getGithubRepoName());

        // Initialize chat history w/ system prompt for the assessment
        // ChatMessage chatMessage = new ChatMessage();
        // chatMessage.setAssessment(assessment);
        // chatMessage.setMessageType(MessageType.SYSTEM);
        // chatMessage.setModel("N/A");
        // chatMessage.setText(AssessmentCreationPrompts.SYSTEM_PROMPT);
        assessment.setChatMessages(List.of());

        log.info("set github repository link for assessment: {}", assessment);
        log.info("assessment name: {}", assessment.getName());
        log.info("assessment description: {}", assessment.getDescription());
        log.info("assessment details: {}", assessment.getDetails());
        log.info("assessment role: {}", assessment.getRole());
        log.info("assessment start date: {}", assessment.getStartDate());
        log.info("assessment end date: {}", assessment.getEndDate());
        log.info("assessment duration: {}", assessment.getDuration());
        log.info("assessment skills: {}", assessment.getSkills());
        log.info("assessment github repo name: {}", assessment.getGithubRepoName());
        log.info("assessment github repository link: {}", assessment.getGithubRepositoryLink());
        log.info("assessment language options: {}", assessment.getLanguageOptions());
        // save assessment in DB
        AssessmentCacheDto savedAssessment = new AssessmentCacheDto(assessmentRepository.save(assessment));
        
        // Update cache: add to general cache and evict specific caches
        updateCacheAfterAssessmentCreation(user.getId(), savedAssessment);
        
        return savedAssessment;
    }

    // Get assessment by ID
    @Cacheable(value = "assessments", key = "#id")
    @Transactional(readOnly = true)
    public AssessmentCacheDto getAssessmentByIdCache(Long id) {
        return new AssessmentCacheDto(assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id)));
    }

    @Transactional(readOnly = true)
    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new AssessmentNotFoundException("Assessment not found with id: " + id));
    }

    // Get chat history by assessment ID
    @Cacheable(value = "chat_messages", key = "'assessment:' + #id")
    @Transactional(readOnly = true)
    public List<ChatMessageCacheDto> getChatMessagesById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new AssessmentNotFoundException("Assessment not found with id: " + id))
                .getChatMessages().stream()
                .map(ChatMessageCacheDto::new)
                .collect(Collectors.toList());
    }

    // Get all assessments with pagination
    // @Cacheable(value = "assessments", key = "#pageable.pageNumber + ':' +
    // #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<AssessmentCacheDto> getAllAssessments(Pageable pageable) {
    // return
    // assessmentRepository.findAll(pageable).getContent().stream().map(AssessmentCacheDto::new).collect(Collectors.toList());
    // }

    /**
     * Get assessments with filters using a new caching strategy:
     * - Cache keys only contain user ID and date range (startDate, endDate)
     * - Other filters (status, skills, languageOptions, assessmentStartDate, assessmentEndDate) are applied in memory
     * - This reduces cache key proliferation and improves cache hit rates
     * 
     * @param user            The user whose assessments to retrieve
     * @param status          Filter by assessment status (applied in memory)
     * @param startDate       Start date for creation date range filter (used in cache key)
     * @param endDate         End date for creation date range filter (used in cache key)
     * @param assessmentStartDate Start date for assessment date range filter (applied in memory)
     * @param assessmentEndDate   End date for assessment date range filter (applied in memory)
     * @param skills          Filter by required skills (applied in memory)
     * @param languageOptions Filter by required language options (applied in memory)
     * @param pageable        Pagination and sorting parameters (applied in memory)
     * @return PaginatedResponseDto containing filtered assessments and pagination metadata
     * 
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDto<AssessmentCacheDto> getAssessmentsWithFilters(UserCacheDto user, AssessmentStatus status,
            LocalDateTime createdAfter, LocalDateTime createdBefore, LocalDateTime assessmentStartDate, LocalDateTime assessmentEndDate,
            List<String> skills, List<String> languageOptions, Pageable pageable) {
        
        // Check if no filters are applied (only user filter)
        boolean hasFilters = status != null || createdAfter != null || createdBefore != null || 
                           assessmentStartDate != null || assessmentEndDate != null || 
                           (skills != null && !skills.isEmpty()) || (languageOptions != null && !languageOptions.isEmpty());
        
        if (!hasFilters) {
            // No filters - check general cache or fetch from DB
            return getGeneralUserAssessments(user, pageable);
        }
        
        // Generate specific cache key for filtered query
        String specificCacheKey = generateSpecificCacheKey(user.getId(), status, createdAfter, createdBefore, 
                                                          assessmentStartDate, assessmentEndDate, skills, languageOptions);
        
        // Try to get from specific cache first
        List<AssessmentCacheDto> cachedSpecificResult = getCachedAssessmentList(specificCacheKey);
        if (cachedSpecificResult != null) {
            return applyPaginationToList(cachedSpecificResult, pageable);
        }
        
        // Try to get from general cache and apply filters in memory
        String generalCacheKey = generateGeneralCacheKey(user.getId());
        List<AssessmentCacheDto> cachedGeneralResult = getCachedAssessmentList(generalCacheKey);
        if (cachedGeneralResult != null) {
            List<AssessmentCacheDto> filteredResult = applyFiltersInMemory(cachedGeneralResult, status, 
                    createdAfter, createdBefore, assessmentStartDate, assessmentEndDate, skills, languageOptions);
            
            // Cache the filtered result for future use
            cacheAssessmentList(specificCacheKey, filteredResult);
            
            return applyPaginationToList(filteredResult, pageable);
        }
        
        // No cache hit - fetch from database with all filters
        return fetchFromDatabaseWithFilters(user, status, createdAfter, createdBefore, 
                                          assessmentStartDate, assessmentEndDate, skills, languageOptions, 
                                          pageable, specificCacheKey);
    }

    // Update assessment
    @CachePut(value = "assessments", key = "#result.id")
    public AssessmentCacheDto updateAssessment(Long id, Assessment assessmentUpdates) {
        Assessment existingAssessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));

        // Update fields if provided
        if (assessmentUpdates.getName() != null) {
            existingAssessment.setName(assessmentUpdates.getName());
        }
        if (assessmentUpdates.getDescription() != null) {
            existingAssessment.setDescription(assessmentUpdates.getDescription());
        }
        if (assessmentUpdates.getRole() != null) {
            existingAssessment.setRole(assessmentUpdates.getRole());
        }
        if (assessmentUpdates.getStatus() != null) {
            existingAssessment.setStatus(assessmentUpdates.getStatus());
        }
        if (assessmentUpdates.getStartDate() != null) {
            existingAssessment.setStartDate(assessmentUpdates.getStartDate());
        }
        if (assessmentUpdates.getEndDate() != null) {
            existingAssessment.setEndDate(assessmentUpdates.getEndDate());
        }
        if (assessmentUpdates.getDuration() != null) {
            existingAssessment.setDuration(assessmentUpdates.getDuration());
        }
        if (assessmentUpdates.getGithubRepositoryLink() != null) {
            existingAssessment.setGithubRepositoryLink(assessmentUpdates.getGithubRepositoryLink());
        }
        if (assessmentUpdates.getSkills() != null) {
            existingAssessment.setSkills(assessmentUpdates.getSkills());
        }
        if (assessmentUpdates.getLanguageOptions() != null) {
            existingAssessment.setLanguageOptions(assessmentUpdates.getLanguageOptions());
        }
        if (assessmentUpdates.getMetadata() != null) {
            existingAssessment.setMetadata(assessmentUpdates.getMetadata());
        }
        if (assessmentUpdates.getDetails() != null) {
            existingAssessment.setDetails(assessmentUpdates.getDetails());
        }

        // Validate date logic after updates
        if (existingAssessment.getStartDate() != null && existingAssessment.getEndDate() != null) {
            if (!existingAssessment.getEndDate().isAfter(existingAssessment.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }

        AssessmentCacheDto updatedAssessment = new AssessmentCacheDto(assessmentRepository.save(existingAssessment));
        
        // Update cache: update general cache and evict specific caches
        updateCacheAfterAssessmentUpdate(existingAssessment.getUser().getId(), updatedAssessment);
        
        return updatedAssessment;
    }

    // Update assessment
    @CachePut(value = "assessments", key = "#result.id")
    public AssessmentCacheDto updateAssessment(Assessment existingAssessment, Assessment assessmentUpdates) {
        // Update fields if provided
        if (assessmentUpdates.getName() != null) {
            existingAssessment.setName(assessmentUpdates.getName());
        }
        if (assessmentUpdates.getDescription() != null) {
            existingAssessment.setDescription(assessmentUpdates.getDescription());
        }
        if (assessmentUpdates.getRole() != null) {
            existingAssessment.setRole(assessmentUpdates.getRole());
        }
        if (assessmentUpdates.getStatus() != null) {
            existingAssessment.setStatus(assessmentUpdates.getStatus());
        }
        if (assessmentUpdates.getStartDate() != null) {
            existingAssessment.setStartDate(assessmentUpdates.getStartDate());
        }
        if (assessmentUpdates.getEndDate() != null) {
            existingAssessment.setEndDate(assessmentUpdates.getEndDate());
        }
        if (assessmentUpdates.getDuration() != null) {
            existingAssessment.setDuration(assessmentUpdates.getDuration());
        }
        if (assessmentUpdates.getGithubRepositoryLink() != null) {
            existingAssessment.setGithubRepositoryLink(assessmentUpdates.getGithubRepositoryLink());
        }
        if (assessmentUpdates.getSkills() != null) {
            existingAssessment.setSkills(assessmentUpdates.getSkills());
        }
        if (assessmentUpdates.getLanguageOptions() != null) {
            existingAssessment.setLanguageOptions(assessmentUpdates.getLanguageOptions());
        }
        if (assessmentUpdates.getMetadata() != null) {
            existingAssessment.setMetadata(assessmentUpdates.getMetadata());
        }
        if (assessmentUpdates.getDetails() != null) {
            existingAssessment.setDetails(assessmentUpdates.getDetails());
        }

        // Validate date logic after updates
        if (existingAssessment.getStartDate() != null && existingAssessment.getEndDate() != null) {
            if (!existingAssessment.getEndDate().isAfter(existingAssessment.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }

        AssessmentCacheDto updatedAssessment = new AssessmentCacheDto(assessmentRepository.save(existingAssessment));
        
        // Update cache: update general cache and evict specific caches
        updateCacheAfterAssessmentUpdate(existingAssessment.getUser().getId(), updatedAssessment);
        
        return updatedAssessment;
    }

    public AssessmentCacheDto updateSetupInstructions(AssessmentCacheDto assessment, String setupInstructions) {
        assessment.setInstructions(setupInstructions);
        assessmentRepository.updateSetupInstructions(assessment.getId(), setupInstructions);
        updateCacheAfterAssessmentUpdate(assessment.getUserId(), assessment);
        return assessment;
    }

    // Delete assessment
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public void deleteAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        
        // Get assessment DTO for cache removal
        AssessmentCacheDto assessmentDto = new AssessmentCacheDto(assessment);
        
        assessmentRepository.delete(assessment);
        
        // Update cache: remove from general cache and evict specific caches
        updateCacheAfterAssessmentDeletion(assessment.getUser().getId(), assessmentDto);
    }

    // Get assessments by user ID
    // @Cacheable(value = "assessments", key = "#userId + ':' + #pageable.pageNumber
    // + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<AssessmentCacheDto> getAssessmentsByUserId(Long userId, Pageable
    // pageable) {
    // return assessmentRepository.findByUserId(userId,
    // pageable).getContent().stream().map(AssessmentCacheDto::new).collect(Collectors.toList());
    // }

    // // Get assessments by status
    // @Cacheable(value = "assessments", key = "#status + ':' + #pageable.pageNumber
    // + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<AssessmentCacheDto> getAssessmentsByStatus(AssessmentStatus
    // status, Pageable pageable) {
    // return assessmentRepository.findByStatus(status,
    // pageable).getContent().stream().map(AssessmentCacheDto::new).collect(Collectors.toList());
    // }

    // // Get assessments by user and status
    // @Cacheable(value = "assessments", key = "#userId + ':' + #status + ':' +
    // #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<AssessmentCacheDto> getAssessmentsByUserAndStatus(Long userId,
    // AssessmentStatus status, Pageable pageable) {
    // return assessmentRepository.findByUserIdAndStatus(userId, status,
    // pageable).getContent().stream().map(AssessmentCacheDto::new).collect(Collectors.toList());
    // }

    // Search assessments by name
    @Cacheable(value = "assessments", key = "#user.id + ':' + #name + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> searchAssessmentsByName(UserCacheDto user, String name, Pageable pageable) {
        return assessmentRepository.findByNameContainingIgnoreCase(name, pageable).getContent().stream()
                .map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Search assessments by role name
    @Cacheable(value = "assessments", key = "#user.id + ':' + #role + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> searchAssessmentsByRoleName(UserCacheDto user, String role, Pageable pageable) {
        return assessmentRepository.findByRoleContainingIgnoreCase(role, pageable).getContent().stream()
                .map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Get assessments within date range
    @Cacheable(value = "assessments", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> getAssessmentsInDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return assessmentRepository.findByDateRange(startDate, endDate, pageable).getContent().stream()
                .map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Get active assessments within current date
    @Cacheable(value = "assessments", key = "#currentDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> getActiveAssessmentsInDateRange(LocalDateTime currentDate, Pageable pageable) {
        return assessmentRepository.findActiveAssessmentsInDateRange(currentDate, pageable).getContent().stream()
                .map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Get assessments by duration range
    @Cacheable(value = "assessments", key = "#minDuration + ':' + #maxDuration + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> getAssessmentsByDurationRange(Integer minDuration, Integer maxDuration,
            Pageable pageable) {
        return assessmentRepository.findByDurationBetween(minDuration, maxDuration, pageable).getContent().stream()
                .map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Get assessments by skill
    @Cacheable(value = "assessments", key = "#user.id + ':' + #skill + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> getAssessmentsBySkill(UserCacheDto user, String skill, Pageable pageable) {
        return assessmentRepository.findBySkill(skill, pageable).getContent().stream().map(AssessmentCacheDto::new)
                .collect(Collectors.toList());
    }

    // Get assessments by language option
    @Cacheable(value = "assessments", key = "#language + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> getAssessmentsByLanguageOption(String language, Pageable pageable) {
        return assessmentRepository.findByLanguageOption(language, pageable).getContent().stream()
                .map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Get assessments with attempt count
    // @Cacheable(value = "assessments", key = "#pageable.pageNumber + ':' +
    // #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<Object[]> getAssessmentsWithAttemptCount(Pageable pageable) {
    // return
    // assessmentRepository.findAssessmentsWithAttemptCount(pageable).getContent().stream().map(AssessmentCacheDto::new).collect(Collectors.toList());
    // }

    // Get assessments created by user in date range
    @Cacheable(value = "assessments", key = "#userId + ':' + #startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<AssessmentCacheDto> getAssessmentsByUserInDateRange(Long userId, LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return assessmentRepository.findByUserIdAndCreatedDateBetween(userId, startDate, endDate, pageable).getContent()
                .stream().map(AssessmentCacheDto::new).collect(Collectors.toList());
    }

    // Count assessments by status for a user
    @Cacheable(value = "assessments", key = "#user.id + ':' + #status + ':' + #count")
    @Transactional(readOnly = true)
    public Long countAssessmentsByUserAndStatus(UserCacheDto user, AssessmentStatus status) {
        return assessmentRepository.countByUserIdAndStatus(user.getId(), status);
    }

    // Activate assessment
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment activateAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        assessment.setStatus(AssessmentStatus.ACTIVE);
        return assessmentRepository.save(assessment);
    }

    // Deactivate assessment
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment deactivateAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        assessment.setStatus(AssessmentStatus.INACTIVE);
        return assessmentRepository.save(assessment);
    }

    // Publish assessment (change from draft to active)
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment publishAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new IllegalStateException("Only draft assessments can be published");
        }
        assessment.setStatus(AssessmentStatus.ACTIVE);
        return assessmentRepository.save(assessment);
    }

    // Add skill to assessment
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment addSkill(Long id, String skill) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        if (assessment.getSkills() != null && !assessment.getSkills().contains(skill)) {
            assessment.getSkills().add(skill);
        }
        return assessmentRepository.save(assessment);
    }

    // Add candidate from existing candidate
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#assessmentId")
    // TODO: add cache annotation for candidate attempt
    public CandidateAttempt addCandidateFromExisting(Long assessmentId, Long candidateId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + assessmentId));
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId));
        assessment.addCandidate(candidate);
        assessmentRepository.save(assessment);

        // Check if candidate already has an attempt for this assessment
        Optional<CandidateAttempt> existingAttempt = candidateAttemptRepository.findByCandidateIdAndAssessmentId(
                candidateId,
                assessmentId);

        if (existingAttempt.isPresent()) {
            throw new IllegalArgumentException("Candidate already has an attempt for this assessment");
        }

        // Create a new candidate attempt and set the status to INVITED
        CandidateAttempt candidateAttempt = new CandidateAttempt();
        candidateAttempt.setCandidate(candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId)));
        candidateAttempt.setAssessment(assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + assessmentId)));
        candidateAttempt.setStatus(AttemptStatus.INVITED);

        // Send message to message queue to add candidate to assessment in python
        // service
        candidateInvitationPublisher.publishCandidateInvitation(
                assessment,
                candidate,
                assessment.getUser().getId(),
                assessment.getUser().getEmail());

        return candidateAttemptRepository.save(candidateAttempt);
    }

    // Add a new candidate to the assessment that doesn't already exist in the
    // database
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#assessmentId")
    public CandidateAttempt addCandidateFromNew(Long assessmentId, String firstName, String lastName, String email) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + assessmentId));
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with email: " + email));
        assessment.addCandidate(candidate);
        assessmentRepository.save(assessment);

        // Create a new candidate attempt and set the status to INVITED
        CandidateAttempt candidateAttempt = new CandidateAttempt();
        candidateAttempt.setCandidate(candidate);
        candidateAttempt.setAssessment(assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + assessmentId)));
        candidateAttempt.setStatus(AttemptStatus.INVITED);

        // Send message to message queue to add candidate to assessment in python
        // service
        candidateInvitationPublisher.publishCandidateInvitation(
                assessment,
                candidate,
                assessment.getUser().getId(),
                assessment.getUser().getEmail());

        return candidateAttemptRepository.save(candidateAttempt);
    }

    // Remove skill from assessment
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment removeSkill(Long id, String skill) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        if (assessment.getSkills() != null) {
            assessment.getSkills().remove(skill);
        }
        return assessmentRepository.save(assessment);
    }

    // Update skills
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment updateSkills(Long id, List<String> skills) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        assessment.setSkills(skills);
        return assessmentRepository.save(assessment);
    }

    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#assessmentId")
    public Candidate removeCandidateFromAssessment(Long assessmentId, Long candidateId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + assessmentId));
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId));
        assessment.setCandidates(assessment.getCandidates().stream().filter(c -> !c.getId().equals(candidateId))
                .collect(Collectors.toList()));
        assessmentRepository.save(assessment);
        return candidate;
    }

    // Update language options
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment updateLanguageOptions(Long id, List<String> languageOptions) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        assessment.setLanguageOptions(languageOptions);
        return assessmentRepository.save(assessment);
    }

    // Update metadata
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public Assessment updateMetadata(Long id, Map<String, String> metadata) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        assessment.setMetadata(metadata);
        return assessmentRepository.save(assessment);
    }

    public void updateExpiredAssessments() {
        assessmentRepository.updateExpiredAssessments(LocalDateTime.now());
    }

    /**
     * Cache key generation methods
     */
    private String generateGeneralCacheKey(Long userId) {
        return "cache:user_assessments:" + userId;
    }
    
    private String generateSpecificCacheKey(Long userId, AssessmentStatus status, LocalDateTime createdAfter, 
                                          LocalDateTime createdBefore, LocalDateTime assessmentStartDate, 
                                          LocalDateTime assessmentEndDate, List<String> skills, List<String> languageOptions) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("cache:user_assessments:").append(userId).append(":");
        
        // Add status
        keyBuilder.append(status != null ? status.toString() : "null").append(":");
        
        // Add date filters
        keyBuilder.append(createdAfter != null ? createdAfter.toString() : "null").append(":");
        keyBuilder.append(createdBefore != null ? createdBefore.toString() : "null").append(":");
        keyBuilder.append(assessmentStartDate != null ? assessmentStartDate.toString() : "null").append(":");
        keyBuilder.append(assessmentEndDate != null ? assessmentEndDate.toString() : "null").append(":");
        
        // Add skills (sorted for consistent keys)
        if (skills != null && !skills.isEmpty()) {
            skills.stream().sorted().forEach(skill -> keyBuilder.append(skill).append(","));
        } else {
            keyBuilder.append("null");
        }
        keyBuilder.append(":");
        
        // Add language options (sorted for consistent keys)
        if (languageOptions != null && !languageOptions.isEmpty()) {
            languageOptions.stream().sorted().forEach(lang -> keyBuilder.append(lang).append(","));
        } else {
            keyBuilder.append("null");
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * Cache operations helper methods
     */
    @SuppressWarnings("unchecked")
    private List<AssessmentCacheDto> getCachedAssessmentList(String cacheKey) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached instanceof List<?>) {
                return (List<AssessmentCacheDto>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached assessment list for key: {}, error: {}", cacheKey, e.getMessage());
        }
        return null;
    }
    
    private void cacheAssessmentList(String cacheKey, List<AssessmentCacheDto> assessments) {
        try {
            // Cache for 15 minutes (matching the assessments cache configuration)
            redisService.setWithExpiration(cacheKey, assessments, 15, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache assessment list for key: {}, error: {}", cacheKey, e.getMessage());
        }
    }
    
    private PaginatedResponseDto<AssessmentCacheDto> getGeneralUserAssessments(UserCacheDto user, Pageable pageable) {
        String generalCacheKey = generateGeneralCacheKey(user.getId());
        List<AssessmentCacheDto> cachedResult = getCachedAssessmentList(generalCacheKey);
        
        if (cachedResult != null) {
            return applyPaginationToList(cachedResult, pageable);
        }
        
        // Fetch from database with only user filter
        Specification<Assessment> spec = AssessmentSpecifications.belongsToUser(user.getId());
        List<Assessment> assessments = assessmentRepository.findAll(spec);
        List<AssessmentCacheDto> assessmentDtos = assessments.stream()
                .map(AssessmentCacheDto::new)
                .collect(Collectors.toList());
        
        // Cache the general result
        cacheAssessmentList(generalCacheKey, assessmentDtos);
        
        return applyPaginationToList(assessmentDtos, pageable);
    }
    
    private PaginatedResponseDto<AssessmentCacheDto> fetchFromDatabaseWithFilters(UserCacheDto user, AssessmentStatus status,
            LocalDateTime createdAfter, LocalDateTime createdBefore, LocalDateTime assessmentStartDate, LocalDateTime assessmentEndDate,
            List<String> skills, List<String> languageOptions, Pageable pageable, String specificCacheKey) {
        
        // Build specification with all filters
        Specification<Assessment> spec = AssessmentSpecifications.belongsToUser(user.getId());
        
        if (status != null) {
            spec = spec.and(AssessmentSpecifications.hasAssessmentStatus(status));
        }
        if (createdAfter != null) {
            spec = spec.and(AssessmentSpecifications.createdAfter(createdAfter));
        }
        if (createdBefore != null) {
            spec = spec.and(AssessmentSpecifications.createdBefore(createdBefore));
        }
        if (assessmentStartDate != null) {
            spec = spec.and(AssessmentSpecifications.startDateAfter(assessmentStartDate));
        }
        if (assessmentEndDate != null) {
            spec = spec.and(AssessmentSpecifications.endDateBefore(assessmentEndDate));
        }
        if (skills != null && !skills.isEmpty()) {
            for (String skill : skills) {
                spec = spec.and(AssessmentSpecifications.hasSkill(skill));
            }
        }
        if (languageOptions != null && !languageOptions.isEmpty()) {
            spec = spec.and(AssessmentSpecifications.hasLanguageOptions(languageOptions));
        }
        
        List<Assessment> assessments = assessmentRepository.findAll(spec);
        List<AssessmentCacheDto> assessmentDtos = assessments.stream()
                .map(AssessmentCacheDto::new)
                .collect(Collectors.toList());
        
        // Cache the specific filtered result
        cacheAssessmentList(specificCacheKey, assessmentDtos);
        
        return applyPaginationToList(assessmentDtos, pageable);
    }
    
    /**
     * In-memory filtering and pagination helper methods
     */
    private List<AssessmentCacheDto> applyFiltersInMemory(List<AssessmentCacheDto> assessments, AssessmentStatus status,
            LocalDateTime createdAfter, LocalDateTime createdBefore, LocalDateTime assessmentStartDate, LocalDateTime assessmentEndDate,
            List<String> skills, List<String> languageOptions) {
        
        return assessments.stream()
                .filter(assessment -> status == null || assessment.getStatus() == status)
                .filter(assessment -> createdAfter == null || assessment.getCreatedDate().isAfter(createdAfter))
                .filter(assessment -> createdBefore == null || assessment.getCreatedDate().isBefore(createdBefore))
                .filter(assessment -> assessmentStartDate == null || 
                        (assessment.getStartDate() != null && assessment.getStartDate().isAfter(assessmentStartDate)))
                .filter(assessment -> assessmentEndDate == null || 
                        (assessment.getEndDate() != null && assessment.getEndDate().isBefore(assessmentEndDate)))
                .filter(assessment -> skills == null || skills.isEmpty() || 
                        (assessment.getSkills() != null && assessment.getSkills().containsAll(skills)))
                .filter(assessment -> languageOptions == null || languageOptions.isEmpty() || 
                        (assessment.getLanguageOptions() != null && assessment.getLanguageOptions().containsAll(languageOptions)))
                .collect(Collectors.toList());
    }
    
    private PaginatedResponseDto<AssessmentCacheDto> applyPaginationToList(List<AssessmentCacheDto> assessments, Pageable pageable) {
        // Apply sorting
        List<AssessmentCacheDto> sortedAssessments = new ArrayList<>(assessments);
        if (pageable.getSort().isSorted()) {
            sortedAssessments.sort((a1, a2) -> {
                for (Sort.Order order : pageable.getSort()) {
                    int comparison = compareAssessmentsByField(a1, a2, order.getProperty());
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedAssessments.size());
        
        List<AssessmentCacheDto> pageContent = start >= sortedAssessments.size() ? 
                List.of() : sortedAssessments.subList(start, end);
        
        return new PaginatedResponseDto<>(
                pageContent,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortedAssessments.size()
        );
    }
    
    private int compareAssessmentsByField(AssessmentCacheDto a1, AssessmentCacheDto a2, String field) {
        return switch (field) {
            case "name" -> compareNullable(a1.getName(), a2.getName());
            case "createdDate" -> compareNullable(a1.getCreatedDate(), a2.getCreatedDate());
            case "updatedDate" -> compareNullable(a1.getUpdatedDate(), a2.getUpdatedDate());
            case "startDate" -> compareNullable(a1.getStartDate(), a2.getStartDate());
            case "endDate" -> compareNullable(a1.getEndDate(), a2.getEndDate());
            case "status" -> compareNullable(a1.getStatus(), a2.getStatus());
            case "role" -> compareNullable(a1.getRole(), a2.getRole());
            case "duration" -> compareNullable(a1.getDuration(), a2.getDuration());
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
    private void updateCacheAfterAssessmentCreation(Long userId, AssessmentCacheDto newAssessment) {
        // Add to general cache if it exists
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<AssessmentCacheDto> cachedAssessments = getCachedAssessmentList(generalCacheKey);
        if (cachedAssessments != null) {
            cachedAssessments.add(newAssessment);
            cacheAssessmentList(generalCacheKey, cachedAssessments);
        }
        
        // Evict all specific filter caches for this user
        evictUserAssessmentsSpecificCaches(userId);
    }
    
    private void updateCacheAfterAssessmentUpdate(Long userId, AssessmentCacheDto updatedAssessment) {
        // Update in general cache if it exists
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<AssessmentCacheDto> cachedAssessments = getCachedAssessmentList(generalCacheKey);
        if (cachedAssessments != null) {
            // Find and replace the assessment
            for (int i = 0; i < cachedAssessments.size(); i++) {
                if (cachedAssessments.get(i).getId().equals(updatedAssessment.getId())) {
                    cachedAssessments.set(i, updatedAssessment);
                    break;
                }
            }
            cacheAssessmentList(generalCacheKey, cachedAssessments);
        }
        
        // Evict all specific filter caches for this user
        evictUserAssessmentsSpecificCaches(userId);
    }
    
    private void updateCacheAfterAssessmentDeletion(Long userId, AssessmentCacheDto deletedAssessment) {
        // Remove from general cache if it exists
        String generalCacheKey = generateGeneralCacheKey(userId);
        List<AssessmentCacheDto> cachedAssessments = getCachedAssessmentList(generalCacheKey);
        if (cachedAssessments != null) {
            cachedAssessments.removeIf(assessment -> assessment.getId().equals(deletedAssessment.getId()));
            cacheAssessmentList(generalCacheKey, cachedAssessments);
        }
        
        // Evict all specific filter caches for this user
        evictUserAssessmentsSpecificCaches(userId);
    }

    private void evictUserAssessmentsSpecificCaches(Long userId) {
        // Evict all specific filter caches but keep the general cache
        redisService.evictCache("cache:user_assessments:" + userId + ":*:*");
    }

}
