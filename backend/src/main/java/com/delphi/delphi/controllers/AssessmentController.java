package com.delphi.delphi.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.FetchAssessmentDto;
import com.delphi.delphi.dtos.FetchCandidateAttemptDto;
import com.delphi.delphi.dtos.FetchCandidateDto;
import com.delphi.delphi.dtos.NewAssessmentDto;
import com.delphi.delphi.dtos.NewCandidateDto;
import com.delphi.delphi.dtos.NewUserMessageDto;
import com.delphi.delphi.dtos.PaginatedResponseDto;
import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.ChatMessageCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.dtos.filter_queries.GetAssessmentsDto;
import com.delphi.delphi.dtos.messaging.chat.PublishAssessmentCreationJobDto;
import com.delphi.delphi.dtos.messaging.chat.PublishChatJobDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.ChatService;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.enums.AssessmentStatus;
import com.delphi.delphi.utils.enums.JobStatus;
import com.delphi.delphi.utils.enums.JobType;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final DelegatingSecurityContextAsyncTaskExecutor taskExecutor;

    private final UserService userService;
    private final GithubService githubService;
    private final ChatService chatService;
    // private final ChatMessagePublisher chatMessagePublisher;
    private final JobRepository jobRepository;
    private final AssessmentService assessmentService;
    private final RabbitTemplate rabbitTemplate;
    private final String appInstallUrl;

    private final Logger log = LoggerFactory.getLogger(AssessmentController.class);

    public AssessmentController(AssessmentService assessmentService, UserService userService,
            GithubService githubService, JobRepository jobRepository, RabbitTemplate rabbitTemplate,
            ChatService chatService, @Value("${themus.github.app.name}") String githubAppName,
            DelegatingSecurityContextAsyncTaskExecutor taskExecutor) {
        this.assessmentService = assessmentService;
        // this.chatMessagePublisher = chatMessagePublisher;
        this.userService = userService;
        this.githubService = githubService;
        this.jobRepository = jobRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.chatService = chatService;
        // state specifies whether this installation is for a user or a candidate
        this.appInstallUrl = String.format("https://github.com/apps/%s/installations/new", githubAppName);
        this.taskExecutor = taskExecutor;
    }

    private UserCacheDto getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // TODO: add dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            // List<Assessment> assessments =
            // assessmentService.getAssessmentsByUserId(userId);
            // return ResponseEntity.ok(assessments);
            return ResponseEntity.ok("Dashboard");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving assessments: " + e.getMessage());
        }
    }

    // Create a new assessment
    // @PostMapping("/new")
    // public ResponseEntity<?> createAssessment(@Valid @RequestBody
    // NewAssessmentDto newAssessmentDto) {
    // try {
    // Assessment assessment = assessmentService.createAssessment(newAssessmentDto);

    // // publish to chat message queue

    // // get chat completion from the LLM
    // chatService.getChatCompletion(AssessmentCreationPrompts.USER_PROMPT,
    // Map.of("ROLE", newAssessmentDto.getRole(),
    // "ASSESSMENT_TYPE", newAssessmentDto.getAssessmentType(),
    // "DURATION", newAssessmentDto.getDuration(),
    // "SKILLS", newAssessmentDto.getSkills(),
    // "LANGUAGE_OPTIONS", newAssessmentDto.getLanguageOptions(),
    // "OTHER_DETAILS", newAssessmentDto.getOtherDetails()),
    // newAssessmentDto.getModel(),
    // assessment.getId(),
    // newAssessmentDto.getUserId(),
    // assessment.getChatHistory().getId());

    // // agent loop
    // // userChatService.getChatCompletion(assessmentCreationSystemPromptMessage,
    // // Map.of("role", newAssessmentDto.getRole(), "experienceLevel",
    // // newAssessmentDto.getExperienceLevel(), "languages",
    // // newAssessmentDto.getLanguages(), "libraries",
    // // newAssessmentDto.getLibraries(), "frameworks",
    // // newAssessmentDto.getFrameworks(), "tools", newAssessmentDto.getTools()),
    // // newAssessmentDto.getDescription(), "gpt-4o-mini");

    // return ResponseEntity.status(HttpStatus.CREATED).body(new
    // FetchAssessmentDto(assessment));
    // } catch (IllegalArgumentException e) {
    // return ResponseEntity.badRequest().body("Error creating assessment: " +
    // e.getMessage());
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Internal server error: " + e.getMessage());
    // }
    // }

    @GetMapping("/chat-history/{assessmentId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long assessmentId) {
        try {
            List<ChatMessageCacheDto> messages = chatService.getMessagesByAssessmentId(assessmentId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting chat history: " + e.getMessage());
        }
    }

    // /*
    // * Create a new assessment
    // * Publishes a job to the LLM topic
    // */
    // @PostMapping("/new")
    // public ResponseEntity<?> createAssessment(@Valid @RequestBody
    // NewAssessmentDto newAssessmentDto, HttpServletResponse response) {
    // try {
    // UserCacheDto user = getCurrentUser();
    // log.info("User's email: {}", user.getEmail());
    // log.info("User's id: {}", user.getId());
    // log.info("User's github access token: {}", user.getGithubAccessToken());
    // log.info("User's github username: {}", user.getGithubUsername());
    // log.info("User's github account type: {}", user.getGithubAccountType());
    // log.info("Assessmentcontroller - Checking if user is connected to github");

    // if (!userService.connectedGithub(user)) {
    // log.info("User is not connected to github, redirecting to installation
    // page");
    // response.setHeader("Location", appInstallUrl);
    // response.setStatus(302);
    // return ResponseEntity.status(HttpStatus.FOUND).build();
    // }

    // log.info("User is connected to github, validating credentials");
    // Map<String, Object> githubCredentialsValid =
    // githubService.validateGithubCredentials(user.getGithubAccessToken());
    // log.info("Github credentials validated: {}", githubCredentialsValid);
    // if (githubCredentialsValid == null) {
    // log.info("Github credentials are invalid, redirecting to installation page");
    // response.setHeader("Location", appInstallUrl);
    // response.setStatus(302);
    // return ResponseEntity.status(HttpStatus.FOUND).build();
    // }
    // log.info("Github credentials are valid, creating assessment");
    // // if user is not connected to github, redirect them to the installation page
    // log.info("assessment creation request received: {}", newAssessmentDto);
    // AssessmentCacheDto assessment =
    // assessmentService.createAssessment(newAssessmentDto, user);
    // log.info("assessment created: {}", assessment);

    // // Publish to assessment creation queue instead of direct call
    // log.info("Passing form data to chat message queue");
    // // Long jobId = UUID.randomUUID().getMostSignificantBits();
    // Job job = new Job(JobStatus.PENDING, JobType.CREATE_ASSESSMENT);
    // job = jobRepository.save(job);

    // log.info("Job created: {}", job.getId());

    // PublishAssessmentCreationJobDto publishAssessmentCreationJobDto = new
    // PublishAssessmentCreationJobDto(job.getId(), assessment, user,
    // newAssessmentDto.getModel());
    // rabbitTemplate.convertAndSend(TopicConfig.LLM_TOPIC_EXCHANGE_NAME,
    // TopicConfig.CREATE_ASSESSMENT_ROUTING_KEY, publishAssessmentCreationJobDto);
    // log.info("Assessment creation job published to queue");

    // // String requestId = chatMessagePublisher.publishChatCompletionRequest(
    // // AssessmentCreationPrompts.USER_PROMPT,
    // // Map.of("ROLE", newAssessmentDto.getRole(),
    // // "DURATION", newAssessmentDto.getDuration(),
    // // "SKILLS", newAssessmentDto.getSkills(),
    // // "LANGUAGE_OPTIONS", newAssessmentDto.getLanguageOptions(),
    // // "OTHER_DETAILS", newAssessmentDto.getDescription()),
    // // newAssessmentDto.getModel(),
    // // assessment.getId(),
    // // user.getId());

    // return ResponseEntity.status(HttpStatus.CREATED).body(
    // Map.of("jobId", job.getId(), "status", JobStatus.PENDING.toString(),
    // "assessment", new FetchAssessmentDto(assessment)));
    // //"chatRequestId", requestId));
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error creating assessment: " + e.getMessage());
    // }
    // }

    /*
     * Create a new assessment
     * Publishes a job to the LLM topic
     * SSE Endpoint for real-time assessment creation
     */
    @PostMapping(value = "/new", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> createAssessmentSse(@Valid @RequestBody NewAssessmentDto newAssessmentDto,
            HttpServletResponse response) {
        try {
            UserCacheDto user = getCurrentUser();
            log.info("User's email: {}", user.getEmail());
            log.info("User's id: {}", user.getId());
            log.info("User's github access token: {}", user.getGithubAccessToken());
            log.info("User's github username: {}", user.getGithubUsername());
            log.info("User's github account type: {}", user.getGithubAccountType());
            log.info("Assessmentcontroller - Checking if user is connected to github");

            // TODO: replace 302 responses with redirect URL responses
            if (!userService.connectedGithub(user)) {
                log.info("User is not connected to github, redirecting to installation page");
                return ResponseEntity.ok(Map.of("redirectUrl", userService.generateGitHubInstallUrl(user.getEmail()), "requiresRedirect", true));
            }

            log.info("User is connected to github, validating credentials");
            Map<String, Object> githubCredentialsValid = githubService
                    .validateGithubCredentials(user.getGithubAccessToken());
            log.info("Github credentials validated: {}", githubCredentialsValid);
            if (githubCredentialsValid == null) {
                log.info("Github credentials are invalid, redirecting to installation page");
                return ResponseEntity.ok(Map.of("redirectUrl", appInstallUrl, "requiresRedirect", true));
            }

            log.info("Github credentials are valid, creating assessment");
            log.info("assessment creation request received: {}", newAssessmentDto);
            AssessmentCacheDto assessment = assessmentService.createAssessment(newAssessmentDto, user);
            log.info("assessment created: {}", assessment);

            // Publish to assessment creation queue instead of direct call
            log.info("Passing form data to chat message queue");
            Job job = new Job(JobStatus.PENDING, JobType.CREATE_ASSESSMENT);
            job = jobRepository.save(job);
            final UUID jobId = job.getId();

            SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
            chatService.registerSseEmitter(jobId, emitter);

            // Handle emitter completion/error
            emitter.onCompletion(() -> {
                log.info("SSE emitter completed for assessment creation job: {}", jobId);
                chatService.removeSseEmitter(jobId);
            });

            emitter.onTimeout(() -> {
                log.info("SSE emitter timed out for assessment creation job: {}", jobId);
                chatService.removeSseEmitter(jobId);
                emitter.complete();
            });

            emitter.onError((ex) -> {
                log.error("SSE emitter error for assessment creation job: {}", jobId, ex);
                chatService.removeSseEmitter(jobId);
                emitter.completeWithError(ex);
            });

            log.info("Job created: {}", jobId);

            try {
                // Send job created confirmation
                emitter.send(SseEmitter.event()
                        .name("job_created")
                        .data(Map.of("jobId", jobId.toString(), "assessmentId", assessment.getId(), "status",
                                JobStatus.PENDING.toString())));
            } catch (IOException e) {
                log.error("Error sending job created confirmation: {}", e.getMessage());
                emitter.completeWithError(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error sending job created confirmation: " + e.getMessage());
            }

            CompletableFuture.runAsync(() -> {
                // Copy security context to async thread
                // SecurityContext securityContext = SecurityContextHolder.getContext();
                // SecurityContextHolder.setContext(securityContext);

                try {
                    PublishAssessmentCreationJobDto publishAssessmentCreationJobDto = new PublishAssessmentCreationJobDto(
                            jobId, assessment, user, newAssessmentDto.getModel());
                    rabbitTemplate.convertAndSend(TopicConfig.LLM_TOPIC_EXCHANGE_NAME,
                            TopicConfig.CREATE_ASSESSMENT_ROUTING_KEY, publishAssessmentCreationJobDto);
                    log.info("Assessment creation job published to queue");
                } catch (AmqpException e) {
                    log.error("Error publishing assessment creation job", e);
                    try {
                        emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of("error", e.getMessage())));
                        emitter.complete();
                    } catch (IOException ioEx) {
                        emitter.completeWithError(ioEx);
                    }
                } finally {
                    // TODO: see if this can be deleted
                    SecurityContextHolder.clearContext();
                }
            }, taskExecutor);

            return ResponseEntity.status(HttpStatus.CREATED).body(emitter);

        } catch (AmqpException e) {
            log.error("Error pushing assessment creation job to queue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error setting up SSE assessment creation: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error setting up SSE assessment creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error setting up SSE assessment creation: " + e.getMessage());
        }
    }

    // @GetMapping("/test-agent")
    // public ResponseEntity<?> testAgent() {
    // try {
    // ChatResponse chatResponse = chatService.testAgent("What is the weather in San
    // Francisco?", "z-ai/glm-4.5-air:free");
    // return ResponseEntity.ok(chatResponse);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error testing agent: " + e.getMessage());
    // }
    // }
    // Chat with the AI agent
    // @PostMapping("/chat")
    // public ResponseEntity<?> chat(@RequestBody NewUserMessageDto messageDto) {
    // try {
    // Assessment assessment =
    // assessmentService.getAssessmentByIdOrThrow(messageDto.getAssessmentId());
    // ChatResponse chatResponse =
    // chatService.getChatCompletion(messageDto.getMessage(), messageDto.getModel(),
    // messageDto.getAssessmentId(), messageDto.getUserId(),
    // assessment.getChatHistory().getId());
    // return ResponseEntity.ok(chatResponse);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error chatting with AI agent: " + e.getMessage());
    // }
    // }
    // Chat with the AI agent (SSE endpoint for real-time chat)
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> chatSse(@Valid @RequestBody NewUserMessageDto messageDto, HttpServletResponse response) {
        try {
            UserCacheDto user = getCurrentUser();

            if (!userService.connectedGithub(user)) {
                log.info("User is not connected to github, redirecting to installation page");
                return ResponseEntity.ok(Map.of("redirectUrl", appInstallUrl, "requiresRedirect", true));
            }
            log.info("User is connected to github, proceeding...");

            AssessmentCacheDto assessment = assessmentService.getAssessmentByIdCache(messageDto.getAssessmentId());

            Job job = new Job(JobStatus.PENDING, JobType.CHAT_COMPLETION);
            job = jobRepository.save(job);
            final UUID jobId = job.getId();
            
            SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
            chatService.registerSseEmitter(jobId, emitter);

            // Handle emitter completion/error
            emitter.onCompletion(() -> {
                log.info("SSE emitter completed for assessment creation job: {}", jobId);
                chatService.removeSseEmitter(jobId);
            });

            emitter.onTimeout(() -> {
                log.info("SSE emitter timed out for assessment creation job: {}", jobId);
                chatService.removeSseEmitter(jobId);
                emitter.complete();
            });

            emitter.onError((ex) -> {
                log.error("SSE emitter error for assessment creation job: {}", jobId, ex);
                chatService.removeSseEmitter(jobId);
                emitter.completeWithError(ex);
            });

            log.info("Job created: {}", jobId);
            
            try {
                // Send job created confirmation
                emitter.send(SseEmitter.event()
                        .name("job_created")
                        .data(Map.of("jobId", jobId.toString(), "assessmentId", assessment.getId(), "status",
                                JobStatus.PENDING.toString())));
            } catch (IOException e) {
                log.error("Error sending job created confirmation: {}", e.getMessage());
                emitter.completeWithError(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error sending job created confirmation: " + e.getMessage());
            }

            CompletableFuture.runAsync(() -> {
                // Copy security context to async thread
                // SecurityContext securityContext = SecurityContextHolder.getContext();
                // SecurityContextHolder.setContext(securityContext);

                try {
                    PublishChatJobDto publishChatJobDto = new PublishChatJobDto(jobId, messageDto.getMessage(),
                            assessment, user, messageDto.getModel());
                    rabbitTemplate.convertAndSend(TopicConfig.LLM_TOPIC_EXCHANGE_NAME, TopicConfig.LLM_CHAT_ROUTING_KEY,
                            publishChatJobDto);
                    log.info("Assessment creation job published to queue");
                } catch (AmqpException e) {
                    log.error("Error publishing assessment creation job", e);
                    try {
                        emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of("error", e.getMessage())));
                        emitter.complete();
                    } catch (IOException ioEx) {
                        emitter.completeWithError(ioEx);
                    }
                } finally {
                    // TODO: see if this can be deleted
                    SecurityContextHolder.clearContext();
                }
            }, taskExecutor);

            return ResponseEntity.status(HttpStatus.CREATED).body(emitter);

        } catch (AmqpException e) {
            log.error("Error pushing chat job to queue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error pushing chat job to queue: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error setting up SSE chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error setting up SSE chat: " + e.getMessage());
        }
    }

    // Chat with the AI agent (legacy synchronous endpoint)
    // @PostMapping("/chat")
    // public ResponseEntity<?> chat(@RequestBody NewUserMessageDto messageDto) {
    // try {
    // UserCacheDto user = getCurrentUser();
    // AssessmentCacheDto assessment =
    // assessmentService.getAssessmentByIdCache(messageDto.getAssessmentId());
    // // publish chat completion request to the chat message queue
    // log.info("Publishing chat completion request to the chat message queue");
    // // String jobId = UUID.randomUUID().toString();
    // Job job = new Job(JobStatus.PENDING, JobType.CHAT_COMPLETION);
    // job = jobRepository.save(job);

    // PublishChatJobDto publishChatJobDto = new PublishChatJobDto(job.getId(),
    // messageDto.getMessage(), assessment, user, messageDto.getModel());
    // rabbitTemplate.convertAndSend(TopicConfig.LLM_TOPIC_EXCHANGE_NAME,
    // TopicConfig.LLM_CHAT_ROUTING_KEY, publishChatJobDto);
    // log.info("Chat completion request published to queue");
    // // String requestId = chatMessagePublisher.publishChatCompletionRequest(
    // // messageDto.getMessage(),
    // // messageDto.getModel(),
    // // messageDto.getAssessmentId(),
    // // user.getId()
    // // );

    // // Return request ID for client to track the async response
    // return ResponseEntity.accepted().body(Map.of("jobId", job.getId().toString(),
    // "assessmentId", assessment.getId(), "status", JobStatus.PENDING.toString()));
    // } catch (AmqpException e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error submitting chat request: " + e.getMessage());
    // }
    // }

    // Add an existing candidate to an assessment
    @PostMapping("/{assessmentId}/invite-candidate/{candidateId}")
    public ResponseEntity<?> inviteCandidate(@PathVariable Long assessmentId, @PathVariable Long candidateId) {
        try {
            CandidateAttempt candidateAttempt = assessmentService.addCandidateFromExisting(assessmentId, candidateId);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(candidateAttempt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding candidate: " + e.getMessage());
        }
    }

    /*
     * Add a new candidate and invite them to an assessment
     */
    @PostMapping("/{assessmentId}/add-and-invite")
    public ResponseEntity<?> addAndInviteCandidate(@Valid @RequestBody NewCandidateDto newCandidateDto,
            @PathVariable Long assessmentId) {
        try {
            CandidateAttempt candidateAttempt = assessmentService.addCandidateFromNew(assessmentId,
                    newCandidateDto.getFirstName(), newCandidateDto.getLastName(), newCandidateDto.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchCandidateAttemptDto(candidateAttempt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating candidate: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /* Remove a candidate from an assessment */
    @DeleteMapping("/{assessmentId}/remove/{candidateId}")
    public ResponseEntity<?> removeCandidateFromAssessment(@PathVariable Long assessmentId,
            @PathVariable Long candidateId) {
        try {
            Candidate candidate = assessmentService.removeCandidateFromAssessment(assessmentId, candidateId);
            return ResponseEntity.ok(new FetchCandidateDto(candidate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing candidate: " + e.getMessage());
        }
    }

    // Get assessment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getAssessmentById(@PathVariable Long id) {
        try {
            AssessmentCacheDto assessment = assessmentService.getAssessmentByIdCache(id);
            // if (assessment.getStatus() == AssessmentStatus.ACTIVE) {
            // return ResponseEntity.ok(new FetchAssessmentDto(assessment));
            // } else {
            // return ResponseEntity.notFound().build();
            // }
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error retrieving assessment: " + e.getMessage());
        }
    }

    // Get all assessments with pagination and filtering for the current user
    @GetMapping("/filter")
    public ResponseEntity<?> getAllAssessments(GetAssessmentsDto getAssessmentsDto) {
        try {
            UserCacheDto user = getCurrentUser();

            Sort sort = getAssessmentsDto.getSortDirection().equalsIgnoreCase("asc")
                    ? Sort.by(getAssessmentsDto.getSortBy()).ascending()
                    : Sort.by(getAssessmentsDto.getSortBy()).descending();

            Pageable pageable = PageRequest.of(getAssessmentsDto.getPage(), getAssessmentsDto.getSize(), sort);
            PaginatedResponseDto<AssessmentCacheDto> paginatedResponse = assessmentService.getAssessmentsWithFilters(
                    user, getAssessmentsDto.getStatus(), getAssessmentsDto.getCreatedAfter(),
                    getAssessmentsDto.getCreatedBefore(), getAssessmentsDto.getAssessmentStartDate(),
                    getAssessmentsDto.getAssessmentEndDate(), getAssessmentsDto.getSkills(),
                    getAssessmentsDto.getLanguageOptions(), pageable);

            // Convert AssessmentCacheDto to FetchAssessmentDto
            List<FetchAssessmentDto> assessmentDtos = paginatedResponse.getContent().stream()
                    .map(FetchAssessmentDto::new)
                    .collect(Collectors.toList());

            // Create response with pagination metadata
            PaginatedResponseDto<FetchAssessmentDto> response = new PaginatedResponseDto<>(
                    assessmentDtos,
                    paginatedResponse.getPage(),
                    paginatedResponse.getSize(),
                    paginatedResponse.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving assessments: " + e.getMessage());
        }
    }

    // Update assessment
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssessment(@PathVariable Long id,
            @Valid @RequestBody NewAssessmentDto assessmentUpdates) {
        try {
            Assessment updateAssessment = new Assessment();
            updateAssessment.setName(assessmentUpdates.getName());
            updateAssessment.setDescription(assessmentUpdates.getDescription());
            updateAssessment.setRole(assessmentUpdates.getRole());
            updateAssessment.setStartDate(assessmentUpdates.getStartDate());
            updateAssessment.setEndDate(assessmentUpdates.getEndDate());
            updateAssessment.setDuration(assessmentUpdates.getDuration());
            updateAssessment.setSkills(assessmentUpdates.getSkills());
            updateAssessment.setLanguageOptions(assessmentUpdates.getLanguageOptions());
            updateAssessment.setMetadata(assessmentUpdates.getMetadata());
            updateAssessment.setDetails(assessmentUpdates.getDetails());
            updateAssessment.setStatus(assessmentUpdates.getStatus() == null ? null
                    : AssessmentStatus.valueOf(assessmentUpdates.getStatus().toUpperCase()));
            AssessmentCacheDto updatedAssessment = assessmentService.updateAssessment(id, updateAssessment);
            return ResponseEntity.ok(new FetchAssessmentDto(updatedAssessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating assessment: " + e.getMessage());
        }
    }

    // Delete assessment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssessment(@PathVariable Long id) {
        try {
            assessmentService.deleteAssessment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting assessment: " + e.getMessage());
        }
    }

    // Get assessments by user ID
    // @GetMapping("/get")
    // public ResponseEntity<?> getAssessmentsByUser(
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // User user = getCurrentUser();
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments =
    // assessmentService.getAssessmentsByUserId(user.getId(), pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving assessments: " + e.getMessage());
    // }
    // }

    // Get assessments by status
    // @GetMapping("/status/{status}")
    // public ResponseEntity<?> getAssessmentsByStatus(
    // @PathVariable AssessmentStatus status,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments =
    // assessmentService.getAssessmentsByStatus(status, pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving assessments: " + e.getMessage());
    // }
    // }

    // // Get assessments by type
    // @GetMapping("/type/{type}")
    // public ResponseEntity<?> getAssessmentsByType(
    // @PathVariable AssessmentType type,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments = assessmentService.getAssessmentsByType(type,
    // pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving assessments: " + e.getMessage());
    // }
    // }

    // Search assessments by name
    @GetMapping("/search/name")
    public ResponseEntity<?> searchAssessmentsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UserCacheDto user = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size);
            List<AssessmentCacheDto> assessments = assessmentService.searchAssessmentsByName(user, name, pageable);
            List<FetchAssessmentDto> assessmentDtos = assessments.stream()
                    .map(FetchAssessmentDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching assessments: " + e.getMessage());
        }
    }

    // Search assessments by role name
    @GetMapping("/search/role")
    public ResponseEntity<?> searchAssessmentsByRoleName(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UserCacheDto user = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size);
            List<AssessmentCacheDto> assessments = assessmentService.searchAssessmentsByRoleName(user, role, pageable);
            List<FetchAssessmentDto> assessmentDtos = assessments.stream()
                    .map(FetchAssessmentDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching assessments: " + e.getMessage());
        }
    }

    // Get assessments within date range
    // @GetMapping("/date-range")
    // public ResponseEntity<?> getAssessmentsInDateRange(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime endDate,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments =
    // assessmentService.getAssessmentsInDateRange(startDate, endDate, pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving assessments: " + e.getMessage());
    // }
    // }

    // Get active assessments
    // @GetMapping("/active")
    // public ResponseEntity<?> getActiveAssessments(
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments =
    // assessmentService.getActiveAssessmentsInDateRange(LocalDateTime.now(),
    // pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving active assessments: " + e.getMessage());
    // }
    // }

    // Get assessments by duration range
    // @GetMapping("/duration-range")
    // public ResponseEntity<?> getAssessmentsByDurationRange(
    // @RequestParam Integer minDuration,
    // @RequestParam Integer maxDuration,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments =
    // assessmentService.getAssessmentsByDurationRange(minDuration, maxDuration,
    // pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving assessments: " + e.getMessage());
    // }
    // }

    // Get assessments by skill
    @GetMapping("/skill/{skill}")
    public ResponseEntity<?> getAssessmentsBySkill(
            @PathVariable String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UserCacheDto user = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size);
            List<AssessmentCacheDto> assessments = assessmentService.getAssessmentsBySkill(user, skill, pageable);
            List<FetchAssessmentDto> assessmentDtos = assessments.stream()
                    .map(FetchAssessmentDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving assessments: " + e.getMessage());
        }
    }

    // Get assessments by language option
    // @GetMapping("/language/{language}")
    // public ResponseEntity<?> getAssessmentsByLanguageOption(
    // @PathVariable String language,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<Assessment> assessments =
    // assessmentService.getAssessmentsByLanguageOption(language, pageable);
    // Page<FetchAssessmentDto> assessmentDtos =
    // assessments.map(FetchAssessmentDto::new);

    // return ResponseEntity.ok(assessmentDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving assessments: " + e.getMessage());
    // }
    // }

    // Count assessments by user and status
    @GetMapping("/count/status/{status}")
    public ResponseEntity<?> countAssessmentsByUserAndStatus(
            @PathVariable AssessmentStatus status) {
        try {
            UserCacheDto user = getCurrentUser();
            Long count = assessmentService.countAssessmentsByUserAndStatus(user, status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting assessments: " + e.getMessage());
        }
    }

    // Activate assessment
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateAssessment(@PathVariable Long id) {
        try {
            Assessment assessment = assessmentService.activateAssessment(id);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error activating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating assessment: " + e.getMessage());
        }
    }

    // Deactivate assessment
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateAssessment(@PathVariable Long id) {
        try {
            Assessment assessment = assessmentService.deactivateAssessment(id);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error deactivating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating assessment: " + e.getMessage());
        }
    }

    // Publish assessment
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishAssessment(@PathVariable Long id) {
        try {
            Assessment assessment = assessmentService.publishAssessment(id);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error publishing assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error publishing assessment: " + e.getMessage());
        }
    }

    // Update skills
    @PutMapping("/{id}/skills")
    public ResponseEntity<?> updateSkills(
            @PathVariable Long id,
            @RequestBody List<String> skills) {
        try {
            Assessment assessment = assessmentService.updateSkills(id, skills);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating skills: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating skills: " + e.getMessage());
        }
    }

    // Add skill
    @PostMapping("/{id}/skills")
    public ResponseEntity<?> addSkill(
            @PathVariable Long id,
            @RequestParam String skill) {
        try {
            Assessment assessment = assessmentService.addSkill(id, skill);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error adding skill: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding skill: " + e.getMessage());
        }
    }

    // Remove skill
    @DeleteMapping("/{id}/skills/{skill}")
    public ResponseEntity<?> removeSkill(
            @PathVariable Long id,
            @PathVariable String skill) {
        try {
            Assessment assessment = assessmentService.removeSkill(id, skill);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error removing skill: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing skill: " + e.getMessage());
        }
    }

    // Update language options
    @PutMapping("/{id}/language-options")
    public ResponseEntity<?> updateLanguageOptions(
            @PathVariable Long id,
            @RequestBody List<String> languageOptions) {
        try {
            Assessment assessment = assessmentService.updateLanguageOptions(id, languageOptions);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating language options: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating language options: " + e.getMessage());
        }
    }

    // Update metadata
    @PutMapping("/{id}/metadata/new")
    public ResponseEntity<?> updateMetadata(
            @PathVariable Long id,
            @RequestBody Map<String, String> metadata) {
        try {
            Assessment assessment = assessmentService.updateMetadata(id, metadata);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating metadata: " + e.getMessage());
        }
    }

    // @PutMapping("/{id}/metadata")
    // public ResponseEntity<?> updateGithubRepo(
    // @PathVariable Long id,
    // @RequestBody String key, @RequestBody String value) {
    // try {
    // Assessment assessment = assessmentService.updateMetadata(id, Map.of(key,
    // value));
    // return ResponseEntity.ok(new FetchAssessmentDto(assessment));
    // } catch (IllegalArgumentException e) {
    // return ResponseEntity.badRequest().body("Error updating metadata: " +
    // e.getMessage());
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error updating metadata: " + e.getMessage());
    // }
    // }
}