package com.delphi.delphi.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.OpenAiToolResponse;

@Repository
public interface OpenAIToolResponseRepository extends JpaRepository<OpenAiToolResponse, String> {
    // Find tool responses by tool call ID
    Page<OpenAiToolResponse> findById(String id, Pageable pageable);
    Page<OpenAiToolResponse> findByChatMessageId(Long chatMessageId, Pageable pageable);
    // List<OpenAiToolResponse> findByChatMessageId(Long chatMessageId);
}
