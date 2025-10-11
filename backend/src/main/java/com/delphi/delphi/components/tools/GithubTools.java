package com.delphi.delphi.components.tools;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.delphi.delphi.utils.git.GithubBranchDetails;
import com.delphi.delphi.utils.git.GithubFile;
import com.delphi.delphi.utils.git.GithubReference;
import com.delphi.delphi.utils.git.GithubRepoBranch;
import com.delphi.delphi.utils.git.GithubRepoContents;

@Component
/*
 * Contains placeholder method tool definitions for the LLM to make GitHub API calls
 */
public class GithubTools implements Tools {
    
    private final Logger log = LoggerFactory.getLogger(GithubTools.class);  

    public GithubTools() {
    }

    // private String getCurrentRepoName(Map<String, Object> context) {
    //     try {
    //         String githubRepoName = (String) context.get("githubRepoName");
    //         return githubRepoName;
    //     } catch (NullPointerException e) {
    //         throw new IllegalStateException("GitHub repo name not set in context", e);
    //     }
    //     catch (ClassCastException e) {
    //         throw new IllegalStateException("GitHub repo name must be a String", e);
    //     }
    // }

    // private String getGithubUsername(Map<String, Object> context) {
    //     try {
    //         String githubUsername = (String) context.get("githubUsername");
    //         if (githubUsername == null) throw new IllegalStateException("GitHub username not set in context");
    //         return githubUsername;
    //     } catch (ClassCastException e) {
    //         throw new IllegalStateException("GitHub username must be a String", e);
    //     } catch (NullPointerException e) {
    //         throw new IllegalStateException("GitHub username not set in context", e);
    //     }
    // }

    // private Long getCurrentAssessmentId(Map<String, Object> context) {
    //     try {
    //         Long assessmentId = (Long) context.get("assessmentId");
    //         return assessmentId;
    //     } catch (NullPointerException e) {
    //         throw new IllegalStateException("Assessment ID not set in context", e);
    //     }
    //     catch (ClassCastException e) {
    //         throw new IllegalStateException("Assessment ID must be a Long", e);
    //     }
    // }

    /* Tool definitions */

    // @Tool(description = "Creates a Git repository in the user's GitHub account using the GitHub API.")
    // public ResponseEntity<String> createRepo(@ToolParam(required = true, description = "The name of the repository to create") String repoName) {
    //     String pat = getPAT();
    //     return githubService.createRepo(pat, repoName);
    // }

    @Tool(name = "addFile", description = "Adds a file to a repository using the GitHub API.")
    public GithubFile addFileToRepo(
        @ToolParam(required = true, description = "The path of the file to add") String filePath, 
        @ToolParam(required = true, description = "The content of the file to add") String fileContent,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = false, description = "The branch to add the file to. If not provided, the default branch will be used.") String branch) {
        throw new RuntimeException("Placeholder method being called for addFile");
    }

    @Tool(name = "getRepositoryContents", description = "Gets the file and directory contents of the default branch of the Git repository using the GitHub API.")
    public GithubRepoContents getRepoContents(
        @ToolParam(required = true, description = "The path of the file to get the contents of") String filePath,
        @ToolParam(required = false, description = "The branch to get the contents of. If not provided, the default branch will be used.") String branch) {
        throw new RuntimeException("Placeholder method being called for getRepoContents");
    }

    @Tool(name = "getRepositoryBranches", description = "Gets the branches of the repository using the GitHub API.")
    public List<GithubRepoBranch> getRepoBranches() {
        throw new RuntimeException("Placeholder method being called for getRepoBranches");
    }

    @Tool(name = "addBranch", description = "Adds a branch to the repository using the GitHub API.")
    public GithubReference addBranch(
        @ToolParam(required = true, description = "The name of the branch to add") String branchName,
        @ToolParam(required = true, description = "The base branch to add the new branch from") String baseBranch
        ) {
        throw new RuntimeException("Placeholder method being called for addBranch");
    }

    @Tool(name = "editFile", description = "Replaces the contents of an existing file in the repository with new content using the GitHub API.")
    public GithubFile editFile(
        @ToolParam(required = true, description = "The path of the file to edit") String filePath,
        @ToolParam(required = true, description = "The content of the file to edit") String fileContent,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = true, description = "The SHA of the file to edit") String sha) {
        throw new RuntimeException("Placeholder method being called for editFile");
    }

    @Tool(name = "deleteFile", description = "Deletes a file in the Git repository using the GitHub API.")
    public String deleteFile(
        @ToolParam(required = true, description = "The path of the file to delete") String filePath,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = true, description = "The SHA of the file to delete") String sha) {
        throw new RuntimeException("Placeholder method being called for deleteFile");
    }

    @Tool(name = "getBranchDetails", description = "Gets the details of a branch in the Git repository using the GitHub API.")
    public GithubBranchDetails getBranchDetails(
        @ToolParam(required = true, description = "The name of the branch to get the details of") String branchName) {
        throw new RuntimeException("Placeholder method being called for getBranchDetails");
    }

    @Tool(name = "sendMessageToUser", description = "Sends a message to the user after applying changes to the repository.", returnDirect=true)
    public String sendMessageToUser(
        @ToolParam(required = true, description = "The message to send to the user") String message) {
            log.info("--------------------------------");
            log.info("SENDING MESSAGE TO USER - GITHUB TOOL:");
            log.info("Message: {}", message.substring(0, Math.min(message.length(), 100)) + "...");
            log.info("STOPPING CONVERSATION - WAITING FOR USER RESPONSE");
            log.info("--------------------------------");
        
            return "STOP: " + message + "\n\n[Conversation paused - waiting for user response]";
            // return githubService.sendMessageToUser(
        //     message, 
        //     getCurrentAssessmentId(toolContext.getContext()), 
        //     (String) toolContext.getContext().get("model")
        // );
    }

    // TODO: add todo list tool for agent to use
    // TODO: allow agent to spawn subagents to handle different parts of the task
    // e.g. copying the code to a separate branch for different language options
    // TODO: allow agent to use the todo list tool to add tasks to the todo list
    // TODO: allow agent to use the todo list tool to remove tasks from the todo list
    // TODO: allow agent to use the todo list tool to mark tasks as complete
    // TODO: allow agent to use the todo list tool to mark tasks as not complete
    // TODO: allow agent to use the todo list tool to get the status of a task
    // TODO: allow agent to use the todo list tool to get the list of tasks

}