package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.dtos.FetchCandidateAttemptDto;
import com.delphi.delphi.dtos.NewCandidateAttemptDto;
import com.delphi.delphi.dtos.cache.AssessmentCacheDto;
import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.repositories.AssessmentRepository;
import com.delphi.delphi.repositories.CandidateRepository;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.CandidateAttemptService;
import com.delphi.delphi.utils.AttemptStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attempts")
public class CandidateAttemptController {
    private final AssessmentService assessmentService;
    private final CandidateAttemptService candidateAttemptService;
    private final AssessmentRepository assessmentRepository;
    private final CandidateRepository candidateRepository;

    public CandidateAttemptController(CandidateAttemptService candidateAttemptService, 
                                    AssessmentService assessmentService,
                                    AssessmentRepository assessmentRepository,
                                    CandidateRepository candidateRepository) {
        this.assessmentService = assessmentService;
        this.candidateAttemptService = candidateAttemptService;
        this.assessmentRepository = assessmentRepository;
        this.candidateRepository = candidateRepository;
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteCandidate(@Valid @RequestBody NewCandidateAttemptDto newCandidateAttemptDto) {
        try {
            // Fetch the candidate and assessment entities
            Candidate candidate = candidateRepository.findById(newCandidateAttemptDto.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + newCandidateAttemptDto.getCandidateId()));
            
            Assessment assessment = assessmentRepository.findById(newCandidateAttemptDto.getAssessmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id: " + newCandidateAttemptDto.getAssessmentId()));
            
            // Create a CandidateAttempt entity from the DTO
            CandidateAttempt candidateAttempt = new CandidateAttempt();
            //candidateAttempt.setGithubRepositoryLink(newCandidateAttemptDto.getGithubRepositoryLink());
            //candidateAttempt.setLanguageChoice(newCandidateAttemptDto.getLanguageChoice().orElse(null));
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
    
    // Create a new candidate attempt
    @PostMapping("/start")
    public ResponseEntity<?> startCandidateAttempt(@Valid @RequestBody NewCandidateAttemptDto newCandidateAttemptDto) {
        try {
            AssessmentCacheDto assessment = assessmentService.getAssessmentByIdCache(newCandidateAttemptDto.getAssessmentId());
            // return error if an invalid language choice is provided
            if (!assessment.getLanguageOptions().isEmpty() && newCandidateAttemptDto.getLanguageChoice().isPresent() && !assessment.getLanguageOptions().contains(newCandidateAttemptDto.getLanguageChoice().get())) {
                return ResponseEntity.badRequest().body("Language choice not supported for this assessment");
            }

            // Use the startAttempt method with the correct signature
            String languageChoice = newCandidateAttemptDto.getLanguageChoice().orElse(null);
            CandidateAttemptCacheDto createdAttempt = candidateAttemptService.startAttempt(
                newCandidateAttemptDto.getCandidateId(), 
                newCandidateAttemptDto.getAssessmentId(), 
                languageChoice);
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
    public ResponseEntity<?> getAllCandidateAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long assessmentId,
            @RequestParam(required = false) AttemptStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startedBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completedBefore) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService.getCandidateAttemptsWithFilters(
                candidateId, assessmentId, status, startedAfter, startedBefore, 
                completedAfter, completedBefore, pageable);
            List<FetchCandidateAttemptDto> attemptDtos = attempts.stream()
                    .map(FetchCandidateAttemptDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving candidate attempts: " + e.getMessage());
        }
    }
    
    // Update candidate attempt
    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateCandidateAttempt(@PathVariable Long id, @Valid @RequestBody NewCandidateAttemptDto attemptUpdates) {
        try {
            CandidateAttempt updateAttempt = new CandidateAttempt();
            updateAttempt.setGithubRepositoryLink(attemptUpdates.getGithubRepositoryLink());
            updateAttempt.setLanguageChoice(attemptUpdates.getLanguageChoice().orElse(null));
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
    // @DeleteMapping("/{id}/delete")
    // public ResponseEntity<?> deleteCandidateAttempt(@PathVariable Long id) {
    //     try {
    //         candidateAttemptService.deleteCandidateAttempt(id);
    //         return ResponseEntity.noContent().build();
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.notFound().build();
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error deleting candidate attempt: " + e.getMessage());
    //     }
    // }
    
    // Get attempts by candidate ID
    // @GetMapping("/candidate/{candidateId}/all")
    // public ResponseEntity<?> getAttemptsByCandidateId(
    //         @PathVariable Long candidateId,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByCandidateId(candidateId, pageable);
    //         Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
    //         return ResponseEntity.ok(attemptDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempts: " + e.getMessage());
    //     }
    // }
    
    // Get attempts by assessment ID
    // @GetMapping("/assessment/{assessmentId}/all")
    // public ResponseEntity<?> getAttemptsByAssessmentId(
    //         @PathVariable Long assessmentId,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByAssessmentId(assessmentId, pageable);
    //         Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
    //         return ResponseEntity.ok(attemptDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempts: " + e.getMessage());
    //     }
    // }
    
    // Get attempts by status
    // @GetMapping("/status/{status}/all")
    // public ResponseEntity<?> getAttemptsByStatus(
    //         @PathVariable AttemptStatus status,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByStatus(status, pageable);
    //         Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
    //         return ResponseEntity.ok(attemptDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempts: " + e.getMessage());
    //     }
    // }
    
    // Get attempt by candidate and assessment
    // @GetMapping("/candidate/{candidateId}/assessment/{assessmentId}/all")
    // public ResponseEntity<?> getAttemptByCandidateAndAssessment(
    //         @PathVariable Long candidateId,
    //         @PathVariable Long assessmentId) {
    //     try {
    //         Optional<CandidateAttempt> attempt = candidateAttemptService.getAttemptByCandidateAndAssessment(candidateId, assessmentId);
    //         if (attempt.isPresent()) {
    //             return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt.get()));
    //         } else {
    //             return ResponseEntity.notFound().build();
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempt: " + e.getMessage());
    //     }
    // }
    
    // Get attempts by language choice
    @GetMapping("/language/{languageChoice}/all")
    public ResponseEntity<?> getAttemptsByLanguageChoice(
            @PathVariable String languageChoice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService.getAttemptsByLanguageChoice(languageChoice, pageable);
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
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsCreatedBetween(startDate, endDate, pageable);
    //         Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
    //         return ResponseEntity.ok(attemptDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempts: " + e.getMessage());
    //     }
    // }
    
    // Get attempts started within date range
    // @GetMapping("/started-between/all")
    // public ResponseEntity<?> getAttemptsStartedBetween(
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsStartedBetween(startDate, endDate, pageable);
    //         Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
    //         return ResponseEntity.ok(attemptDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempts: " + e.getMessage());
    //     }
    // }
    
    // Get attempts completed within date range
    // @GetMapping("/completed-between/all")
    // public ResponseEntity<?> getAttemptsCompletedBetween(
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsSubmittedBetween(startDate, endDate, pageable);
    //         Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
    //         return ResponseEntity.ok(attemptDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error retrieving attempts: " + e.getMessage());
    //     }
    // }
    
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
            List<CandidateAttemptCacheDto> attempts = candidateAttemptService.getSubmittedAttemptsWithoutEvaluation(pageable);
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

    @PostMapping("/{id}/authenticate-candidate")
    public ResponseEntity<?> inviteCandidateToAssessment(@PathVariable Long id, @RequestBody String email) {
        try {
            Assessment assessment = assessmentService.getAssessmentById(id);
            if (assessment.getCandidates().stream().anyMatch(c -> c.getEmail().toLowerCase().equals(email.toLowerCase()))) {
                return ResponseEntity.ok("Candidate authenticated");
            }
            return ResponseEntity.badRequest().body("Candidate not authorized to take this assessment");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error authenticating candidate: " + e.getMessage() + " " + email + " - is this candidate authorized to take this assessment?");
        }
    }
    
    // Start attempt
    // @PostMapping("/{assessmentId}/start")
    // public ResponseEntity<?> startAttempt(@PathVariable Long assessmentId, @RequestBody Long candidateId, @RequestBody Optional<String> languageChoice) {
    //     try {
    //         Assessment assessment = assessmentService.getAssessmentByIdOrThrow(assessmentId);
    //         // return error if an invalid language choice is provided
    //         if (!assessment.getLanguageOptions().isEmpty() && languageChoice.isPresent() && !assessment.getLanguageOptions().contains(languageChoice.get())) {
    //             return ResponseEntity.badRequest().body("Language choice not supported for this assessment");
    //         }

    //         // return error if language choice is provided for an assessment that does not support it
    //         if (assessment.getLanguageOptions().isEmpty() && languageChoice.isPresent()) {
    //             return ResponseEntity.badRequest().body("This assessment does not support language choice.");
    //         }

    //         // return error if candidate is not found
    //         // Candidate candidate = candidateService.getCandidateByIdOrThrow(candidateId);

    //         // start the attempt
    //         CandidateAttempt attempt = candidateAttemptService.startAttempt(candidateId, assessmentId, languageChoice);
    //         return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
    //     } catch (IllegalStateException | IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body("Error starting attempt: " + e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error starting attempt: " + e.getMessage());
    //     }
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
    //     try {
    //         boolean isOverdue = candidateAttemptService.isAttemptOverdue(id);
    //         return ResponseEntity.ok(isOverdue);
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body("Error checking overdue status: " + e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error checking overdue status: " + e.getMessage());
    //     }
    // }
} 