package com.delphi.delphi.components;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.delphi.delphi.services.UserService;
import com.delphi.delphi.services.agent.UserStateService;

@Component
/*
 * Contains tool definitions for the LLM to make GitHub API calls
 * Uses the methods defined in the GithubClient class
 */
public class GithubTools {
    private final UserStateService userStateService; // for managing user state
    private final UserService userService; // for getting user info (PAT, username)
    private final GithubClient githubClient; // for making GitHub API calls
    private final Base64.Encoder base64Encoder; // for encoding content to base64 for GitHub API

    public GithubTools(UserStateService userStateService, UserService userService, GithubClient githubClient) {
        this.userStateService = userStateService;
        this.userService = userService;
        this.githubClient = githubClient;
        this.base64Encoder = Base64.getEncoder();
    }

    /* Helper methods */
    private String getPAT() {
        Long userId = userStateService.getCurrentUserId();
        if (userId == null) throw new IllegalStateException("User not set in context");
        return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getGithubAccessToken();
    }

    private String getCurrentRepoName() {
        return userStateService.getCurrentAssessmentRepoName();
    }

    private String getGithubUsername() {
        Long userId = userStateService.getCurrentUserId();
        return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getGithubUsername();
    }

    private Long getCurrentChatHistoryId() {
        return userStateService.getCurrentChatHistoryId();
    }

    // GitHub API requires base64-encoded content for file contents
    private String encodeToBase64(String content) {
        return base64Encoder.encodeToString(content.getBytes(StandardCharsets.UTF_8));
    } 

    /* Tool definitions */

    // @Tool(description = "Creates a Git repository in the user's GitHub account using the GitHub API.")
    // public ResponseEntity<String> createRepo(@ToolParam(required = true, description = "The name of the repository to create") String repoName) {
    //     String pat = getPAT();
    //     return githubClient.createRepo(pat, repoName);
    // }

    @Tool(description = "Adds a file to a repository using the GitHub API.")
    public ResponseEntity<String> addFileToRepo(
        @ToolParam(required = true, description = "The path of the file to add") String filePath, 
        @ToolParam(required = true, description = "The content of the file to add") String fileContent,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = false, description = "The branch to add the file to. If not provided, the default branch will be used.") String branch) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.addFileToRepo(pat, username, getCurrentRepoName(), filePath, branch=null, encodeToBase64(fileContent),
                commitMessage);
    }

    @Tool(description = "Gets the file and directory contents of the default branch of the Git repository using the GitHub API.")
    public ResponseEntity<Map> getRepoContents(
        @ToolParam(required = true, description = "The path of the file to get the contents of") String filePath) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.getRepoContents(pat, username, getCurrentRepoName(), filePath);
        // return "Contents of file " + filePath + " in repository " + repoName + ":\n"
        // + contents.toString();
    }

    @Tool(description = "Gets the branches of the repository using the GitHub API.")
    public ResponseEntity<List> getRepoBranches() {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.getRepoBranches(pat, username, getCurrentRepoName());
    }

    @Tool(description = "Adds a branch to the repository using the GitHub API.")
    public ResponseEntity<String> addBranch(
        @ToolParam(required = true, description = "The name of the branch to add") String branchName,
        @ToolParam(required = true, description = "The base branch to add the new branch from") String baseBranch) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.addBranch(pat, username, getCurrentRepoName(), branchName, baseBranch);
    }

    @Tool(description = "Replaces the contents of an existing file in the repository with new content using the GitHub API.")
    public ResponseEntity<String> editFile(
        @ToolParam(required = true, description = "The path of the file to edit") String filePath,
        @ToolParam(required = true, description = "The content of the file to edit") String fileContent,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = true, description = "The SHA of the file to edit") String sha) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.editFile(pat, username, getCurrentRepoName(), filePath, encodeToBase64(fileContent), commitMessage,
                sha);
    }

    @Tool(description = "Deletes a file in the Git repository using the GitHub API.")
    public ResponseEntity<String> deleteFile(
        @ToolParam(required = true, description = "The path of the file to delete") String filePath,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = true, description = "The SHA of the file to delete") String sha) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.deleteFile(pat, username, getCurrentRepoName(), filePath, commitMessage, sha);
    }

    @Tool(description = "Gets the details of a branch in the Git repository using the GitHub API.")
    public ResponseEntity<Map> getBranchDetails(
        @ToolParam(required = true, description = "The name of the branch to get the details of") String branchName) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.getBranchDetails(pat, username, getCurrentRepoName(), branchName);
    }

    @Tool(description = "Sends a message to the user after applying changes to the repository.")
    public String sendMessageToUser(
        @ToolParam(required = true, description = "The message to send to the user") String message) {
        return githubClient.sendMessageToUser(message, getCurrentChatHistoryId());
    }

}