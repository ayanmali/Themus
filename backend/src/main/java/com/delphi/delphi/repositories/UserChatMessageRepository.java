package com.delphi.delphi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.UserChatMessage;

@Repository
public interface UserChatMessageRepository extends JpaRepository<UserChatMessage, Long> {

    // Find chat messages by chat history ID
    List<UserChatMessage> findByChatHistoryId(Long chatHistoryId);
}
