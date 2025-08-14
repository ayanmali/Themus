package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.specifications.CandidateAttemptSpecifications;
import com.delphi.delphi.utils.AttemptStatus;

@Service
@Transactional
// TODO: add cache annotations for other entity caches
/*
 * There are two different caches used here
 * 1. candidate_attempts - data about a given candidate attempt
 * 2. assessment_attempts - the list of candidate attempts for an assessment
 */
public class CandidateAttemptService {

    private final CandidateAttemptRepository candidateAttemptRepository;

    public CandidateAttemptService(CandidateAttemptRepository candidateAttemptRepository) {
        this.candidateAttemptRepository = candidateAttemptRepository;
    }

    public CandidateAttemptCacheDto inviteCandidate(CandidateAttempt candidateAttempt) {
        candidateAttempt.setStatus(AttemptStatus.INVITED);
        candidateAttempt.setStartedDate(null);
        candidateAttempt.setCompletedDate(null);
        candidateAttempt.setEvaluatedDate(null);
        candidateAttempt.setGithubRepositoryLink(null);
        candidateAttempt.setLanguageChoice(null);
        candidateAttempt.setEvaluation(null);
        //candidateAttempt.set
        return new CandidateAttemptCacheDto(candidateAttemptRepository.save(candidateAttempt));
    }

    // Start a new candidate attempt: change status to from INVITED to STARTED
    @CachePut(value = "candidateAttempts", key = "#result.id")
    public CandidateAttemptCacheDto startAttempt(Long candidateId, Long assessmentId, Optional<String> languageChoice, AttemptStatus status, LocalDateTime startedDate) {
        // Check if candidate already has an attempt for this assessment
        Optional<CandidateAttempt> existingAttempt = candidateAttemptRepository.findByCandidateIdAndAssessmentId(
                candidateId,
                assessmentId);

        if (!existingAttempt.isPresent()) {
            throw new IllegalArgumentException("Candidate does not have an attempt for this assessment");
        }

        existingAttempt.get().setStatus(AttemptStatus.STARTED);
        existingAttempt.get().setStartedDate(startedDate);

        return new CandidateAttemptCacheDto(candidateAttemptRepository.save(existingAttempt.get()));
    }

    // Create a new candidate attempt
    @CachePut(value = "candidateAttempts", key = "#result.id")
    public CandidateAttemptCacheDto startAttempt(CandidateAttempt candidateAttempt) {
        // Check if candidate already has an attempt for this assessment
        Optional<CandidateAttempt> existingAttempt = candidateAttemptRepository.findByCandidateIdAndAssessmentId(
                candidateAttempt.getCandidate().getId(),
                candidateAttempt.getAssessment().getId());

        if (existingAttempt.isPresent()) {
            throw new IllegalArgumentException("Candidate already has an attempt for this assessment");
        }

        // Set default status if not provided
        if (candidateAttempt.getStatus() == null) {
            candidateAttempt.setStatus(AttemptStatus.STARTED);
        }

        // Set started date if not provided
        if (candidateAttempt.getStartedDate() == null) {
            candidateAttempt.setStartedDate(LocalDateTime.now());
        }

        return new CandidateAttemptCacheDto(candidateAttemptRepository.save(candidateAttempt));
    }

    // Get candidate attempt by ID
    @Cacheable(value = "candidateAttempts", key = "#id")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getCandidateAttemptById(Long id) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id)));
    }

    // Get candidate attempt by ID or throw exception
    @Cacheable(value = "candidateAttempts", key = "#id")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getCandidateAttemptByIdOrThrow(Long id) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id)));
    }

    // Get all candidate attempts with pagination
    @Cacheable(value = "candidateAttempts", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAllCandidateAttempts(Pageable pageable) {
        return candidateAttemptRepository.findAll(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get candidate attempts with multiple filters
    @Cacheable(value = "candidateAttempts", key = "#candidateId + ':' + #assessmentId + ':' + #status + ':' + #startedAfter + ':' + #startedBefore + ':' + #completedAfter + ':' + #completedBefore + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getCandidateAttemptsWithFilters(Long candidateId, Long assessmentId, 
                                                                 AttemptStatus status, LocalDateTime startedAfter, 
                                                                 LocalDateTime startedBefore, LocalDateTime completedAfter, 
                                                                 LocalDateTime completedBefore, Pageable pageable) {
        Specification<CandidateAttempt> spec = Specification.allOf(
            CandidateAttemptSpecifications.hasCandidateId(candidateId)
            .and(CandidateAttemptSpecifications.hasAssessmentId(assessmentId))
            .and(CandidateAttemptSpecifications.hasStatus(status))
            .and(CandidateAttemptSpecifications.startedAfter(startedAfter))
            .and(CandidateAttemptSpecifications.startedBefore(startedBefore))
            .and(CandidateAttemptSpecifications.completedAfter(completedAfter))
            .and(CandidateAttemptSpecifications.completedBefore(completedBefore))
        );
        return candidateAttemptRepository.findAll(spec, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Update candidate attempt
    @CachePut(value = "candidateAttempts", key = "#id")
    public CandidateAttemptCacheDto updateCandidateAttempt(Long id, CandidateAttempt attemptUpdates) {
        CandidateAttempt existingAttempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        // Update fields if provided
        if (attemptUpdates.getGithubRepositoryLink() != null) {
            existingAttempt.setGithubRepositoryLink(attemptUpdates.getGithubRepositoryLink());
        }
        if (attemptUpdates.getStatus() != null) {
            // Validate status transitions
            validateStatusTransition(existingAttempt.getStatus(), attemptUpdates.getStatus());
            existingAttempt.setStatus(attemptUpdates.getStatus());

            // Set timestamps based on status
            updateTimestampsForStatus(existingAttempt, attemptUpdates.getStatus());
        }
        if (attemptUpdates.getLanguageChoice() != null) {
            existingAttempt.setLanguageChoice(attemptUpdates.getLanguageChoice());
        }

        return new CandidateAttemptCacheDto(candidateAttemptRepository.save(existingAttempt));
    }

    // Delete candidate attempt
    @CacheEvict(value = "candidateAttempts", key = "#id")
    public void deleteCandidateAttempt(Long id) {
        if (!candidateAttemptRepository.existsById(id)) {
            throw new IllegalArgumentException("CandidateAttempt not found with id: " + id);
        }
        candidateAttemptRepository.deleteById(id);
    }

    // Get attempts by candidate ID
    @Cacheable(value = "candidateAttempts", key = "#candidateId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByCandidateId(Long candidateId, Pageable pageable) {
        return candidateAttemptRepository.findByCandidateId(candidateId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment ID
    @Cacheable(value = "candidateAttempts", key = "#assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByAssessmentId(Long assessmentId, Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentId(assessmentId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by status
    @Cacheable(value = "candidateAttempts", key = "#status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByStatus(AttemptStatus status, Pageable pageable) {
        return candidateAttemptRepository.findByStatus(status, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempt by candidate and assessment
    @Cacheable(value = "candidateAttempts", key = "#candidateId + ':' + #assessmentId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getAttemptByCandidateAndAssessment(Long candidateId, Long assessmentId) {
        return candidateAttemptRepository.findByCandidateIdAndAssessmentId(candidateId, assessmentId)
                .map(CandidateAttemptCacheDto::new)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with candidate id: " + candidateId + " and assessment id: " + assessmentId));
    }

    // Get attempts by candidate and status
    @Cacheable(value = "candidateAttempts", key = "#candidateId + ':' + #status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByCandidateAndStatus(Long candidateId, AttemptStatus status,
            Pageable pageable) {
        return candidateAttemptRepository.findByCandidateIdAndStatus(candidateId, status, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment and status
    @Cacheable(value = "candidateAttempts", key = "#assessmentId + ':' + #status + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status,
            Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentIdAndStatus(assessmentId, status, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by language choice
    @Cacheable(value = "candidateAttempts", key = "#languageChoice + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByLanguageChoice(String languageChoice, Pageable pageable) {
        return candidateAttemptRepository.findByLanguageChoiceIgnoreCase(languageChoice, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts created within date range
    @Cacheable(value = "candidateAttempts", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByCreatedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts started within date range
    @Cacheable(value = "candidateAttempts", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsStartedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByStartedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts submitted within date range
    @Cacheable(value = "candidateAttempts", key = "#startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsSubmittedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByCompletedDateBetween(startDate, endDate, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get overdue attempts
    @Cacheable(value = "candidateAttempts", key = "overdue + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getOverdueAttempts(Pageable pageable) {
        return candidateAttemptRepository.findOverdueAttempts(LocalDateTime.now(), pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by user
    @Cacheable(value = "candidateAttempts", key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findByUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts with evaluation
    @Cacheable(value = "candidateAttempts", key = "withEvaluation + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
        public List<CandidateAttemptCacheDto> getAttemptsWithEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findAttemptsWithEvaluation(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get submitted attempts without evaluation
    @Cacheable(value = "candidateAttempts", key = "submittedWithoutEvaluation + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getSubmittedAttemptsWithoutEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findSubmittedAttemptsWithoutEvaluation(pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Count attempts by assessment and status
    @Cacheable(value = "candidateAttempts", key = "count + ':' + #assessmentId + ':' + #status")
    @Transactional(readOnly = true)
    public Long countAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status) {
        return candidateAttemptRepository.countByAssessmentIdAndStatus(assessmentId, status);
    }

    // Count attempts by candidate
    @Cacheable(value = "candidateAttempts", key = "count + ':' + #candidateId")
    @Transactional(readOnly = true)
    public Long countAttemptsByCandidate(Long candidateId) {
        return candidateAttemptRepository.countByCandidateId(candidateId);
    }

    // Get attempt with details
    @Cacheable(value = "candidateAttempts", key = "withDetails + ':' + #attemptId")
    @Transactional(readOnly = true)
    public CandidateAttemptCacheDto getAttemptWithDetails(Long attemptId) {
        return new CandidateAttemptCacheDto(candidateAttemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + attemptId)));
    }

    // Get recent attempts by user
    @Cacheable(value = "candidateAttempts", key = "recent + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getRecentAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findRecentAttemptsByUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Get attempts by assessment user
    @Cacheable(value = "candidateAttempts", key = "byAssessmentUser + ':' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<CandidateAttemptCacheDto> getAttemptsByAssessmentUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentUserId(userId, pageable).getContent().stream().map(CandidateAttemptCacheDto::new).collect(Collectors.toList());
    }

    // Start attempt that is already created (i.e. status is INVITED)
    // public CandidateAttempt startAttempt(Long candidateAttemptId) {
    //     CandidateAttempt attempt = getCandidateAttemptByIdOrThrow(candidateAttemptId);

    //     if (attempt.getStatus() != AttemptStatus.INVITED) {
    //         throw new IllegalStateException("Only invited attempts can be started");
    //     }

    //     attempt.setStatus(AttemptStatus.STARTED);
    //     attempt.setStartedDate(LocalDateTime.now());

    //     return candidateAttemptRepository.save(attempt);
    // }

    // Submit attempt
    @CachePut(value = "candidateAttempts", key = "#result.id")
    public CandidateAttemptCacheDto submitAttempt(Long id, String githubRepositoryLink) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        if (attempt.getStatus() != AttemptStatus.STARTED) {
            throw new IllegalStateException("Only started attempts can be submitted");
        }

        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setCompletedDate(LocalDateTime.now());
        if (githubRepositoryLink != null) {
            attempt.setGithubRepositoryLink(githubRepositoryLink);
        }

        return new CandidateAttemptCacheDto(candidateAttemptRepository.save(attempt));
    }

    // Mark as evaluated
    @CachePut(value = "candidateAttempts", key = "#result.id")
    public CandidateAttemptCacheDto markAsEvaluated(Long id) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new IllegalStateException("Only completed attempts can be evaluated");
        }

        attempt.setStatus(AttemptStatus.EVALUATED);
        attempt.setEvaluatedDate(LocalDateTime.now());

        return new CandidateAttemptCacheDto(candidateAttemptRepository.save(attempt));
    }

    // Check if attempt is overdue
    @Cacheable(value = "candidateAttempts", key = "overdue + ':' + #id")
    @Transactional(readOnly = true)
    public boolean isAttemptOverdue(Long id) {
        CandidateAttempt attempt = candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));

        if (attempt.getStatus() != AttemptStatus.STARTED || attempt.getStartedDate() == null) {
            return false;
        }

        LocalDateTime deadline = attempt.getStartedDate().plusMinutes(attempt.getAssessment().getDuration());
        return LocalDateTime.now().isAfter(deadline);
    }

    // Validate status transition
    private void validateStatusTransition(AttemptStatus currentStatus, AttemptStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No transition
        }

        switch (currentStatus) {
            case INVITED -> {
                if (newStatus != AttemptStatus.STARTED) {
                    throw new IllegalStateException("Can only transition from INVITED to STARTED");
                }
            }
            case STARTED -> {
                if (newStatus != AttemptStatus.COMPLETED) {
                    throw new IllegalStateException("Can only transition from STARTED to COMPLETED");
                }
            }
            case COMPLETED -> {
                if (newStatus != AttemptStatus.EVALUATED) {
                    throw new IllegalStateException("Can only transition from COMPLETED to EVALUATED");
                }
            }
            case EVALUATED -> throw new IllegalStateException("Cannot transition from EVALUATED status");
            default -> throw new IllegalStateException("Unknown status: " + currentStatus);
        }
    }

    // Update timestamps based on status
    private void updateTimestampsForStatus(CandidateAttempt attempt, AttemptStatus status) {
        
        LocalDateTime now = LocalDateTime.now();

        switch (status) {
            case STARTED -> {
                if (attempt.getStartedDate() == null) {
                    attempt.setStartedDate(now);
                }
            }
            case COMPLETED -> {
                if (attempt.getCompletedDate() == null) {
                    attempt.setCompletedDate(now);
                }
            }
            case EVALUATED -> {
                if (attempt.getEvaluatedDate() == null) {
                    attempt.setEvaluatedDate(now);
                }
            }
            default -> {
            }
        }
        // No timestamp update needed
    }

    // public void updateExpiredAttempts() {
    //     LocalDateTime now = LocalDateTime.now();
    //     candidateAttemptRepository.updateExpiredAttempts(now);
    // }

    public void updateAttemptsForInactiveAssessments() {
        candidateAttemptRepository.updateAttemptsForInactiveAssessments();
    }
}
