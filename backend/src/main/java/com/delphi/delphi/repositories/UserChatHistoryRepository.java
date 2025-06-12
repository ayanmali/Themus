package com.delphi.delphi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.UserChatHistory;

@Repository
public interface UserChatHistoryRepository extends JpaRepository<UserChatHistory, Long> {

    // Find chat history by assessment ID
    UserChatHistory findByAssessmentId(Long assessmentId);
}
