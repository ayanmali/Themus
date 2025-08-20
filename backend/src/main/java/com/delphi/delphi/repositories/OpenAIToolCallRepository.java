package com.delphi.delphi.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.OpenAiToolCall;

@Repository
public interface OpenAIToolCallRepository extends JpaRepository<OpenAiToolCall, String> {
    Page<OpenAiToolCall> findByChatMessageId(Long chatMessageId, Pageable pageable);
    List<OpenAiToolCall> findByChatMessageId(Long chatMessageId);
}
