// package com.delphi.delphi.components.tools;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;
// import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
// import org.springframework.stereotype.Component;

// import com.delphi.delphi.services.GithubService;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;

// @Component
// /**
//  * Handles logic for executing tool calls when the LLM requests to call a tool.
//  */
// public class RepoAnalyzerToolCallHandler {

//     private final GithubService githubService;

//     private final ObjectMapper objectMapper;
//     private final Logger log = LoggerFactory.getLogger(RepoAnalyzerToolCallHandler.class);

//     public RepoAnalyzerToolCallHandler(GithubService githubService) {
//         this.objectMapper = new ObjectMapper();
//         this.githubService = githubService;
//     }

//     /**
//      * Custom tool call execution with filtering logic
//      * @param toolCall The tool call to execute
//      * @param encryptedGithubToken The encrypted GitHub token
//      * @param githubUsername The GitHub username
//      * @param githubRepoName The GitHub repository name
//      * @return ToolResponse if executed, null if skipped
//      */
//     public ToolResponse executeToolCall(ToolCall toolCall, String encryptedGithubToken, String githubUsername, String githubRepoName) {
//         // Check if this tool call should be skipped
//         if (shouldSkipToolCall(toolCall)) {
//             return null; // Skip this tool call
//         }

//         try {
//             // Parse the JSON arguments
//             Map<String, Object> args = objectMapper.readValue(toolCall.arguments(), new TypeReference<Map<String, Object>>() {});

//             log.info("Encrypted GitHub Token: {}", encryptedGithubToken);
//             log.info("GitHub Username: {}", githubUsername);
//             log.info("GitHub Repo Name: {}", githubRepoName);
//             log.info("Tool Call Arguments: {}", args.toString());

//             // Execute the tool call based on its name
//             switch (toolCall.name()) {
//                 case "addNote" -> {
//                     String note = (String) args.get("note");
//                     return new ToolResponse(toolCall.id(), toolCall.name(), note);
//                 }
//                 case "getNotes" -> {
//                     return new ToolResponse(toolCall.id(), toolCall.name(), "TODO: Notes retrieved");
//                 }
//                 case "getRepositoryContents" -> {
//                     String filePath = (String) args.get("filePath");
//                     return new ToolResponse(toolCall.id(), toolCall.name(), 
//                         githubService.getRepoContents(encryptedGithubToken, githubUsername, githubRepoName, filePath, null).toString());
//                 }
//                 case "returnRepositoryAnalysis" -> {
//                     // This is a special case - don't execute, just return a placeholder
//                     return new ToolResponse(toolCall.id(), toolCall.name(), "Message sent to user");
//                 }
//                 default -> throw new IllegalArgumentException("Unknown tool call: " + toolCall.name());
//             }
//         } catch (JsonProcessingException | IllegalArgumentException e) {
//             throw new RuntimeException("Error executing tool call: " + toolCall.name(), e);
//         }
//     }

//     /**
//      * Execute multiple tool calls with custom filtering
//      * @param toolCalls List of tool calls to execute
//      * @param encryptedGithubToken The encrypted GitHub token
//      * @param githubUsername The GitHub username
//      * @param githubRepoName The GitHub repository name
//      * @return List of tool responses (excluding skipped ones)
//      */
//     public List<ToolResponse> executeToolCalls(List<ToolCall> toolCalls, String encryptedGithubToken, String githubUsername, String githubRepoName) {
//         List<ToolResponse> responses = new ArrayList<>();
        
//         for (ToolCall toolCall : toolCalls) {
//             ToolResponse response = executeToolCall(toolCall, encryptedGithubToken, githubUsername, githubRepoName);
//             if (response != null) {
//                 responses.add(response);
//             }
//         }
        
//         return responses;
//     }

//     /**
//      * Determine if a tool call should be skipped
//      * @param toolCall The tool call to check
//      * @return true if the tool call should be skipped
//      */
//     private boolean shouldSkipToolCall(ToolCall toolCall) {
//         // Skip sendMessageToUser tool calls - they should not generate responses
//         // Add other skip conditions here as needed
//         // For example, you might want to skip certain tool calls based on context
        
//         return "sendMessageToUser".equals(toolCall.name());
//     }
// }
