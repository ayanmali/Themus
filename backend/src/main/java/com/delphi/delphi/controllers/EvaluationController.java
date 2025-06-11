package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.Map;
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

import com.delphi.delphi.dtos.FetchEvaluationDto;
import com.delphi.delphi.dtos.NewEvaluationDto;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.Evaluation;
import com.delphi.delphi.services.EvaluationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    
    @Autowired
    private EvaluationService evaluationService;
    
    // Create a new evaluation
    @PostMapping
    public ResponseEntity<?> createEvaluation(@Valid @RequestBody NewEvaluationDto newEvaluationDto) {
        try {
            Evaluation evaluation = new Evaluation();
            
            // Set candidate attempt relationship
            CandidateAttempt candidateAttempt = new CandidateAttempt();
            candidateAttempt.setId(newEvaluationDto.getCandidateAttemptId());
            evaluation.setCandidateAttempt(candidateAttempt);
            
            Evaluation createdEvaluation = evaluationService.createEvaluation(evaluation);
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
            Optional<Evaluation> evaluation = evaluationService.getEvaluationById(id);
            if (evaluation.isPresent()) {
                return ResponseEntity.ok(new FetchEvaluationDto(evaluation.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
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
            Page<Evaluation> evaluations = evaluationService.getAllEvaluations(pageable);
            Page<FetchEvaluationDto> evaluationDtos = evaluations.map(FetchEvaluationDto::new);
            
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
            Evaluation updateEvaluation = new Evaluation();
            // Only metadata can be updated for evaluations based on the service
            
            Evaluation updatedEvaluation = evaluationService.updateEvaluation(id, updateEvaluation);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
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
            evaluationService.deleteEvaluation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
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
            Optional<Evaluation> evaluation = evaluationService.getEvaluationByCandidateAttemptId(candidateAttemptId);
            if (evaluation.isPresent()) {
                return ResponseEntity.ok(new FetchEvaluationDto(evaluation.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
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
            Page<Evaluation> evaluations = evaluationService.getEvaluationsCreatedBetween(startDate, endDate, pageable);
            Page<FetchEvaluationDto> evaluationDtos = evaluations.map(FetchEvaluationDto::new);
            
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
            Page<Evaluation> evaluations = evaluationService.getEvaluationsByCandidateId(candidateId, pageable);
            Page<FetchEvaluationDto> evaluationDtos = evaluations.map(FetchEvaluationDto::new);
            
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
            Page<Evaluation> evaluations = evaluationService.getEvaluationsByAssessmentId(assessmentId, pageable);
            Page<FetchEvaluationDto> evaluationDtos = evaluations.map(FetchEvaluationDto::new);
            
            return ResponseEntity.ok(evaluationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving evaluations: " + e.getMessage());
        }
    }
    
    // Get evaluations by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEvaluationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Evaluation> evaluations = evaluationService.getEvaluationsByUserId(userId, pageable);
            Page<FetchEvaluationDto> evaluationDtos = evaluations.map(FetchEvaluationDto::new);
            
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
            Optional<Evaluation> evaluation = evaluationService.getEvaluationWithDetails(id);
            if (evaluation.isPresent()) {
                return ResponseEntity.ok(new FetchEvaluationDto(evaluation.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
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
    @GetMapping("/recent/user/{userId}")
    public ResponseEntity<?> getRecentEvaluationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Evaluation> evaluations = evaluationService.getRecentEvaluationsByUserId(userId, pageable);
            Page<FetchEvaluationDto> evaluationDtos = evaluations.map(FetchEvaluationDto::new);
            
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
            Evaluation updatedEvaluation = evaluationService.updateEvaluationMetadata(id, metadata);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
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
            Evaluation updatedEvaluation = evaluationService.addMetadata(id, key, value);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
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
            Evaluation updatedEvaluation = evaluationService.removeMetadata(id, key);
            return ResponseEntity.ok(new FetchEvaluationDto(updatedEvaluation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error removing metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error removing metadata: " + e.getMessage());
        }
    }
} 