// package com.delphi.delphi.services.agent;

// import org.springframework.context.annotation.Scope;
// import org.springframework.context.annotation.ScopedProxyMode;
// import org.springframework.stereotype.Service;

// import com.delphi.delphi.entities.Assessment;
// import com.delphi.delphi.repositories.AssessmentRepository;

// import jakarta.annotation.PreDestroy;

// @Service
// @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
// /*
//  * Maintains the application state (assessment ID) between LLM tool calls
//  * Uses AssessmentRepository to get related user and chat history information
//  */
// public class UserStateService {

//     private ThreadLocal<Long> currentAssessmentId = new ThreadLocal<>();
    
//     private final AssessmentRepository assessmentRepository;

//     public UserStateService(AssessmentRepository assessmentRepository) {
//         this.assessmentRepository = assessmentRepository;
//     }

//     public Long getCurrentAssessmentId() {
//         return currentAssessmentId.get();
//     }

//     public void setCurrentAssessmentId(Long assessmentId) {
//         this.currentAssessmentId.set(assessmentId);
//     }

//     public Long getCurrentUserId() {
//         Long assessmentId = getCurrentAssessmentId();
//         if (assessmentId == null) {
//             throw new RuntimeException("No current assessment set");
//         }
        
//         Assessment assessment = assessmentRepository.findById(assessmentId)
//                 .orElseThrow(() -> new RuntimeException("Assessment not found with ID: " + assessmentId));
        
//         return assessment.getUser().getId();
//     }

//     public String getCurrentAssessmentRepoName() {
//         Long assessmentId = getCurrentAssessmentId();
        
//         if (assessmentId == null) {
//             throw new RuntimeException("No current assessment set");
//         }
        
//         Assessment assessment = assessmentRepository.findById(assessmentId)
//                 .orElseThrow(() -> new RuntimeException("Assessment not found with ID: " + assessmentId));
        
//         return assessment.getGithubRepoName(); // Assuming Assessment entity has a repoName field
//     }

//     public Long getCurrentChatHistoryId() {
//         Long assessmentId = getCurrentAssessmentId();
//         if (assessmentId == null) {
//             throw new RuntimeException("No current assessment set");
//         }
        
//         Assessment assessment = assessmentRepository.findById(assessmentId)
//                 .orElseThrow(() -> new RuntimeException("Assessment not found with ID: " + assessmentId));
        
//         return assessment.getChatHistory().getId(); // Assuming Assessment entity has a chatHistoryId field
//     }

//     @PreDestroy
//     public void cleanup() {
//         currentAssessmentId.remove();
//     }
// }
