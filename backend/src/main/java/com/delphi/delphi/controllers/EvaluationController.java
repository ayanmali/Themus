package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.dtos.FetchEvaluationDto;
import com.delphi.delphi.dtos.NewEvaluationDto;
import com.delphi.delphi.dtos.cache.EvaluationCacheDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.Evaluation;
import com.delphi.delphi.services.EvaluationService;
import com.delphi.delphi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    
    private final EvaluationService evaluationService;
    
    private final UserService userService;

    public EvaluationController(EvaluationService evaluationService, UserService userService) {
        this.evaluationService = evaluationService;
        this.userService = userService;
    }

    private UserCacheDto getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    /**
     * Verifies that the current user owns the specified evaluation
     * @param evaluationId The ID of the evaluation to check
     * @throws IllegalArgumentException if the evaluation doesn't exist or doesn't belong to the current user
     */
    private void verifyEvaluationOwnership(Long evaluationId) {
        UserCacheDto currentUser = getCurrentUser();
        
        // Check if the evaluation belongs to the current user by checking if it exists in their evaluations
        List<EvaluationCacheDto> userEvaluations = evaluationService.getEvaluationsByUserId(currentUser.getId(), PageRequest.of(0, Integer.MAX_VALUE));
        boolean isOwned = userEvaluations.stream()
                .anyMatch(eval -> eval.getId().equals(evaluationId));
        
        if (!isOwned) {
            throw new IllegalArgumentException("Access denied: You can only access your own evaluations");
        }
    }
    // Create a new evaluation
    @PostMapping
    public ResponseEntity<?> createEvaluation(@Valid @RequestBody NewEvaluationDto newEvaluationDto) {
        try {
            Evaluation evaluation = new Evaluation();
            
            // Set candidate attempt relationship
            CandidateAttempt candidateAttempt = new CandidateAttempt();
            candidateAttempt.setId(newEvaluationDto.getCandidateAttemptId());
            evaluation.setCandidateAttempt(candidateAttempt);
            
            EvaluationCacheDto createdEvaluation = evaluationService.createEvaluation(evaluation);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchEvaluationDto(createdEvaluation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating evaluation: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
        }
    }
    
    // Get evaluation by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getEvaluationById(@PathVariable Long id) {
        try {
            verifyEvaluationOwnership(id);
            EvaluationCacheDto evaluation = evaluationService.getEvaluationById(id);
            return ResponseEntity.ok(new FetchEvaluationDto(evaluation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluation: " + e.getMessage());
        }
    }
    
    // Get all evaluations with pagination
    @GetMapping
    public ResponseEntity<?> getAllEvaluations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            List<EvaluationCacheDto> evaluations = evaluationService.getAllEvaluations(pageable);
            List<FetchEvaluationDto> evaluationDtos = evaluations.stream()
                    .map(FetchEvaluationDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluations: " + e.getMessage());
        }
    }
    
    // Update evaluation
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvaluation(@PathVariable Long id, @Valid @RequestBody NewEvaluationDto evaluationUpdates) {
        try {
            verifyEvaluationOwnership(id);
            Evaluation updateEvaluation = new Evaluation();
            // Only metadata can be updated for evaluations based on the service
            
            EvaluationCacheDto updatedEvaluation = evaluationService.updateEvaluation(id, updateEvaluation);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error updating evaluation: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating evaluation: " + e.getMessage());
        }
    }
    
    // Delete evaluation
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvaluation(@PathVariable Long id) {
        try {
            verifyEvaluationOwnership(id);
            evaluationService.deleteEvaluation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting evaluation: " + e.getMessage());
        }
    }
    
    // Get evaluation by candidate attempt ID
    @GetMapping("/candidate-attempt/{candidateAttemptId}")
    public ResponseEntity<?> getEvaluationByCandidateAttemptId(@PathVariable Long candidateAttemptId) {
        try {
            Optional<EvaluationCacheDto> evaluation = evaluationService.getEvaluationByCandidateAttemptId(candidateAttemptId);
            if (evaluation.isPresent()) {
                // Verify ownership of the evaluation
                verifyEvaluationOwnership(evaluation.get().getId());
                return ResponseEntity.ok(new FetchEvaluationDto(evaluation.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluation: " + e.getMessage());
        }
    }
    
    // Get evaluations created within date range
    @GetMapping("/created-between")
    public ResponseEntity<?> getEvaluationsCreatedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<EvaluationCacheDto> evaluations = evaluationService.getEvaluationsCreatedBetween(startDate, endDate, pageable);
            List<FetchEvaluationDto> evaluationDtos = evaluations.stream()
                    .map(FetchEvaluationDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluations: " + e.getMessage());
        }
    }
    
    // Get evaluations by candidate ID
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<?> getEvaluationsByCandidateId(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<EvaluationCacheDto> evaluations = evaluationService.getEvaluationsByCandidateId(candidateId, pageable);
            List<FetchEvaluationDto> evaluationDtos = evaluations.stream()
                    .map(FetchEvaluationDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluations: " + e.getMessage());
        }
    }
    
    // Get evaluations by assessment ID
    @GetMapping("/assessment/{assessmentId}")
    public ResponseEntity<?> getEvaluationsByAssessmentId(
            @PathVariable Long assessmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<EvaluationCacheDto> evaluations = evaluationService.getEvaluationsByAssessmentId(assessmentId, pageable);
            List<FetchEvaluationDto> evaluationDtos = evaluations.stream()
                    .map(FetchEvaluationDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluations: " + e.getMessage());
        }
    }
    
    // Get evaluations by user ID
    @GetMapping("/get")
    public ResponseEntity<?> getEvaluationsByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UserCacheDto user = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size);
            List<EvaluationCacheDto> evaluations = evaluationService.getEvaluationsByUserId(user.getId(), pageable);
            List<FetchEvaluationDto> evaluationDtos = evaluations.stream()
                    .map(FetchEvaluationDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluations: " + e.getMessage());
        }
    }
    
    // Get evaluation with full details
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getEvaluationWithDetails(@PathVariable Long id) {
        try {
            verifyEvaluationOwnership(id);
            EvaluationCacheDto evaluation = evaluationService.getEvaluationWithDetails(id);
            return ResponseEntity.ok(new FetchEvaluationDto(evaluation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluation details: " + e.getMessage());
        }
    }
    
    // Count evaluations by assessment
    @GetMapping("/count/assessment/{assessmentId}")
    public ResponseEntity<?> countEvaluationsByAssessment(@PathVariable Long assessmentId) {
        try {
            Long count = evaluationService.countEvaluationsByAssessment(assessmentId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error counting evaluations: " + e.getMessage());
        }
    }
    
    // Get recent evaluations for a user
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentEvaluationsByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UserCacheDto user = getCurrentUser();
            Pageable pageable = PageRequest.of(page, size);
            List<EvaluationCacheDto> evaluations = evaluationService.getRecentEvaluationsByUserId(user.getId(), pageable);
            List<FetchEvaluationDto> evaluationDtos = evaluations.stream()
                    .map(FetchEvaluationDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving recent evaluations: " + e.getMessage());
        }
    }
    
    // Update evaluation metadata
    @PutMapping("/{id}/metadata")
    public ResponseEntity<?> updateEvaluationMetadata(
            @PathVariable Long id,
            @RequestBody Map<String, String> metadata) {
        try {
            verifyEvaluationOwnership(id);
            EvaluationCacheDto updatedEvaluation = evaluationService.updateEvaluationMetadata(id, metadata);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error updating metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating metadata: " + e.getMessage());
        }
    }
    
    // Add metadata entry
    @PostMapping("/{id}/metadata")
    public ResponseEntity<?> addMetadata(
            @PathVariable Long id,
            @RequestParam String key,
            @RequestParam String value) {
        try {
            verifyEvaluationOwnership(id);
            EvaluationCacheDto updatedEvaluation = evaluationService.addMetadata(id, key, value);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error adding metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding metadata: " + e.getMessage());
        }
    }
    
    // Remove metadata entry
    @DeleteMapping("/{id}/metadata/{key}")
    public ResponseEntity<?> removeMetadata(
            @PathVariable Long id,
            @PathVariable String key) {
        try {
            verifyEvaluationOwnership(id);
            EvaluationCacheDto updatedEvaluation = evaluationService.removeMetadata(id, key);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error removing metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error removing metadata: " + e.getMessage());
        }
    }
} 