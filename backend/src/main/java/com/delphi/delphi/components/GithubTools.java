package com.delphi.delphi.components;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.delphi.delphi.services.EncryptionService;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.utils.git.GithubBranchDetails;
import com.delphi.delphi.utils.git.GithubFile;
import com.delphi.delphi.utils.git.GithubReference;
import com.delphi.delphi.utils.git.GithubRepoBranch;
import com.delphi.delphi.utils.git.GithubRepoContents;

@Component
/*
 * Contains tool definitions for the LLM to make GitHub API calls
 * Uses the methods defined in the GithubService class
 */
public class GithubTools {

    private final EncryptionService encryptionService;

    private final GithubService githubService; // for making GitHub API calls
    private final Base64.Encoder base64Encoder; // for encoding content to base64 for GitHub API
    private final Base64.Decoder base64Decoder; // for decoding content from base64 for GitHub API
    private final Logger log = LoggerFactory.getLogger(GithubTools.class);  

    public GithubTools(GithubService githubService, EncryptionService encryptionService) {
        this.githubService = githubService;
        this.base64Encoder = Base64.getEncoder();
        this.base64Decoder = Base64.getDecoder();
        this.encryptionService = encryptionService;
    }

    /* Helper methods */

    // get the decrypted PAT from the user service
    private String getPAT(String encryptedGithubToken) {
        try {
            if (encryptedGithubToken == null) throw new IllegalStateException("Encrypted GitHub token not set in context");
            return encryptionService.decrypt(encryptedGithubToken);
        } catch (Exception e) {
            throw new IllegalStateException("Error decrypting GitHub token", e);
        } 
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

    // GitHub API requires base64-encoded content for file contents
    private String encodeToBase64(String content) {
        return base64Encoder.encodeToString(content.getBytes(StandardCharsets.UTF_8));
    } 

    private String decodeFromBase64(String content) {
        return new String(base64Decoder.decode(content), StandardCharsets.UTF_8);
    }

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
        @ToolParam(required = false, description = "The branch to add the file to. If not provided, the default branch will be used.") String branch,
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        GithubFile fileResponse = githubService.addFileToRepo(pat, githubUsername, githubRepoName, filePath, branch, encodeToBase64(fileContent),
                commitMessage).block();
        if (fileResponse != null) {
            GithubFile file = fileResponse;
            file.setContent(fileContent);
            return file;
        } else {
            throw new RuntimeException("Error adding file to repo");
        }
    }

    @Tool(name = "getRepositoryContents", description = "Gets the file and directory contents of the default branch of the Git repository using the GitHub API.")
    public GithubRepoContents getRepoContents(
        @ToolParam(required = true, description = "The path of the file to get the contents of") String filePath,
        @ToolParam(required = false, description = "The branch to get the contents of. If not provided, the default branch will be used.") String branch,
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        GithubRepoContents repoContentsResponse = githubService.getRepoContents(pat, githubUsername, githubRepoName, filePath, branch).block();
        if (repoContentsResponse != null) {
            GithubRepoContents repoContents = repoContentsResponse;
            if (repoContents.getType().equals("file")) {
                repoContents.setContent(decodeFromBase64(repoContents.getContent()));
            }
            return repoContents;
        } else {
            throw new RuntimeException("Error getting repo contents");
        }
    }

    @Tool(name = "getRepositoryBranches", description = "Gets the branches of the repository using the GitHub API.")
    public List<GithubRepoBranch> getRepoBranches(
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        List<GithubRepoBranch> repoBranchesResponse = githubService.getRepoBranches(pat, githubUsername, githubRepoName).block();
        if (repoBranchesResponse != null) {
            return repoBranchesResponse;
        } else {
            throw new RuntimeException("Error getting repo branches");
        }
    }

    @Tool(name = "addBranch", description = "Adds a branch to the repository using the GitHub API.")
    public GithubReference addBranch(
        @ToolParam(required = true, description = "The name of the branch to add") String branchName,
        @ToolParam(required = true, description = "The base branch to add the new branch from") String baseBranch,
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        GithubReference addBranchResponse = githubService.addBranch(pat, githubUsername, githubRepoName, branchName, baseBranch).block();
        if (addBranchResponse != null) {
            return addBranchResponse;
        } else {
            throw new RuntimeException("Error adding branch");
        }
    }

    @Tool(name = "editFile", description = "Replaces the contents of an existing file in the repository with new content using the GitHub API.")
    public GithubFile editFile(
        @ToolParam(required = true, description = "The path of the file to edit") String filePath,
        @ToolParam(required = true, description = "The content of the file to edit") String fileContent,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = true, description = "The SHA of the file to edit") String sha,
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        GithubFile editFileResponse = githubService.editFile(pat, githubUsername, githubRepoName, filePath, encodeToBase64(fileContent), commitMessage,
                sha).block();
        if (editFileResponse != null) {
            GithubFile file = editFileResponse;
            file.setContent(fileContent);
            return file;
        } else {
            throw new RuntimeException("Error editing file");
        }
    }

    @Tool(name = "deleteFile", description = "Deletes a file in the Git repository using the GitHub API.")
    public String deleteFile(
        @ToolParam(required = true, description = "The path of the file to delete") String filePath,
        @ToolParam(required = true, description = "The commit message for the file") String commitMessage,
        @ToolParam(required = true, description = "The SHA of the file to delete") String sha,
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        String deleteFileResponse = githubService.deleteFile(pat, githubUsername, githubRepoName, filePath, commitMessage, sha).block();
        if (deleteFileResponse != null) {
            return "File: " + filePath + " deleted successfully";
        } else {
            throw new RuntimeException("Error deleting file");
        }
    }

    @Tool(name = "getBranchDetails", description = "Gets the details of a branch in the Git repository using the GitHub API.")
    public GithubBranchDetails getBranchDetails(
        @ToolParam(required = true, description = "The name of the branch to get the details of") String branchName,
        String encryptedGithubToken,
        String githubUsername,
        String githubRepoName) {
        String pat = getPAT(encryptedGithubToken);
        GithubBranchDetails branchDetailsResponse = githubService.getBranchDetails(pat, githubUsername, githubRepoName, branchName).block();
        if (branchDetailsResponse != null) {
            return branchDetailsResponse;
        } else {
            throw new RuntimeException("Error getting branch details");
        }
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

}