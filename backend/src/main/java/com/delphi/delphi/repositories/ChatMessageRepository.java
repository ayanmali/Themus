package com.delphi.delphi.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Find chat messages by assessment ID
    Page<ChatMessage> findByAssessmentId(Long assessmentId, Pageable pageable);
}
