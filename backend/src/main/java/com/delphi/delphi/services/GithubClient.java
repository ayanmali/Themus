package com.delphi.delphi.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
/*
 * This class contains methods to interact with the GitHub API.
 * It is used to create repositories, add files to repositories, and get repository contents.
 * It is used to get repository branches, add branches, edit files, and delete files.
 * It is used to get branch SHA, and add branch.
 * It is used to get repository contents, and add files to a repository.
 * It is used to get repository branches, and add branch.
 * It is used to get branch SHA, and add branch.
 */
public class GithubClient {

    public void createRepo(String accessToken, String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/user/repos";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        Map<String, Object> body = Map.of(
            "name", repoName,
            "private", true
        );
    
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, String.class);
    }

    public void addFileToRepo(String accessToken, String owner, String repo, String path, String content, String commitMessage) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
    
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    
        Map<String, Object> body = Map.of(
            "message", commitMessage,
            "content", base64Content
        );
    
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.put(url, entity);
    }

    public Map<String, Object> getRepositoryContents(String accessToken, String owner, String repo, String path) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        //HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public List<Map<String, Object>> getRepositoryBranches(String accessToken, String owner, String repo) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        //HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return response.getBody();
    }

    public void addBranch(String accessToken, String owner, String repo, String branchName, String baseBranch) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "ref", "refs/heads/" + branchName,
            "sha", getBranchSha(accessToken, owner, repo, baseBranch)
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, String.class);
    }

    public void editFile(String accessToken, String owner, String repo, String path, String content, String commitMessage, String sha) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
        
        Map<String, Object> body = Map.of(
            "message", commitMessage,
            "content", base64Content,
            "sha", sha
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.put(url, entity);
    }

    public void deleteFile(String accessToken, String owner, String repo, String path, String commitMessage, String sha) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "message", commitMessage,
            "sha", sha
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    public String getBranchSha(String accessToken, String owner, String repo, String branch) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/branches/%s", owner, repo, branch);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        //HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody();
        if (body != null) {
            Map<String, Object> commit = (Map<String, Object>) body.get("commit");
            return (String) commit.get("sha");
        }
        throw new RuntimeException("Failed to get branch SHA");
    }
}
