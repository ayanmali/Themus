package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.CandidateAttempt;
import com.delphi.delphi.repositories.CandidateAttemptRepository;
import com.delphi.delphi.utils.AttemptStatus;

@Service
@Transactional
public class CandidateAttemptService {

    @Autowired
    private CandidateAttemptRepository candidateAttemptRepository;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private AssessmentService assessmentService;

    // Create a new candidate attempt
    public CandidateAttempt startAttempt(Long candidateId, Long assessmentId, Optional<String> languageChoice, AttemptStatus status, LocalDateTime startedDate) {
        // Check if candidate already has an attempt for this assessment
        Optional<CandidateAttempt> existingAttempt = candidateAttemptRepository.findByCandidateIdAndAssessmentId(
                candidateId,
                assessmentId);

        if (existingAttempt.isPresent()) {
            throw new IllegalArgumentException("Candidate already has an attempt for this assessment");
        }

        CandidateAttempt candidateAttempt = new CandidateAttempt();
        candidateAttempt.setCandidate(candidateService.getCandidateByIdOrThrow(candidateId));
        candidateAttempt.setAssessment(assessmentService.getAssessmentByIdOrThrow(assessmentId));
        // Set language choice if provided and valid
        if (languageChoice.isPresent() && !assessmentService.getAssessmentByIdOrThrow(assessmentId).getLanguageOptions()
                .contains(languageChoice.get())) {
            candidateAttempt.setLanguageChoice(languageChoice.get());
        }
        candidateAttempt.setStatus(status);
        candidateAttempt.setStartedDate(startedDate);

        return candidateAttemptRepository.save(candidateAttempt);
    }

    // Create a new candidate attempt
    public CandidateAttempt startAttempt(CandidateAttempt candidateAttempt) {
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

        return candidateAttemptRepository.save(candidateAttempt);
    }

    // Get candidate attempt by ID
    @Transactional(readOnly = true)
    public Optional<CandidateAttempt> getCandidateAttemptById(Long id) {
        return candidateAttemptRepository.findById(id);
    }

    // Get candidate attempt by ID or throw exception
    @Transactional(readOnly = true)
    public CandidateAttempt getCandidateAttemptByIdOrThrow(Long id) {
        return candidateAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CandidateAttempt not found with id: " + id));
    }

    // Get all candidate attempts with pagination
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAllCandidateAttempts(Pageable pageable) {
        return candidateAttemptRepository.findAll(pageable);
    }

    // Update candidate attempt
    public CandidateAttempt updateCandidateAttempt(Long id, CandidateAttempt attemptUpdates) {
        CandidateAttempt existingAttempt = getCandidateAttemptByIdOrThrow(id);

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

        return candidateAttemptRepository.save(existingAttempt);
    }

    // Delete candidate attempt
    public void deleteCandidateAttempt(Long id) {
        if (!candidateAttemptRepository.existsById(id)) {
            throw new IllegalArgumentException("CandidateAttempt not found with id: " + id);
        }
        candidateAttemptRepository.deleteById(id);
    }

    // Get attempts by candidate ID
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByCandidateId(Long candidateId, Pageable pageable) {
        return candidateAttemptRepository.findByCandidateId(candidateId, pageable);
    }

    // Get attempts by assessment ID
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByAssessmentId(Long assessmentId, Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentId(assessmentId, pageable);
    }

    // Get attempts by status
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByStatus(AttemptStatus status, Pageable pageable) {
        return candidateAttemptRepository.findByStatus(status, pageable);
    }

    // Get attempt by candidate and assessment
    @Transactional(readOnly = true)
    public Optional<CandidateAttempt> getAttemptByCandidateAndAssessment(Long candidateId, Long assessmentId) {
        return candidateAttemptRepository.findByCandidateIdAndAssessmentId(candidateId, assessmentId);
    }

    // Get attempts by candidate and status
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByCandidateAndStatus(Long candidateId, AttemptStatus status,
            Pageable pageable) {
        return candidateAttemptRepository.findByCandidateIdAndStatus(candidateId, status, pageable);
    }

    // Get attempts by assessment and status
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status,
            Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentIdAndStatus(assessmentId, status, pageable);
    }

    // Get attempts by language choice
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByLanguageChoice(String languageChoice, Pageable pageable) {
        return candidateAttemptRepository.findByLanguageChoiceIgnoreCase(languageChoice, pageable);
    }

    // Get attempts created within date range
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByCreatedDateBetween(startDate, endDate, pageable);
    }

    // Get attempts started within date range
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsStartedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByStartedDateBetween(startDate, endDate, pageable);
    }

    // Get attempts submitted within date range
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsSubmittedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return candidateAttemptRepository.findByCompletedDateBetween(startDate, endDate, pageable);
    }

    // Get overdue attempts
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getOverdueAttempts(Pageable pageable) {
        return candidateAttemptRepository.findOverdueAttempts(LocalDateTime.now(), pageable);
    }

    // Get attempts by user
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findByUserId(userId, pageable);
    }

    // Get attempts with evaluation
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsWithEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findAttemptsWithEvaluation(pageable);
    }

    // Get submitted attempts without evaluation
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getSubmittedAttemptsWithoutEvaluation(Pageable pageable) {
        return candidateAttemptRepository.findSubmittedAttemptsWithoutEvaluation(pageable);
    }

    // Count attempts by assessment and status
    @Transactional(readOnly = true)
    public Long countAttemptsByAssessmentAndStatus(Long assessmentId, AttemptStatus status) {
        return candidateAttemptRepository.countByAssessmentIdAndStatus(assessmentId, status);
    }

    // Count attempts by candidate
    @Transactional(readOnly = true)
    public Long countAttemptsByCandidate(Long candidateId) {
        return candidateAttemptRepository.countByCandidateId(candidateId);
    }

    // Get attempt with details
    @Transactional(readOnly = true)
    public Optional<CandidateAttempt> getAttemptWithDetails(Long attemptId) {
        return candidateAttemptRepository.findByIdWithDetails(attemptId);
    }

    // Get recent attempts by user
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getRecentAttemptsByUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findRecentAttemptsByUserId(userId, pageable);
    }

    // Get attempts by assessment user
    @Transactional(readOnly = true)
    public Page<CandidateAttempt> getAttemptsByAssessmentUserId(Long userId, Pageable pageable) {
        return candidateAttemptRepository.findByAssessmentUserId(userId, pageable);
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
    public CandidateAttempt submitAttempt(Long id, String githubRepositoryLink) {
        CandidateAttempt attempt = getCandidateAttemptByIdOrThrow(id);

        if (attempt.getStatus() != AttemptStatus.STARTED) {
            throw new IllegalStateException("Only started attempts can be submitted");
        }

        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setCompletedDate(LocalDateTime.now());
        if (githubRepositoryLink != null) {
            attempt.setGithubRepositoryLink(githubRepositoryLink);
        }

        return candidateAttemptRepository.save(attempt);
    }

    // Mark as evaluated
    public CandidateAttempt markAsEvaluated(Long id) {
        CandidateAttempt attempt = getCandidateAttemptByIdOrThrow(id);

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new IllegalStateException("Only completed attempts can be evaluated");
        }

        attempt.setStatus(AttemptStatus.EVALUATED);
        attempt.setEvaluatedDate(LocalDateTime.now());

        return candidateAttemptRepository.save(attempt);
    }

    // Check if attempt is overdue
    @Transactional(readOnly = true)
    public boolean isAttemptOverdue(Long id) {
        CandidateAttempt attempt = getCandidateAttemptByIdOrThrow(id);

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
}
