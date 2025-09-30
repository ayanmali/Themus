package com.delphi.delphi.dtos.messaging.candidates;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.delphi.delphi.dtos.cache.CandidateCacheDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidateInvitationMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long assessmentId;
    private String assessmentName;
    private String assessmentDescription;
    private LocalDateTime assessmentStartDate;
    private LocalDateTime assessmentEndDate;
    private Integer assessmentDuration;
    private CandidateCacheDto candidate;
    private Long userId;
    private String userEmail;
    private LocalDateTime invitationDate;
    private String invitationId;

    public CandidateInvitationMessage() {}

    public CandidateInvitationMessage(Long assessmentId, String assessmentName, String assessmentDescription,
                                    LocalDateTime assessmentStartDate,
                                    LocalDateTime assessmentEndDate, Integer assessmentDuration,
                                    CandidateCacheDto candidate, Long userId, String userEmail,
                                    LocalDateTime invitationDate, String invitationId) {
        this.assessmentId = assessmentId;
        this.assessmentName = assessmentName;
        this.assessmentDescription = assessmentDescription;
        this.assessmentStartDate = assessmentStartDate;
        this.assessmentEndDate = assessmentEndDate;
        this.assessmentDuration = assessmentDuration;
        this.candidate = candidate;
        this.userId = userId;
        this.userEmail = userEmail;
        this.invitationDate = invitationDate;
        this.invitationId = invitationId;
    }

    // Getters and Setters
    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getAssessmentName() {
        return assessmentName;
    }

    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    public String getAssessmentDescription() {
        return assessmentDescription;
    }

    public void setAssessmentDescription(String assessmentDescription) {
        this.assessmentDescription = assessmentDescription;
    }


    public LocalDateTime getAssessmentStartDate() {
        return assessmentStartDate;
    }

    public void setAssessmentStartDate(LocalDateTime assessmentStartDate) {
        this.assessmentStartDate = assessmentStartDate;
    }

    public LocalDateTime getAssessmentEndDate() {
        return assessmentEndDate;
    }

    public void setAssessmentEndDate(LocalDateTime assessmentEndDate) {
        this.assessmentEndDate = assessmentEndDate;
    }

    public Integer getAssessmentDuration() {
        return assessmentDuration;
    }

    public void setAssessmentDuration(Integer assessmentDuration) {
        this.assessmentDuration = assessmentDuration;
    }

    public CandidateCacheDto getCandidate() {
        return candidate;
    }

    public void setCandidate(CandidateCacheDto candidate) {
        this.candidate = candidate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getInvitationDate() {
        return invitationDate;
    }

    public void setInvitationDate(LocalDateTime invitationDate) {
        this.invitationDate = invitationDate;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }
} 