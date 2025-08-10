package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.dtos.cache.EvaluationCacheDto;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.entities.Evaluation;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.repositories.EvaluationRepository;

@Service
@Transactional
// TODO: add cache annotations for other entity caches
public class EvaluationService {

    private final CandidateAttemptRepository candidateAttemptRepository;
    
    private final EvaluationRepository evaluationRepository;
    
    public EvaluationService(EvaluationRepository evaluationRepository, CandidateAttemptRepository candidateAttemptRepository) {
        this.evaluationRepository = evaluationRepository;
        this.candidateAttemptRepository = candidateAttemptRepository;
    }
    
    // Create a new evaluation
    @CachePut(value = "evaluations", key = "#result.id")
    public EvaluationCacheDto createEvaluation(Evaluation evaluation) {
        // Validate that the associated candidate attempt exists
        if (evaluation.getCandidateAttempt() != null && evaluation.getCandidateAttempt().getId() != null) {
            CandidateAttempt candidateAttempt = candidateAttemptRepository.findById(evaluation.getCandidateAttempt().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Candidate attempt not found with id: " + evaluation.getCandidateAttempt().getId()));
            evaluation.setCandidateAttempt(candidateAttempt);
            
            // Check if evaluation already exists for this candidate attempt
            Optional<Evaluation> existingEvaluation = evaluationRepository.findByCandidateAttemptId(candidateAttempt.getId());
            if (existingEvaluation.isPresent()) {
                throw new IllegalArgumentException("Evaluation already exists for candidate attempt id: " + candidateAttempt.getId());
            }
        }
        
        return new EvaluationCacheDto(evaluationRepository.save(evaluation));
    }
    
    // Get evaluation by ID
    @Cacheable(value = "evaluations", key = "#id")
    @Transactional(readOnly = true)
    public EvaluationCacheDto getEvaluationById(Long id) {
        return new EvaluationCacheDto(evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id)));
    }
    
    // Get evaluation by ID or throw exception
    @Cacheable(value = "evaluations", key = "#id")
    @Transactional(readOnly = true)
    public EvaluationCacheDto getEvaluationByIdOrThrow(Long id) {
        return new EvaluationCacheDto(evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id)));
    }
    
    // Get all evaluations with pagination
    @Cacheable(value = "evaluations", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<EvaluationCacheDto> getAllEvaluations(Pageable pageable) {
        return evaluationRepository.findAll(pageable).getContent().stream().map(EvaluationCacheDto::new).collect(Collectors.toList());
    }
    
    // Update evaluation
    @CachePut(value = "evaluations", key = "#result.id")
    public EvaluationCacheDto updateEvaluation(Long id, Evaluation evaluationUpdates) {
        Evaluation existingEvaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
        
        // Update fields if provided
        if (evaluationUpdates.getMetadata() != null) {
            existingEvaluation.setMetadata(evaluationUpdates.getMetadata());
        }
        
        return new EvaluationCacheDto(evaluationRepository.save(existingEvaluation));
    }
    
    // Delete evaluation
    @CacheEvict(value = "evaluations", key = "#id")
    public void deleteEvaluation(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new IllegalArgumentException("Evaluation not found with id: " + id);
        }
        evaluationRepository.deleteById(id);
    }
    
    // Get evaluation by candidate attempt ID
    @Cacheable(value = "evaluations", key = "#candidateAttemptId")
    @Transactional(readOnly = true)
    public Optional<EvaluationCacheDto> getEvaluationByCandidateAttemptId(Long candidateAttemptId) {
        return evaluationRepository.findByCandidateAttemptId(candidateAttemptId)
                .map(EvaluationCacheDto::new);
    }
    
    // Get evaluations created within date range
    @Cacheable(value = "evaluations", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<EvaluationCacheDto> getEvaluationsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return evaluationRepository.findByCreatedDateBetween(startDate, endDate, pageable).getContent().stream().map(EvaluationCacheDto::new).collect(Collectors.toList());
    }
    
    // Get evaluations by candidate ID
    @Cacheable(value = "evaluations", key = "#candidateId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<EvaluationCacheDto> getEvaluationsByCandidateId(Long candidateId, Pageable pageable) {
        return evaluationRepository.findByCandidateId(candidateId, pageable).getContent().stream().map(EvaluationCacheDto::new).collect(Collectors.toList());
    }
    
    // Get evaluations by assessment ID
    @Cacheable(value = "evaluations", key = "#assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
        @Transactional(readOnly = true)
    public List<EvaluationCacheDto> getEvaluationsByAssessmentId(Long assessmentId, Pageable pageable) {
        return evaluationRepository.findByAssessmentId(assessmentId, pageable).getContent().stream().map(EvaluationCacheDto::new).collect(Collectors.toList());
    }
    
    // Get evaluations by user ID
    @Cacheable(value = "evaluations", key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<EvaluationCacheDto> getEvaluationsByUserId(Long userId, Pageable pageable) {
        return evaluationRepository.findByUserId(userId, pageable).getContent().stream().map(EvaluationCacheDto::new).collect(Collectors.toList());
    }
    
    // Get evaluation with full details
    @Cacheable(value = "evaluations", key = "withDetails + ':' + #evaluationId")
    @Transactional(readOnly = true)
    public EvaluationCacheDto getEvaluationWithDetails(Long evaluationId) {
        return new EvaluationCacheDto(evaluationRepository.findByIdWithDetails(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + evaluationId)));
    }
    
    // Count evaluations by assessment
    @Cacheable(value = "evaluations", key = "count + ':' + #assessmentId")
    @Transactional(readOnly = true)
    public Long countEvaluationsByAssessment(Long assessmentId) {
        return evaluationRepository.countByAssessmentId(assessmentId);
    }
    
    // Get recent evaluations for a user
    @Cacheable(value = "evaluations", key = "recent + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<EvaluationCacheDto> getRecentEvaluationsByUserId(Long userId, Pageable pageable) {
        return evaluationRepository.findRecentEvaluationsByUserId(userId, pageable).getContent().stream().map(EvaluationCacheDto::new).collect(Collectors.toList());
    }
    
    // Update evaluation metadata
    @CachePut(value = "evaluations", key = "#result.id")
    public EvaluationCacheDto updateEvaluationMetadata(Long id, Map<String, String> metadata) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
        evaluation.setMetadata(metadata);
        return new EvaluationCacheDto(evaluationRepository.save(evaluation));
    }
    
    // Add metadata entry
    @CachePut(value = "evaluations", key = "#result.id")
    public EvaluationCacheDto addMetadata(Long id, String key, String value) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
        if (evaluation.getMetadata() == null) {
            evaluation.setMetadata(Map.of(key, value));
        } else {
            evaluation.getMetadata().put(key, value);
        }
        return new EvaluationCacheDto(evaluationRepository.save(evaluation));
    }
    
    // Remove metadata entry
    @CachePut(value = "evaluations", key = "#result.id")
    public EvaluationCacheDto removeMetadata(Long id, String key) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
        if (evaluation.getMetadata() != null) {
            evaluation.getMetadata().remove(key);
        }
        return new EvaluationCacheDto(evaluationRepository.save(evaluation));
    }
}
