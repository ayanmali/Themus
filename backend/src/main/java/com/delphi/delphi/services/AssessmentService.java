package com.delphi.delphi.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import com.delphi.delphi.components.messaging.candidates.CandidateInvitationPublisher;
import com.delphi.delphi.dtos.NewAssessmentDto;
import com.delphi.delphi.dtos.PaginatedResponseDto;
import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.AssessmentRepository;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.repositories.UserRepository;
import com.delphi.delphi.specifications.AssessmentSpecifications;
import com.delphi.delphi.utils.AssessmentStatus;
import com.delphi.delphi.utils.AttemptStatus;
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

        evictUserAssessmentsCache(assessment.getUser().getId());
        return new AssessmentCacheDto(assessmentRepository.save(assessment));
    }

    @CachePut(value = "assessments", key = "#result.id")
    public AssessmentCacheDto createAssessment(NewAssessmentDto newAssessmentDto, UserCacheDto user) throws Exception {
        Assessment assessment = new Assessment();
        assessment.setName(newAssessmentDto.getName());
        assessment.setDescription(newAssessmentDto.getDescription());
        assessment.setRole(newAssessmentDto.getRole());
        assessment.setDuration(newAssessmentDto.getDuration());
        assessment.setSkills(newAssessmentDto.getSkills());
        assessment.setLanguageOptions(newAssessmentDto.getLanguageOptions());
        // TODO: replace w/ something else?
        assessment.setGithubRepoName(newAssessmentDto.getName().toLowerCase().replace(" ", "-") + "-"
                + String.valueOf(Instant.now().getEpochSecond()));
        assessment.setUser(userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId())));
        assessment.setStatus(AssessmentStatus.DRAFT);

        // create github repo for the assessment
        if (user.getGithubAccountType() == GithubAccountType.USER) {
            githubService.createPersonalRepo(user.getGithubAccessToken(), assessment.getGithubRepoName());
        } else {
            githubService.createOrgRepo(user.getGithubAccessToken(), user.getGithubUsername(),
                    assessment.getGithubRepoName());
        }

        log.info("repo created, setting github repository link for assessment: {}", assessment);
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
        evictUserAssessmentsCache(user.getId());
        return new AssessmentCacheDto(assessmentRepository.save(assessment));
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
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
    }

    // Get chat history by assessment ID
    @Cacheable(value = "chat_messages", key = "#id")
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessagesById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id))
                .getChatMessages();
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
     */
    @Transactional(readOnly = true)
    public PaginatedResponseDto<AssessmentCacheDto> getAssessmentsWithFilters(UserCacheDto user, AssessmentStatus status,
            LocalDateTime createdAfter, LocalDateTime createdBefore, LocalDateTime assessmentStartDate, LocalDateTime assessmentEndDate,
            List<String> skills, List<String> languageOptions, Pageable pageable) {
        String cacheKey = "cache:user_assessments:" + user.getId();

        // Check if cache exists
        List<AssessmentCacheDto> cachedAssessments = null;
        if (redisService.hasKey(cacheKey)) {
            cachedAssessments = (List<AssessmentCacheDto>) redisService.get(cacheKey);
            log.info("cache hit for assessments: {}", cacheKey);
        }

        // If cache doesn't exist, fetch from DB with all applicable filters
        if (cachedAssessments == null) {
            Specification<Assessment> spec = Specification.allOf(
                    AssessmentSpecifications.belongsToUser(user.getId()));

            // Fetch all assessments for the user within date range (no pagination at DB
            // level)
            cachedAssessments = assessmentRepository.findAll(spec).stream()
                    .map(AssessmentCacheDto::new)
                    .collect(Collectors.toList());

            // Store in cache for future requests
            redisService.set(cacheKey, cachedAssessments);
        }

        // Apply remaining filters in memory for better performance and flexibility
        List<AssessmentCacheDto> filteredAssessments = cachedAssessments.stream()
                .filter(assessment -> {
                    // Filter by status
                    if (status != null && assessment.getStatus() != status) {
                        return false;
                    }

                    // Filter by assessment start date
                    if (assessmentStartDate != null) {
                        if (assessment.getStartDate() == null || assessment.getStartDate().isBefore(assessmentStartDate)) {
                            return false;
                        }
                    }

                    // Filter by assessment end date
                    if (assessmentEndDate != null) {
                        if (assessment.getEndDate() == null || assessment.getEndDate().isAfter(assessmentEndDate)) {
                            return false;
                        }
                    }

                    // Filter by skills - assessment must have at least one of the required skills
                    if (skills != null && !skills.isEmpty()) {
                        if (assessment.getSkills() == null || assessment.getSkills().isEmpty()) {
                            return false;
                        }
                        boolean hasRequiredSkill = skills.stream()
                                .anyMatch(skill -> assessment.getSkills().contains(skill));
                        if (!hasRequiredSkill) {
                            return false;
                        }
                    }

                    // Filter by language options - assessment must have at least one of the
                    // required language options
                    if (languageOptions != null && !languageOptions.isEmpty()) {
                        if (assessment.getLanguageOptions() == null || assessment.getLanguageOptions().isEmpty()) {
                            return false;
                        }
                        boolean hasRequiredLanguage = languageOptions.stream()
                                .anyMatch(language -> assessment.getLanguageOptions().contains(language));
                        if (!hasRequiredLanguage) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Apply sorting in memory based on Pageable sort criteria
        if (pageable.getSort().isSorted()) {
            filteredAssessments = filteredAssessments.stream()
                    .sorted((a1, a2) -> {
                        for (Sort.Order order : pageable.getSort()) {
                            int comparison = 0;
                            comparison = switch (order.getProperty().toLowerCase()) {
                                case "id" -> a1.getId().compareTo(a2.getId());
                                case "name" -> (a1.getName() != null && a2.getName() != null)
                                        ? a1.getName().compareTo(a2.getName())
                                        : 0;
                                case "role" -> (a1.getRole() != null && a2.getRole() != null)
                                        ? a1.getRole().compareTo(a2.getRole())
                                        : 0;
                                case "status" -> (a1.getStatus() != null && a2.getStatus() != null)
                                        ? a1.getStatus().compareTo(a2.getStatus())
                                        : 0;
                                case "createddate" -> (a1.getCreatedDate() != null && a2.getCreatedDate() != null)
                                        ? a1.getCreatedDate().compareTo(a2.getCreatedDate())
                                        : 0;
                                case "updateddate" -> (a1.getUpdatedDate() != null && a2.getUpdatedDate() != null)
                                        ? a1.getUpdatedDate().compareTo(a2.getUpdatedDate())
                                        : 0;
                                case "startdate" -> (a1.getStartDate() != null && a2.getStartDate() != null)
                                        ? a1.getStartDate().compareTo(a2.getStartDate())
                                        : 0;
                                case "enddate" -> (a1.getEndDate() != null && a2.getEndDate() != null)
                                        ? a1.getEndDate().compareTo(a2.getEndDate())
                                        : 0;
                                case "duration" -> (a1.getDuration() != null && a2.getDuration() != null)
                                        ? a1.getDuration().compareTo(a2.getDuration())
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
        long totalElements = filteredAssessments.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        
        // Apply pagination in memory
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredAssessments.size());
        
        List<AssessmentCacheDto> paginatedAssessments;
        if (start >= filteredAssessments.size()) {
            paginatedAssessments = List.of();
        } else {
            paginatedAssessments = filteredAssessments.subList(start, end);
        }

        // Return paginated response with metadata
        return new PaginatedResponseDto<>(
            paginatedAssessments,
            pageable.getPageNumber(),
            pageable.getPageSize(),
            totalElements
        );
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

        evictUserAssessmentsCache(existingAssessment.getUser().getId());

        return new AssessmentCacheDto(assessmentRepository.save(existingAssessment));
    }

    // Delete assessment
    @CacheEvict(value = "assessments", beforeInvocation = true, key = "#id")
    public void deleteAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + id));
        assessmentRepository.delete(assessment);
        evictUserAssessmentsCache(assessment.getUser().getId());
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
        assessmentRepository.updateExpiredAssessments(LocalDate.now());
    }

    /**
     * Add a candidate to the list corresponding to the user's candidates cache
     * key(s)
     * 
     * @param userId    The user ID for which to add the candidate to the cache
     * @param candidate The candidate to add to the cache
     */
    private void evictUserAssessmentsCache(Long userId) {
        redisService.evictCache("cache:user_assessments:" + userId + ":*");
    }

}
