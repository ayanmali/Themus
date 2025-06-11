package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.Evaluation;
import com.delphi.delphi.repositories.EvaluationRepository;

@Service
@Transactional
public class EvaluationService {
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private CandidateAttemptService candidateAttemptService;
    
    // Create a new evaluation
    public Evaluation createEvaluation(Evaluation evaluation) {
        // Validate that the associated candidate attempt exists
        if (evaluation.getCandidateAttempt() != null && evaluation.getCandidateAttempt().getId() != null) {
            CandidateAttempt candidateAttempt = candidateAttemptService.getCandidateAttemptByIdOrThrow(evaluation.getCandidateAttempt().getId());
            evaluation.setCandidateAttempt(candidateAttempt);
            
            // Check if evaluation already exists for this candidate attempt
            Optional<Evaluation> existingEvaluation = evaluationRepository.findByCandidateAttemptId(candidateAttempt.getId());
            if (existingEvaluation.isPresent()) {
                throw new IllegalArgumentException("Evaluation already exists for candidate attempt id: " + candidateAttempt.getId());
            }
        }
        
        return evaluationRepository.save(evaluation);
    }
    
    // Get evaluation by ID
    @Transactional(readOnly = true)
    public Optional<Evaluation> getEvaluationById(Long id) {
        return evaluationRepository.findById(id);
    }
    
    // Get evaluation by ID or throw exception
    @Transactional(readOnly = true)
    public Evaluation getEvaluationByIdOrThrow(Long id) {
        return evaluationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
    }
    
    // Get all evaluations with pagination
    @Transactional(readOnly = true)
    public Page<Evaluation> getAllEvaluations(Pageable pageable) {
        return evaluationRepository.findAll(pageable);
    }
    
    // Update evaluation
    public Evaluation updateEvaluation(Long id, Evaluation evaluationUpdates) {
        Evaluation existingEvaluation = getEvaluationByIdOrThrow(id);
        
        // Update fields if provided
        if (evaluationUpdates.getMetadata() != null) {
            existingEvaluation.setMetadata(evaluationUpdates.getMetadata());
        }
        
        return evaluationRepository.save(existingEvaluation);
    }
    
    // Delete evaluation
    public void deleteEvaluation(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new IllegalArgumentException("Evaluation not found with id: " + id);
        }
        evaluationRepository.deleteById(id);
    }
    
    // Get evaluation by candidate attempt ID
    @Transactional(readOnly = true)
    public Optional<Evaluation> getEvaluationByCandidateAttemptId(Long candidateAttemptId) {
        return evaluationRepository.findByCandidateAttemptId(candidateAttemptId);
    }
    
    // Get evaluations created within date range
    @Transactional(readOnly = true)
    public Page<Evaluation> getEvaluationsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return evaluationRepository.findByCreatedDateBetween(startDate, endDate, pageable);
    }
    
    // Get evaluations by candidate ID
    @Transactional(readOnly = true)
    public Page<Evaluation> getEvaluationsByCandidateId(Long candidateId, Pageable pageable) {
        return evaluationRepository.findByCandidateId(candidateId, pageable);
    }
    
    // Get evaluations by assessment ID
    @Transactional(readOnly = true)
    public Page<Evaluation> getEvaluationsByAssessmentId(Long assessmentId, Pageable pageable) {
        return evaluationRepository.findByAssessmentId(assessmentId, pageable);
    }
    
    // Get evaluations by user ID
    @Transactional(readOnly = true)
    public Page<Evaluation> getEvaluationsByUserId(Long userId, Pageable pageable) {
        return evaluationRepository.findByUserId(userId, pageable);
    }
    
    // Get evaluation with full details
    @Transactional(readOnly = true)
    public Optional<Evaluation> getEvaluationWithDetails(Long evaluationId) {
        return evaluationRepository.findByIdWithDetails(evaluationId);
    }
    
    // Count evaluations by assessment
    @Transactional(readOnly = true)
    public Long countEvaluationsByAssessment(Long assessmentId) {
        return evaluationRepository.countByAssessmentId(assessmentId);
    }
    
    // Get recent evaluations for a user
    @Transactional(readOnly = true)
    public Page<Evaluation> getRecentEvaluationsByUserId(Long userId, Pageable pageable) {
        return evaluationRepository.findRecentEvaluationsByUserId(userId, pageable);
    }
    
    // Update evaluation metadata
    public Evaluation updateEvaluationMetadata(Long id, Map<String, String> metadata) {
        Evaluation evaluation = getEvaluationByIdOrThrow(id);
        evaluation.setMetadata(metadata);
        return evaluationRepository.save(evaluation);
    }
    
    // Add metadata entry
    public Evaluation addMetadata(Long id, String key, String value) {
        Evaluation evaluation = getEvaluationByIdOrThrow(id);
        if (evaluation.getMetadata() == null) {
            evaluation.setMetadata(Map.of(key, value));
        } else {
            evaluation.getMetadata().put(key, value);
        }
        return evaluationRepository.save(evaluation);
    }
    
    // Remove metadata entry
    public Evaluation removeMetadata(Long id, String key) {
        Evaluation evaluation = getEvaluationByIdOrThrow(id);
        if (evaluation.getMetadata() != null) {
            evaluation.getMetadata().remove(key);
        }
        return evaluationRepository.save(evaluation);
    }
}
