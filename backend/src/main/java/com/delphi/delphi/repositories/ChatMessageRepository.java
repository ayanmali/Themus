package com.delphi.delphi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Find chat messages by chat history ID
    List<ChatMessage> findByChatHistoryId(Long chatHistoryId);
}
