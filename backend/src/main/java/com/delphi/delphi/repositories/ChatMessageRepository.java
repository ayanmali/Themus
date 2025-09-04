package com.delphi.delphi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Find chat messages by assessment ID, sorted by creation date
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.assessmentId = :assessmentId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByAssessmentIdOrderByCreatedAtAsc(@Param("assessmentId") Long assessmentId);
    
    // Legacy method for backward compatibility
    List<ChatMessage> findByAssessmentId(Long assessmentId);
}
