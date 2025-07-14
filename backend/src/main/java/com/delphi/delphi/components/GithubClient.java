package com.delphi.delphi.components;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.MessageType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import com.delphi.delphi.entities.ChatHistory;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.repositories.ChatHistoryRepository;
import com.delphi.delphi.services.GithubAppService;
import com.delphi.delphi.utils.git.GithubBranchDetails;
import com.delphi.delphi.utils.git.GithubFile;
import com.delphi.delphi.utils.git.GithubReference;
import com.delphi.delphi.utils.git.GithubRepoContents;
import com.delphi.delphi.utils.git.GithubRepoBranch;

@Component
/*
 * This class contains methods to interact with the GitHub API.
 * It is used to create repositories, add files to repositories, and get
 * repository contents.
 * It is used to get repository branches, add branches, edit files, and delete
 * files.
 * It is used to get branch SHA, and add branch.
 * It is used to get repository contents, and add files to a repository.
 * It is used to get repository branches, and add branch.
 * It is used to get branch SHA, and add branch.
 */

public class GithubClient {

    private final ChatHistoryRepository chatHistoryRepository;

    // private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final Map<String, String> committer;
    private final GithubAppService githubAppService;

    public GithubClient(RestTemplate restTemplate, HttpHeaders githubClientheaders, Map<String, String> committer,
            // ChatMessageRepository chatMessageRepository,
            ChatHistoryRepository chatHistoryRepository,
            GithubAppService githubAppService) {
        this.restTemplate = restTemplate;
        this.headers = githubClientheaders;
        this.committer = committer;
        // this.chatMessageRepository = chatMessageRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.githubAppService = githubAppService;
    }

    public ResponseEntity<GithubRepoContents> createRepo(String accessToken, String repoName) {
        try {
            String url = "https://api.github.com/user/repos";

            headers.setBearerAuth(accessToken);
            // headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "name", repoName,
                    "private", true);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(url, entity, GithubRepoContents.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error creating repo: " + e.getMessage());
        }
    }

    public ResponseEntity<?> addContributor(String accessToken, String owner, String repo, String username) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/collaborators/%s", owner, repo, username);

            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error adding contributor: " + e.getMessage());
        }
    }

    public ResponseEntity<GithubFile> addFileToRepo(String accessToken, String owner, String repo, String path,
            String branch, String content, String commitMessage) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            headers.setBearerAuth(accessToken);
            // headers.setContentType(MediaType.APPLICATION_JSON);

            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "branch", branch,
                    "committer", committer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, GithubFile.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error adding file to repo: " + e.getMessage());
        }
    }

    public ResponseEntity<GithubRepoContents> getRepoContents(String accessToken, String owner, String repo,
            String path, String branch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
            if (branch != null) {
                url += "?ref=" + branch;
            }

            headers.setBearerAuth(accessToken);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, GithubRepoContents.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting repo contents: " + e.getMessage());
        }
    }

    public ResponseEntity<List<GithubRepoBranch>> getRepoBranches(String accessToken, String owner, String repo) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);

            headers.setBearerAuth(accessToken);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<List<GithubRepoBranch>>() {}
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting repo branches: " + e.getMessage());
        }
    }

    public ResponseEntity<GithubReference> addBranch(String accessToken, String owner, String repo, String branchName,
            String baseBranch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);

            headers.setBearerAuth(accessToken);
            // headers.setContentType(MediaType.APPLICATION_JSON);

            // Get the SHA of the base branch first
            ResponseEntity<GithubBranchDetails> branchResponse = getBranchDetails(accessToken, owner, repo, baseBranch);
            if (!branchResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get base branch SHA: " + branchResponse.getStatusCode());
            }

            GithubBranchDetails branchBody = branchResponse.getBody();
            if (branchBody == null) {
                throw new RuntimeException("Failed to get base branch details");
            }

            String sha = branchBody.getCommit().getSha();

            Map<String, Object> body = Map.of(
                    "ref", "refs/heads/" + branchName,
                    "sha", sha);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(url, entity, GithubReference.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error adding branch: " + e.getMessage());
        }
    }

    public ResponseEntity<GithubFile> editFile(String accessToken, String owner, String repo, String path, String content,
            String commitMessage, String sha) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            headers.setBearerAuth(accessToken);
            // headers.setContentType(MediaType.APPLICATION_JSON);

            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "sha", sha,
                    "committer", committer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, GithubFile.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error editing file: " + e.getMessage());
        }
    }

    public ResponseEntity<String> deleteFile(String accessToken, String owner, String repo, String path,
            String commitMessage, String sha) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            headers.setBearerAuth(accessToken);
            // headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "sha", sha,
                    "committer", committer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }

    public ResponseEntity<GithubBranchDetails> getBranchDetails(String accessToken, String owner, String repo, String branch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/branches/%s", owner, repo, branch);

            headers.setBearerAuth(accessToken);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, GithubBranchDetails.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting branch details: " + e.getMessage());
        }
    }

    public ResponseEntity<Map> getCommitDetails(String accessToken, String owner, String repo, String commit) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, commit);

            headers.setBearerAuth(accessToken);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting commit details: " + e.getMessage());
        }
    }

    public ChatMessage sendMessageToUser(String text, Long chatHistoryId, String model) {
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setText(text);
            chatMessage.setChatHistory(chatHistoryRepository.findById(chatHistoryId)
                    .orElseThrow(() -> new Exception("Chat history not found with id: " + chatHistoryId)));

            chatMessage.setMessageType(MessageType.ASSISTANT);
            chatMessage.setModel(model);
            // TODO: add this back?
            // chatMessageRepository.save(chatMessage);

            ChatHistory existingChatHistory = chatHistoryRepository.findById(chatHistoryId)
                    .orElseThrow(() -> new Exception("Chat history not found with id: " + chatHistoryId));

            // TODO: add this back?
            // existingChatHistory.getMessages().add(chatMessage);
            existingChatHistory.addMessage(chatMessage);
            chatHistoryRepository.save(existingChatHistory);
            return chatMessage;
        } catch (Exception e) {
            throw new RuntimeException("Error sending message: " + e.getMessage());
        }
    }

    // Deprecated: Use getBranchDetails() instead for full response information
    // @Deprecated
    // public String getBranchSha(String accessToken, String owner, String repo,
    // String branch) {
    // ResponseEntity<Map> response = getBranchDetails(accessToken, owner, repo,
    // branch);
    // Map<String, Object> body = response.getBody();
    // if (body != null) {
    // Map<String, Object> commit = (Map<String, Object>) body.get("commit");
    // return (String) commit.get("sha");
    // }
    // throw new RuntimeException("Failed to get branch SHA");
    // }

    // ============= GITHUB APP METHODS =============

    /**
     * Create repository using GitHub App installation token
     */
    public ResponseEntity<GithubRepoContents> createRepoWithInstallation(Long installationId, String repoName) {
        try {
            String url = "https://api.github.com/user/repos";
            String installationToken = githubAppService.getInstallationAccessToken(installationId);

            headers.setBearerAuth(installationToken);
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            Map<String, Object> body = Map.of(
                    "name", repoName,
                    "private", true);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(url, entity, GithubRepoContents.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error creating repo with GitHub App: " + e.getMessage());
        }
    }

    /**
     * Add file to repository using GitHub App installation token
     */
    public ResponseEntity<GithubFile> addFileToRepoWithInstallation(Long installationId, String owner, String repo, 
            String path, String branch, String content, String commitMessage) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
            String installationToken = githubAppService.getInstallationAccessToken(installationId);

            headers.setBearerAuth(installationToken);
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "branch", branch,
                    "committer", committer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, GithubFile.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error adding file to repo with GitHub App: " + e.getMessage());
        }
    }

    /**
     * Get repository contents using GitHub App installation token
     */
    public ResponseEntity<GithubRepoContents> getRepoContentsWithInstallation(Long installationId, String owner, 
            String repo, String path, String branch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
            if (branch != null) {
                url += "?ref=" + branch;
            }

            String installationToken = githubAppService.getInstallationAccessToken(installationId);
            headers.setBearerAuth(installationToken);
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, GithubRepoContents.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting repo contents with GitHub App: " + e.getMessage());
        }
    }

    /**
     * Get repository branches using GitHub App installation token
     */
    public ResponseEntity<List<GithubRepoBranch>> getRepoBranchesWithInstallation(Long installationId, String owner, String repo) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);
            String installationToken = githubAppService.getInstallationAccessToken(installationId);

            headers.setBearerAuth(installationToken);
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<List<GithubRepoBranch>>() {}
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting repo branches with GitHub App: " + e.getMessage());
        }
    }

    /**
     * Edit file using GitHub App installation token
     */
    public ResponseEntity<GithubFile> editFileWithInstallation(Long installationId, String owner, String repo, 
            String path, String content, String commitMessage, String sha) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
            String installationToken = githubAppService.getInstallationAccessToken(installationId);

            headers.setBearerAuth(installationToken);
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "sha", sha,
                    "committer", committer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, GithubFile.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error editing file with GitHub App: " + e.getMessage());
        }
    }
}
