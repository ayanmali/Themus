package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.delphi.delphi.dtos.FetchCandidateAttemptDto;
import com.delphi.delphi.dtos.NewCandidateAttemptDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.services.CandidateAttemptService;
import com.delphi.delphi.utils.AttemptStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/candidate-attempts")
public class CandidateAttemptController {
    
    @Autowired
    private CandidateAttemptService candidateAttemptService;
    
    // Create a new candidate attempt
    @PostMapping
    public ResponseEntity<?> createCandidateAttempt(@Valid @RequestBody NewCandidateAttemptDto newCandidateAttemptDto) {
        try {
            CandidateAttempt candidateAttempt = new CandidateAttempt();
            candidateAttempt.setGithubRepositoryLink(newCandidateAttemptDto.getGithubRepositoryLink());
            candidateAttempt.setLanguageChoice(newCandidateAttemptDto.getLanguageChoice());
            candidateAttempt.setStatus(newCandidateAttemptDto.getStatus());
            candidateAttempt.setStartedDate(newCandidateAttemptDto.getStartedDate());
            
            // Set candidate relationship
            Candidate candidate = new Candidate();
            candidate.setId(newCandidateAttemptDto.getCandidateId());
            candidateAttempt.setCandidate(candidate);
            
            // Set assessment relationship
            Assessment assessment = new Assessment();
            assessment.setId(newCandidateAttemptDto.getAssessmentId());
            candidateAttempt.setAssessment(assessment);
            
            CandidateAttempt createdAttempt = candidateAttemptService.createCandidateAttempt(candidateAttempt);
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
            Optional<CandidateAttempt> attempt = candidateAttemptService.getCandidateAttemptById(id);
            if (attempt.isPresent()) {
                return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving candidate attempt: " + e.getMessage());
        }
    }
    
    // Get all candidate attempts with pagination
    @GetMapping
    public ResponseEntity<?> getAllCandidateAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAllCandidateAttempts(pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving candidate attempts: " + e.getMessage());
        }
    }
    
    // Update candidate attempt
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCandidateAttempt(@PathVariable Long id, @Valid @RequestBody NewCandidateAttemptDto attemptUpdates) {
        try {
            CandidateAttempt updateAttempt = new CandidateAttempt();
            updateAttempt.setGithubRepositoryLink(attemptUpdates.getGithubRepositoryLink());
            updateAttempt.setLanguageChoice(attemptUpdates.getLanguageChoice());
            updateAttempt.setStatus(attemptUpdates.getStatus());
            
            CandidateAttempt updatedAttempt = candidateAttemptService.updateCandidateAttempt(id, updateAttempt);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(updatedAttempt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating candidate attempt: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating candidate attempt: " + e.getMessage());
        }
    }
    
    // Delete candidate attempt
    @DeleteMapping("/{id}")
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
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<?> getAttemptsByCandidateId(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByCandidateId(candidateId, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get attempts by assessment ID
    @GetMapping("/assessment/{assessmentId}")
    public ResponseEntity<?> getAttemptsByAssessmentId(
            @PathVariable Long assessmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByAssessmentId(assessmentId, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get attempts by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAttemptsByStatus(
            @PathVariable AttemptStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByStatus(status, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get attempt by candidate and assessment
    @GetMapping("/candidate/{candidateId}/assessment/{assessmentId}")
    public ResponseEntity<?> getAttemptByCandidateAndAssessment(
            @PathVariable Long candidateId,
            @PathVariable Long assessmentId) {
        try {
            Optional<CandidateAttempt> attempt = candidateAttemptService.getAttemptByCandidateAndAssessment(candidateId, assessmentId);
            if (attempt.isPresent()) {
                return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempt: " + e.getMessage());
        }
    }
    
    // Get attempts by language choice
    @GetMapping("/language/{languageChoice}")
    public ResponseEntity<?> getAttemptsByLanguageChoice(
            @PathVariable String languageChoice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsByLanguageChoice(languageChoice, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get attempts created within date range
    @GetMapping("/created-between")
    public ResponseEntity<?> getAttemptsCreatedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsCreatedBetween(startDate, endDate, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get attempts started within date range
    @GetMapping("/started-between")
    public ResponseEntity<?> getAttemptsStartedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsStartedBetween(startDate, endDate, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get attempts completed within date range
    @GetMapping("/completed-between")
    public ResponseEntity<?> getAttemptsCompletedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsSubmittedBetween(startDate, endDate, pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get overdue attempts
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getOverdueAttempts(pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving overdue attempts: " + e.getMessage());
        }
    }
    
    // Get attempts with evaluation
    @GetMapping("/with-evaluation")
    public ResponseEntity<?> getAttemptsWithEvaluation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getAttemptsWithEvaluation(pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
            return ResponseEntity.ok(attemptDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempts: " + e.getMessage());
        }
    }
    
    // Get completed attempts without evaluation
    @GetMapping("/completed-no-evaluation")
    public ResponseEntity<?> getCompletedAttemptsWithoutEvaluation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CandidateAttempt> attempts = candidateAttemptService.getSubmittedAttemptsWithoutEvaluation(pageable);
            Page<FetchCandidateAttemptDto> attemptDtos = attempts.map(FetchCandidateAttemptDto::new);
            
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
            Optional<CandidateAttempt> attempt = candidateAttemptService.getAttemptWithDetails(id);
            if (attempt.isPresent()) {
                return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving attempt details: " + e.getMessage());
        }
    }
    
    // Start attempt
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startAttempt(@PathVariable Long id) {
        try {
            CandidateAttempt attempt = candidateAttemptService.startAttempt(id);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error starting attempt: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error starting attempt: " + e.getMessage());
        }
    }
    
    // Submit attempt
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitAttempt(
            @PathVariable Long id,
            @RequestParam(required = false) String githubRepositoryLink) {
        try {
            CandidateAttempt attempt = candidateAttemptService.submitAttempt(id, githubRepositoryLink);
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
            CandidateAttempt attempt = candidateAttemptService.markAsEvaluated(id);
            return ResponseEntity.ok(new FetchCandidateAttemptDto(attempt));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error marking as evaluated: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error marking as evaluated: " + e.getMessage());
        }
    }
    
    // Check if attempt is overdue
    @GetMapping("/{id}/is-overdue")
    public ResponseEntity<?> isAttemptOverdue(@PathVariable Long id) {
        try {
            boolean isOverdue = candidateAttemptService.isAttemptOverdue(id);
            return ResponseEntity.ok(isOverdue);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error checking overdue status: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error checking overdue status: " + e.getMessage());
        }
    }
} 