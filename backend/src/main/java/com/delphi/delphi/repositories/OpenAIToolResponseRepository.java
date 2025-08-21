package com.delphi.delphi.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.OpenAiToolResponse;

@Repository
public interface OpenAIToolResponseRepository extends JpaRepository<OpenAiToolResponse, String> {
    // Find tool responses by tool call ID
    Page<OpenAiToolResponse> findByToolCallId(String toolCallId, Pageable pageable);
    List<OpenAiToolResponse> findByToolCallId(String toolCallId);
}
