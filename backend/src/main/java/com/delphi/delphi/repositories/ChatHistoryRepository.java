package com.delphi.delphi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.ChatHistory;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    // Find chat history by assessment ID
    ChatHistory findByAssessmentId(Long assessmentId);
}
