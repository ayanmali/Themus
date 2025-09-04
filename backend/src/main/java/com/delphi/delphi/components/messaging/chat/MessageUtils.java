package com.delphi.delphi.components.messaging.chat;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.delphi.delphi.entities.Job;
import com.delphi.delphi.repositories.JobRepository;

import com.delphi.delphi.services.ChatService;
import com.delphi.delphi.utils.enums.JobStatus;

public class MessageUtils {
    private static Logger log = LoggerFactory.getLogger(MessageUtils.class);

    public static void handleJobFailure(ChatService chatService, JobRepository jobRepository, Job job, UUID jobId, Exception e, String errorMessage) {
        try {
            // Update job status in database
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setResult(errorMessage);
                jobRepository.save(job);
            }
            
            // Send error event via SSE
            chatService.sendSseEvent(jobId, "error", 
                Map.of("error", errorMessage,
                       "jobId", jobId.toString(),
                       "status", "failed"));
                       
        } catch (Exception ex) {
            log.error("Additional error while handling job failure for {}: {}", jobId, ex.getMessage());
        }
    }
}
