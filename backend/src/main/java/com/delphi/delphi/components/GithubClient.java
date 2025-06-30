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
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.entities.ChatHistory;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.repositories.ChatHistoryRepository;

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
 * TODO: adjust Github API responses to be more LLM-understandable
 */

public class GithubClient {

    private final ChatHistoryRepository chatHistoryRepository;

    // private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final Map<String, String> committer;

    public GithubClient(RestTemplate restTemplate, HttpHeaders githubClientheaders, Map<String, String> committer,
            // ChatMessageRepository chatMessageRepository,
            ChatHistoryRepository chatHistoryRepository) {
        this.restTemplate = restTemplate;
        this.headers = githubClientheaders;
        this.committer = committer;
        //this.chatMessageRepository = chatMessageRepository;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    public ResponseEntity<String> createRepo(String accessToken, String repoName) {
        String url = "https://api.github.com/user/repos";

        headers.setBearerAuth(accessToken);
        // headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "name", repoName,
                "private", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, entity, String.class);
    }

    public ResponseEntity<?> addContributor(String accessToken, String owner, String repo, String username) {
        String url = String.format("https://api.github.com/repos/%s/%s/collaborators/%s", owner, repo, username);

        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    public ResponseEntity<String> addFileToRepo(String accessToken, String owner, String repo, String path,
            String branch, String content, String commitMessage) {
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
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    public ResponseEntity<Map> getRepoContents(String accessToken, String owner, String repo, String path) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
    }

    public ResponseEntity<List> getRepoBranches(String accessToken, String owner, String repo) {
        String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);

        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
    }

    public ResponseEntity<String> addBranch(String accessToken, String owner, String repo, String branchName,
            String baseBranch) {
        String url = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);

        headers.setBearerAuth(accessToken);
        // headers.setContentType(MediaType.APPLICATION_JSON);

        // Get the SHA of the base branch first
        ResponseEntity<Map> branchResponse = getBranchDetails(accessToken, owner, repo, baseBranch);
        if (!branchResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get base branch SHA: " + branchResponse.getStatusCode());
        }

        Map<String, Object> branchBody = branchResponse.getBody();
        if (branchBody == null) {
            throw new RuntimeException("Failed to get base branch details");
        }

        Map<String, Object> commit = (Map<String, Object>) branchBody.get("commit");
        String sha = (String) commit.get("sha");

        Map<String, Object> body = Map.of(
                "ref", "refs/heads/" + branchName,
                "sha", sha);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, entity, String.class);
    }

    public ResponseEntity<String> editFile(String accessToken, String owner, String repo, String path, String content,
            String commitMessage, String sha) {
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
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    public ResponseEntity<String> deleteFile(String accessToken, String owner, String repo, String path,
            String commitMessage, String sha) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

        headers.setBearerAuth(accessToken);
        // headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "message", commitMessage,
                "sha", sha,
                "committer", committer);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    public ResponseEntity<Map> getBranchDetails(String accessToken, String owner, String repo, String branch) {
        String url = String.format("https://api.github.com/repos/%s/%s/branches/%s", owner, repo, branch);

        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
    }

    public ResponseEntity<Map> getCommitDetails(String accessToken, String owner, String repo, String commit) {
        String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, commit);

        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
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
}
