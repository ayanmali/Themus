package com.delphi.delphi.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.AuthenticateCandidateDto;
import com.delphi.delphi.dtos.FetchCandidateAttemptDto;
import com.delphi.delphi.dtos.InviteCandidateDto;
import com.delphi.delphi.dtos.PaginatedResponseDto;
import com.delphi.delphi.dtos.PreviewAssessmentDto;
import com.delphi.delphi.dtos.StartAttemptDto;
import com.delphi.delphi.dtos.UpdateCandidateAttemptDto;
import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.delphi.delphi.dtos.filter_queries.GetCandidateAttemptsDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.repositories.AssessmentRepository;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.CandidateAttemptService;
import com.delphi.delphi.services.CandidateService;
import com.delphi.delphi.services.EncryptionService;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.utils.enums.AttemptStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attempts")
public class CandidateAttemptController {

    private final EncryptionService encryptionService;

    private final CandidateService candidateService;
    private final AssessmentService assessmentService;
    private final CandidateAttemptService candidateAttemptService;
    private final AssessmentRepository assessmentRepository;
    private final CandidateRepository candidateRepository;
    private final RedisService redisService;
    private final GithubService githubService;
    private final String tokenCacheKeyPrefix = "candidate_github_token:";

    public CandidateAttemptController(CandidateAttemptService candidateAttemptService,
            AssessmentService assessmentService,
            AssessmentRepository assessmentRepository,
            CandidateRepository candidateRepository, CandidateService candidateService,
            RedisService redisService, GithubService githubService,
            @Value("${github.app.name}") String githubAppName, EncryptionService encryptionService) {
        this.assessmentService = assessmentService;
        this.candidateAttemptService = candidateAttemptService;
        this.assessmentRepository = assessmentRepository;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.redisService = redisService;
        this.githubService = githubService;
        this.encryptionService = encryptionService;
    }

    // Invite a candidate to an assessment
    @PostMapping("/invite")
    public ResponseEntity<?> inviteCandidate(@Valid @RequestBody InviteCandidateDto inviteCandidateDto) {
        try {
            // Fetch the candidate and assessment entities
            
            Candidate candidate = candidateRepository.findById(inviteCandidateDto.getCandidateId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Candidate not found with id: " + inviteCandidateDto.getCandidateId()));

            Assessment assessment = assessmentRepository.findById(inviteCandidateDto.getAssessmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Assessment not found with id: " + inviteCandidateDto.getAssessmentId()));

            // Create a CandidateAttempt entity from the DTO
            CandidateAttempt candidateAttempt = new CandidateAttempt();
            // candidateAttempt.setGithubRepositoryLink(newCandidateAttemptDto.getGithubRepositoryLink());
            // candidateAttempt.setLanguageChoice(newCandidateAttemptDto.getLanguageChoice().orElse(null));
            candidateAttempt.setStatus(AttemptStatus.INVITED);
            candidateAttempt.setCandidate(candidate);
            candidateAttempt.setAssessment(assessment);

            CandidateAttemptCacheDto invitedAttempt = candidateAttemptService.inviteCandidate(candidateAttempt);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchCandidateAttemptDto(invitedAttempt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inviting candidate: " + e.getMessage());
        }
    }

    /**
     * check if provided email and password match. If so, generate a github install url and return it
     * @param authenticateCandidateDto
     * @return
     */
    @PostMapping("/live/authenticate")
    public ResponseEntity<?> authenticateCandidate(@Valid @RequestBody AuthenticateCandidateDto authenticateCandidateDto) {
        try {
            String url = candidateAttemptService.authenticateCandidate(authenticateCandidateDto);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error authenticating candidate: " + e.getMessage());
        }
    }

    /**
     * Update DB and create the candidate's github repo
     * @param startAttemptDto
     * @return
     */
    @PostMapping("/start")
    public ResponseEntity<?> startCandidateAttempt(@Valid @RequestBody StartAttemptDto startAttemptDto) {
        try {
            CandidateCacheDto candidate = candidateService.getCandidateByEmail(startAttemptDto.getCandidateEmail());
            AssessmentCacheDto assessment = assessmentService.getAssessmentByIdCache(startAttemptDto.getAssessmentId());

            // Updating DB with full GitHub repository URL
            CandidateAttemptCacheDto createdAttempt = candidateAttemptService.startAttempt(
                    candidate,
                    assessment,
                    startAttemptDto.getLanguageChoice());

            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchCandidateAttemptDto(createdAttempt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating candidate attempt: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    // Get candidate attempt by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCandidateAttemptById(@PathVariable Long id) {
        try {
            CandidateAttemptCacheDto attempt = candidateAttemptService.getCandidateAttemptById(id);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving candidate attempt: " + e.getMessage());
        }
    }

    // Get all candidate attempts with pagination and filtering
    @GetMapping("/filter")
    public ResponseEntity<?> getAllCandidateAttempts(GetCandidateAttemptsDto getCandidateAttemptsDto) {
        try {
            Sort sort = getCandidateAttemptsDto.getSortDirection().equalsIgnoreCase("asc")
                    ? Sort.by(getCandidateAttemptsDto.getSortBy()).ascending()
                    : Sort.by(getCandidateAttemptsDto.getSortBy()).descending();

            Pageable pageable = PageRequest.of(getCandidateAttemptsDto.getPage(), getCandidateAttemptsDto.getSize(),
                    sort);
            PaginatedResponseDto<CandidateAttemptCacheDto> paged = candidateAttemptService.getCandidateAttemptsWithFilters(
                    getCandidateAttemptsDto.getCandidateId(), getCandidateAttemptsDto.getAssessmentId(),
                    getCandidateAttemptsDto.getAttemptStatuses(), getCandidateAttemptsDto.getStartedAfter(),
                    getCandidateAttemptsDto.getStartedBefore(),
                    getCandidateAttemptsDto.getCompletedAfter(), getCandidateAttemptsDto.getCompletedBefore(),
                    pageable);
            List<FetchCandidateAttemptDto> attemptDtos = paged.getContent().stream()
                    .map(FetchCandidateAttemptDto::new)
                    .collect(Collectors.toList());

            PaginatedResponseDto<FetchCandidateAttemptDto> response = new PaginatedResponseDto<>(
                attemptDtos,
                paged.getPage(),
                paged.getSize(),
                paged.getTotalElements()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving candidate attempts: " + e.getMessage());
        }
    }

    // Update candidate attempt
    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateCandidateAttempt(@PathVariable Long id,
            @Valid @RequestBody UpdateCandidateAttemptDto attemptUpdates) {
        try {
            CandidateAttempt updateAttempt = new CandidateAttempt();
            updateAttempt.setGithubRepositoryLink(attemptUpdates.getGithubRepositoryLink());
            updateAttempt.setLanguageChoice(attemptUpdates.getLanguageChoiceOptional().orElse(null));
            updateAttempt.setStatus(attemptUpdates.getStatus());

            CandidateAttemptCacheDto updatedAttempt = candidateAttemptService.updateCandidateAttempt(id, updateAttempt);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(updatedAttempt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating candidate attempt: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating candidate attempt: " + e.getMessage());
        }
    }

    // Delete candidate attempt
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteCandidateAttempt(@PathVariable Long id) {
        try {
            candidateAttemptService.deleteCandidateAttempt(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting candidate attempt: " + e.getMessage());
        }
    }

    // Get attempts by candidate ID
    // @GetMapping("/candidate/{candidateId}/all")
    // public ResponseEntity<?> getAttemptsByCandidateId(
    // @PathVariable Long candidateId,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<CandidateAttempt> attempts =
    // candidateAttemptService.getAttemptsByCandidateId(candidateId, pageable);
    // Page<FetchCandidateAttemptDto> attemptDtos =
    // attempts.map(FetchCandidateAttemptDto::new);

    // return ResponseEntity.ok(attemptDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempts: " + e.getMessage());
    // }
    // }

    // Get attempts by assessment ID
    // @GetMapping("/assessment/{assessmentId}/all")
    // public ResponseEntity<?> getAttemptsByAssessmentId(
    // @PathVariable Long assessmentId,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<CandidateAttempt> attempts =
    // candidateAttemptService.getAttemptsByAssessmentId(assessmentId, pageable);
    // Page<FetchCandidateAttemptDto> attemptDtos =
    // attempts.map(FetchCandidateAttemptDto::new);

    // return ResponseEntity.ok(attemptDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempts: " + e.getMessage());
    // }
    // }

    // Get attempts by status
    // @GetMapping("/status/{status}/all")
    // public ResponseEntity<?> getAttemptsByStatus(
    // @PathVariable AttemptStatus status,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<CandidateAttempt> attempts =
    // candidateAttemptService.getAttemptsByStatus(status, pageable);
    // Page<FetchCandidateAttemptDto> attemptDtos =
    // attempts.map(FetchCandidateAttemptDto::new);

    // return ResponseEntity.ok(attemptDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempts: " + e.getMessage());
    // }
    // }

    // Get attempt by candidate and assessment
    // @GetMapping("/candidate/{candidateId}/assessment/{assessmentId}/all")
    // public ResponseEntity<?> getAttemptByCandidateAndAssessment(
    // @PathVariable Long candidateId,
    // @PathVariable Long assessmentId) {
    // try {
    // Optional<CandidateAttempt> attempt =
    // candidateAttemptService.getAttemptByCandidateAndAssessment(candidateId,
    // assessmentId);
    // if (attempt.isPresent()) {
    // return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt.get()));
    // } else {
    // return ResponseEntity.notFound().build();
    // }
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempt: " + e.getMessage());
    // }
    // }

    // Get attempts by language choice
    @GetMapping("/language/{languageChoice}/all")
    public ResponseEntity<?> getAttemptsByLanguageChoice(
            @PathVariable String languageChoice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService
                    .getAttemptsByLanguageChoice(languageChoice, pageable);
            List<FetchCandidateAttemptDto> attemptDtos = attempts.stream()
                    .map(FetchCandidateAttemptDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving attempts: " + e.getMessage());
        }
    }

    // Get attempts created within date range
    // @GetMapping("/created-between/all")
    // public ResponseEntity<?> getAttemptsCreatedBetween(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime endDate,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<CandidateAttempt> attempts =
    // candidateAttemptService.getAttemptsCreatedBetween(startDate, endDate,
    // pageable);
    // Page<FetchCandidateAttemptDto> attemptDtos =
    // attempts.map(FetchCandidateAttemptDto::new);

    // return ResponseEntity.ok(attemptDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempts: " + e.getMessage());
    // }
    // }

    // Get attempts started within date range
    // @GetMapping("/started-between/all")
    // public ResponseEntity<?> getAttemptsStartedBetween(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime endDate,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<CandidateAttempt> attempts =
    // candidateAttemptService.getAttemptsStartedBetween(startDate, endDate,
    // pageable);
    // Page<FetchCandidateAttemptDto> attemptDtos =
    // attempts.map(FetchCandidateAttemptDto::new);

    // return ResponseEntity.ok(attemptDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempts: " + e.getMessage());
    // }
    // }

    // Get attempts completed within date range
    // @GetMapping("/completed-between/all")
    // public ResponseEntity<?> getAttemptsCompletedBetween(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // LocalDateTime endDate,
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<CandidateAttempt> attempts =
    // candidateAttemptService.getAttemptsSubmittedBetween(startDate, endDate,
    // pageable);
    // Page<FetchCandidateAttemptDto> attemptDtos =
    // attempts.map(FetchCandidateAttemptDto::new);

    // return ResponseEntity.ok(attemptDtos);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error retrieving attempts: " + e.getMessage());
    // }
    // }

    @GetMapping("/live/{assessmentId}")
    public ResponseEntity<?> fetchAssessmentForPreview(@PathVariable Long assessmentId) {
        Assessment assessment = assessmentService.getAssessmentById(assessmentId);
        return ResponseEntity.ok(new PreviewAssessmentDto(assessment));
    }

    // for candidates to generate a github install url
    // @PostMapping("/live/github/generate-install-url")
    // public ResponseEntity<?> generateGitHubInstallUrl(@RequestBody GenerateInstallUrlDto request) {
    //     try {
            
    //         String candidateEmail = request.getCandidateEmail();
    //         String plainTextPassword = request.getPlainTextPassword();

    //         String randomString = UUID.randomUUID().toString();
    //         redisService.setWithExpiration(githubCacheKeyPrefix + candidateEmail, randomString, 10, TimeUnit.MINUTES);
    //         String installUrl = String.format("%s?state=%s_candidate_%s", appInstallBaseUrl, randomString, candidateEmail);
    //         return ResponseEntity.ok(installUrl);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error generating GitHub install URL: " + e.getMessage());
    //     }
    // }

    @GetMapping("/live/{assessmentId}/can-take-assessment")
    public ResponseEntity<?> canTakeAssessment(@PathVariable Long assessmentId, @RequestBody String candidateEmail) {
        // TODO: check if this email address corresponds to a valid candidate attempt in
        // the DB
        // if it does, then we can just redirect to the assessment page
        Assessment assessment = assessmentService.getAssessmentById(assessmentId);
        CandidateCacheDto candidate = candidateService.getCandidateByEmail(candidateEmail);

        if (assessment.getCandidateAttempts().stream()
                .anyMatch(attempt -> attempt.getCandidate().getId().equals(candidate.getId())
                        && attempt.getStatus().equals(AttemptStatus.INVITED))) {
            return ResponseEntity.ok(
                    Map.of("result", true,
                            "attemptId", assessment.getCandidateAttempts().stream()
                                    .filter(attempt -> attempt.getCandidate().getId().equals(candidate.getId())
                                            && attempt.getStatus().equals(AttemptStatus.INVITED))
                                    .findFirst()
                                    .get()
                                    .getId()));
        }
        return ResponseEntity.ok(
                Map.of("result", false));
    }

    @GetMapping("/live/has-valid-github-token")
    public ResponseEntity<?> hasValidGithubToken(@RequestBody String email) {
        try {
            boolean hasValidGithubToken = candidateAttemptService.hasValidGithubToken(email);
            return ResponseEntity.ok(Map.of("result", hasValidGithubToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking if candidate has valid Github token: " + e.getMessage());
        }
    }

    //////////////

    // Get overdue attempts
    @GetMapping("/overdue/all")
    public ResponseEntity<?> getOverdueAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService.getOverdueAttempts(pageable);
            List<FetchCandidateAttemptDto> attemptDtos = attempts.stream()
                    .map(FetchCandidateAttemptDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving overdue attempts: " + e.getMessage());
        }
    }

    // Get attempts with evaluation
    @GetMapping("/with-evaluation/all")
    public ResponseEntity<?> getAttemptsWithEvaluation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService.getAttemptsWithEvaluation(pageable);
            List<FetchCandidateAttemptDto> attemptDtos = attempts.stream()
                    .map(FetchCandidateAttemptDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving attempts: " + e.getMessage());
        }
    }

    // Get completed attempts without evaluation
    @GetMapping("/submitted/all")
    public ResponseEntity<?> getCompletedAttemptsWithoutEvaluation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService
                    .getSubmittedAttemptsWithoutEvaluation(pageable);
            List<FetchCandidateAttemptDto> attemptDtos = attempts.stream()
                    .map(FetchCandidateAttemptDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving attempts: " + e.getMessage());
        }
    }

    // Count attempts by assessment and status
    @GetMapping("/count/assessment/{assessmentId}/status/{status}")
    public ResponseEntity<?> countAttemptsByAssessmentAndStatus(
            @PathVariable Long assessmentId,
            @PathVariable AttemptStatus status) {
        try {
            Long count = candidateAttemptService.countAttemptsByAssessmentAndStatus(assessmentId, status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting attempts: " + e.getMessage());
        }
    }

    // Count attempts by candidate
    @GetMapping("/count/candidate/{candidateId}")
    public ResponseEntity<?> countAttemptsByCandidate(@PathVariable Long candidateId) {
        try {
            Long count = candidateAttemptService.countAttemptsByCandidate(candidateId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting attempts: " + e.getMessage());
        }
    }

    // Get attempt with details
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getAttemptWithDetails(@PathVariable Long id) {
        try {
            CandidateAttemptCacheDto attempt = candidateAttemptService.getAttemptWithDetails(id);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving attempt details: " + e.getMessage());
        }
    }

    // @PostMapping("/{id}/authenticate-candidate")
    // public ResponseEntity<?> inviteCandidateToAssessment(@PathVariable Long id, @RequestBody String email) {
    //     try {
    //         Assessment assessment = assessmentService.getAssessmentById(id);
    //         if (assessment.getCandidates().stream()
    //                 .anyMatch(c -> c.getEmail().toLowerCase().equals(email.toLowerCase()))) {
    //             return ResponseEntity.ok("Candidate authenticated");
    //         }
    //         return ResponseEntity.badRequest().body("Candidate not authorized to take this assessment");

    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error authenticating candidate: " + e.getMessage() + " " + email
    //                         + " - is this candidate authorized to take this assessment?");
    //     }
    // }

    // Start attempt
    // @PostMapping("/{assessmentId}/start")
    // public ResponseEntity<?> startAttempt(@PathVariable Long assessmentId,
    // @RequestBody Long candidateId, @RequestBody Optional<String> languageChoice)
    // {
    // try {
    // Assessment assessment =
    // assessmentService.getAssessmentByIdOrThrow(assessmentId);
    // // return error if an invalid language choice is provided
    // if (!assessment.getLanguageOptions().isEmpty() && languageChoice.isPresent()
    // && !assessment.getLanguageOptions().contains(languageChoice.get())) {
    // return ResponseEntity.badRequest().body("Language choice not supported for
    // this assessment");
    // }

    // // return error if language choice is provided for an assessment that does
    // not support it
    // if (assessment.getLanguageOptions().isEmpty() && languageChoice.isPresent())
    // {
    // return ResponseEntity.badRequest().body("This assessment does not support
    // language choice.");
    // }

    // // return error if candidate is not found
    // // Candidate candidate =
    // candidateService.getCandidateByIdOrThrow(candidateId);

    // // start the attempt
    // CandidateAttempt attempt = candidateAttemptService.startAttempt(candidateId,
    // assessmentId, languageChoice);
    // return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
    // } catch (IllegalStateException | IllegalArgumentException e) {
    // return ResponseEntity.badRequest().body("Error starting attempt: " +
    // e.getMessage());
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error starting attempt: " + e.getMessage());
    // }
    // }

    // Submit attempt
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitAttempt(
            @PathVariable Long id,
            @RequestParam(required = false) String githubRepositoryLink) {
        try {
            CandidateAttemptCacheDto attempt = candidateAttemptService.submitAttempt(id, githubRepositoryLink);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error submitting attempt: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting attempt: " + e.getMessage());
        }
    }

    // Mark as evaluated
    @PostMapping("/{id}/evaluate")
    public ResponseEntity<?> markAsEvaluated(@PathVariable Long id) {
        try {
            CandidateAttemptCacheDto attempt = candidateAttemptService.markAsEvaluated(id);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error marking as evaluated: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking as evaluated: " + e.getMessage());
        }
    }

    // Check if attempt is overdue
    // @GetMapping("/{id}/is-overdue")
    // public ResponseEntity<?> isAttemptOverdue(@PathVariable Long id) {
    // try {
    // boolean isOverdue = candidateAttemptService.isAttemptOverdue(id);
    // return ResponseEntity.ok(isOverdue);
    // } catch (IllegalArgumentException e) {
    // return ResponseEntity.badRequest().body("Error checking overdue status: " +
    // e.getMessage());
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Error checking overdue status: " + e.getMessage());
    // }
    // }
}