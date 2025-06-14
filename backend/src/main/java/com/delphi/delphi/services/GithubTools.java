package com.delphi.delphi.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
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
        return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getGithubAccessToken();
    }

    private String getGithubUsername() {
        Long userId = userStateService.getCurrentUserId();
        return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getGithubUsername();
    }

    // GitHub API requires base64-encoded content for file contents
    private String encodeToBase64(String content) {
        return base64Encoder.encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    /* Tool definitions */

    @Tool(description = "Creates a Git repository in the user's GitHub account using the GitHub API.")
    public ResponseEntity<String> createRepo(String repoName) {
        String pat = getPAT();
        return githubClient.createRepo(pat, repoName);
    }

    @Tool(description = "Adds a file to a repository using the GitHub API.")
    public ResponseEntity<String> addFileToRepo(String repoName, String filePath, String fileContent,
            String commitMessage) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.addFileToRepo(pat, username, repoName, filePath, encodeToBase64(fileContent),
                commitMessage);
    }

    @Tool(description = "Gets the file and directory contents of the default branch of the Git repository using the GitHub API.")
    public ResponseEntity<Map> getRepoContents(String repoName, String filePath) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.getRepoContents(pat, username, repoName, filePath);
        // return "Contents of file " + filePath + " in repository " + repoName + ":\n"
        // + contents.toString();
    }

    @Tool(description = "Gets the branches of the repository using the GitHub API.")
    public ResponseEntity<List> getRepoBranches(String repoName) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.getRepoBranches(pat, username, repoName);
    }

    @Tool(description = "Adds a branch to the repository using the GitHub API.")
    public ResponseEntity<String> addBranch(String repoName, String branchName, String baseBranch) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.addBranch(pat, username, repoName, branchName, baseBranch);
    }

    @Tool(description = "Replaces the contents of an existing file in the repository with new content using the GitHub API.")
    public ResponseEntity<String> editFile(String repoName, String filePath, String fileContent, String commitMessage,
            String sha) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.editFile(pat, username, repoName, filePath, encodeToBase64(fileContent), commitMessage,
                sha);
    }

    @Tool(description = "Deletes a file in the Git repository using the GitHub API.")
    public ResponseEntity<String> deleteFile(String repoName, String filePath, String commitMessage, String sha) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.deleteFile(pat, username, repoName, filePath, commitMessage, sha);
    }

    @Tool(description = "Gets the SHA of a branch in the Git repository using the GitHub API.")
    public ResponseEntity<Map> getBranchDetails(String repoName, String branchName) {
        String pat = getPAT();
        String username = getGithubUsername();
        return githubClient.getBranchDetails(pat, username, repoName, branchName);
    }

}