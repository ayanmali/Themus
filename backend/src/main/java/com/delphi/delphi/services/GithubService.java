package com.delphi.delphi.services;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import com.delphi.delphi.entities.ChatHistory;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.repositories.ChatHistoryRepository;
import com.delphi.delphi.utils.git.GithubBranchDetails;
import com.delphi.delphi.utils.git.GithubFile;
import com.delphi.delphi.utils.git.GithubReference;
import com.delphi.delphi.utils.git.GithubRepoContents;

import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

import com.delphi.delphi.utils.git.GithubRepoBranch;

@Service
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
 * 
 * github.com/apps/delphi-assessments
 */

public class GithubService {

    private final EncryptionService encryptionService;

    private final Logger log = LoggerFactory.getLogger(GithubService.class);
    private final ChatHistoryRepository chatHistoryRepository;

    // private final ChatMessageRepository chatMessageRepository;
    private final String appId;
    private final String clientId;
    private final String clientSecret;
    private final String githubScope;

    private final String candidateAppClientId;
    private final String candidateAppClientSecret;

    private final WebClient webClient;
    private final Map<String, String> author;

    private final String privateKeyRaw;
    private PrivateKey privateKey;
    
    public GithubService(@Value("${github.app.app-id}") String appId,
                        @Value("${github.app.client-id}") String clientId,
                        @Value("${github.app.client-secret}") String clientSecret,
                        @Value("${github.app.private-key}") String privateKeyRaw,
                        @Value("${github.candidate.app.client-id}") String candidateAppClientId,
                        @Value("${github.candidate.app.client-secret}") String candidateAppClientSecret,
                        @Value("${spring.security.oauth2.client.registration.github.scope}") String githubScope,
                        Map<String, String> author,
                        ChatHistoryRepository chatHistoryRepository, EncryptionService encryptionService) {
        this.appId = appId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.privateKeyRaw = privateKeyRaw;
        this.githubScope = githubScope;
        this.candidateAppClientId = candidateAppClientId;
        this.candidateAppClientSecret = candidateAppClientSecret;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Accept", "application/vnd.github.v3+json")
            .build();

        this.author = author;

        loadPrivateKey();

        // this.chatMessageRepository = chatMessageRepository;
        this.chatHistoryRepository = chatHistoryRepository;

        // this.chatMessageRepository = chatMessageRepository;
        this.encryptionService = encryptionService;
    }

    private void loadPrivateKey() {
        try {
            // Clean up the private key string - remove line continuations and normalize whitespace
            String cleanedKey = privateKeyRaw
                .replace("\\", "") // Remove backslash line continuations from properties file
                .replaceAll("\\s+", " ") // Normalize whitespace to single spaces
                .trim();
            
            log.info("Raw private key preview: {}...", cleanedKey.substring(0, Math.min(50, cleanedKey.length())));
            
            // Check if it's RSA or PKCS#8 format
            boolean isRsaFormat = cleanedKey.contains("-----BEGIN RSA PRIVATE KEY-----");
            boolean isPkcs8Format = cleanedKey.contains("-----BEGIN PRIVATE KEY-----");
            
            if (isRsaFormat) {
                log.error("RSA private key format detected. GitHub Apps require PKCS#8 format.");
                log.error("Please convert your private key using: openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in rsa_key.pem -out pkcs8_key.pem");
                log.error("Then update your application.properties to use the PKCS#8 key (should start with -----BEGIN PRIVATE KEY-----)");
                throw new RuntimeException("RSA private key format not supported. Please convert to PKCS#8 format.");
                
            } else if (isPkcs8Format) {
                // Handle PKCS#8 private key format (the correct format for GitHub Apps)
                String keyContent = cleanedKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", ""); // Remove all whitespace
                
                log.info("Processing PKCS#8 private key, content length: {}", keyContent.length());
                
                // Base64 decode the key content
                byte[] keyBytes = Base64.getDecoder().decode(keyContent);
                
                // Create the private key using PKCS#8 format
                KeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.privateKey = keyFactory.generatePrivate(spec);
                
            } else {
                throw new RuntimeException("Unknown private key format. Expected PKCS#8 format starting with -----BEGIN PRIVATE KEY-----");
            }
            
            log.info("Private key loaded successfully");
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Failed to load private key", e);
            throw new RuntimeException("Failed to load private key: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 encoding in private key", e);
            throw new RuntimeException("Invalid Base64 encoding in private key: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error loading private key", e);
            throw new RuntimeException("Failed to load private key: " + e.getMessage(), e);
        }
    }

    // Generate JWT for app authentication
    public String generateAppToken() {
        long now = System.currentTimeMillis() / 1000;
        
        return Jwts.builder()
            .issuer(appId)
            .issuedAt(new Date(now * 1000))
            .expiration(new Date((now + 600) * 1000)) // 10 minutes
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }
    
    // Get installation access token
    public String getInstallationToken(String installationId) {
        // jwt token
        String appToken = generateAppToken();
        
        log.info("Getting installation token for installation ID: {}", installationId);
        
        // generates the installation token
        Map<String, Object> response = webClient
            .post()
            .uri("/app/installations/{installationId}/access_tokens", installationId)
            .header("Authorization", "Bearer " + appToken)
            .header("Accept", "application/vnd.github.v3+json")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();
        
        if (response == null) {
            throw new RuntimeException("Failed to get installation token - null response");
        }
        
        log.info("Installation token response keys: {}", response.keySet());
        
        Object tokenObj = response.get("token");
        if (tokenObj == null) {
            throw new RuntimeException("Failed to get installation token - no token in response: " + response);
        }
        
        String token = tokenObj.toString();
        log.info("Installation token prefix: {}...", token.substring(0, Math.min(10, token.length())));
        
        return token;
    }

    // Get installation information
    public Mono<Map<String, Object>> getInstallationInfo(String installationId) {
        String appToken = generateAppToken();
        
        return webClient
            .get()
            .uri("/app/installations/{installationId}", installationId)
            .header("Authorization", "Bearer " + appToken)
            .header("Accept", "application/vnd.github.v3+json")
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("Installation info error - Status: {}, Body: {}", response.statusCode(), errorBody);
                        return Mono.error(new RuntimeException(
                            String.format("Installation info error %d: %s", response.statusCode().value(), errorBody)
                        ));
                    });
            })
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // OAuth flow - redirect user to GitHub
    public String getAuthorizationUrl(String state) {
        return String.format(
            "https://github.com/login/oauth/authorize?client_id=%s&scope=%s&state=%s",
            clientId, githubScope, state
        );
    }

    // Exchange code for github app user access token
    public Map<String, Object> getAccessToken(String code, boolean isCandidate) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", isCandidate ? candidateAppClientId : clientId);
        params.add("client_secret", isCandidate ? candidateAppClientSecret : clientSecret);
        params.add("code", code);

        // TODO: figure out how to get new access token from refresh token
        //params.add("grant_type", "authorization_code");
        //params.add("refresh_token", refreshToken);
        
        Map<String, Object> response = webClient
            .post()
            .uri("https://github.com/login/oauth/access_token")
            .header("Accept", "application/json")
            .body(BodyInserters.fromFormData(params))
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), resp -> {
                return resp.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("Access token error - Status: {}, Body: {}", resp.statusCode(), errorBody);
                        return Mono.error(new RuntimeException(
                            String.format("Access token error %d: %s", resp.statusCode().value(), errorBody)
                        ));
                    });
            })
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();
        
        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("Failed to get access token: " + response);
        }
        
        return response;
    }

    /*
     * Uses the user's user access token to get their user information from the GitHub API (if the token is valid)
     * MAKE SURE TO USE THE DECRYPTED ACCESS TOKEN, NOT THE ENCRYPTED ONE
     */
    public Map<String, Object> validateGithubCredentials(String githubAccessToken) {
        try {
            // decrypting the access token if it is encrypted
            String token = githubAccessToken;
            if (!githubAccessToken.startsWith("ghu_") && !githubAccessToken.startsWith("gho_")) {
                token = encryptionService.decrypt(githubAccessToken);
            }
            String uri = "/user";
            //String token = encryptionService.decrypt(user.getGithubAccessToken());
            Map<String, Object> result = webClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                    return response.bodyToMono(Map.class)
                        .flatMap(errorBody -> {
                            log.error("Error checking github credentials w/ Github API - Status: {}, Body: {}", response.statusCode(), errorBody);
                            return Mono.error(new RuntimeException(
                                String.format("GitHub API error %d: %s", response.statusCode().value(), errorBody)
                            ));
                        });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (result == null) {
                throw new RuntimeException("Github credentials are null");
            }

            log.info("Github credentials valid, result: {}", result.toString());
            return result;
        } catch (Exception e) {
            log.error("Error checking github credentials, {}", e);
            return null;
        }
    }

    // Validate token and check scopes
    // public Mono<Map<String, Object>> validateToken(String token) {
    //     return webClient
    //         .get()
    //         .uri("https://api.github.com/user")
    //         .header("Authorization", "token " + token)
    //         .header("Accept", "application/vnd.github.v3+json")
    //         .header("User-Agent", "Delphi-App/1.0")
    //         .retrieve()
    //         .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
    //             return response.bodyToMono(String.class)
    //                 .flatMap(errorBody -> {
    //                     log.error("Token validation error - Status: {}, Body: {}", response.statusCode(), errorBody);
    //                     return Mono.error(new RuntimeException(
    //                         String.format("Token validation error %d: %s", response.statusCode().value(), errorBody)
    //                     ));
    //                 });
    //         })
    //         .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    // }

    // Check token scopes
    // public Mono<String> getTokenScopes(String token) {
    //     return webClient
    //         .get()
    //         .uri("https://api.github.com/user")
    //         .header("Authorization", "token " + token)
    //         .header("Accept", "application/vnd.github.v3+json")
    //         .header("User-Agent", "Delphi-App/1.0")
    //         .retrieve()
    //         .toEntity(String.class)
    //         .map(response -> {
    //             String scopes = response.getHeaders().getFirst("X-OAuth-Scopes");
    //             log.info("Token scopes: {}", scopes);
    //             return scopes != null ? scopes : "No scopes found";
    //         })
    //         .onErrorReturn("Error retrieving scopes");
    // }

    public GithubRepoContents createPersonalRepo(String token, String repoName) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            // For GitHub App installation tokens, we need to create repo in the installation's account
            String url = "https://api.github.com/user/repos";

            Map<String, Object> body = Map.of(
                    "name", repoName,
                    "description", "Assessment repository",
                    "private", true,
                    "auto_init", true,
                    "isTemplate", true);

            log.info("Creating repo '{}' with token: {}...", repoName, githubAccessToken.substring(0, Math.min(10, token.length())));
            
            GithubRepoContents repo = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + githubAccessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Delphi-App/1.0")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                    log.error("Error creating personal repo - Status: {}, Body: {}", response.statusCode(), response.bodyToMono(String.class).block());
                    return response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("GitHub API error - Status: {}, Body: {}", response.statusCode(), errorBody);
                            return Mono.error(new RuntimeException(
                                String.format("GitHub API error %d: %s", response.statusCode().value(), errorBody)
                            ));
                        });
                })
                .bodyToMono(GithubRepoContents.class)
                .block();

            if (repo == null) {
                throw new RuntimeException("Failed to create personal repo: repo details from Github API response is null");
            }

            return repo;
        } catch (RestClientException e) {
            log.error("RestClient error creating repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
        catch (Exception e) {
            log.error("Error creating personal repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
    }    

    public GithubRepoContents createOrgRepo(String token, String orgName,String repoName) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            // For GitHub App installation tokens, we need to create repo in the installation's account
            String url = String.format("https://api.github.com/orgs/%s/repos", orgName);

            Map<String, Object> body = Map.of(
                    "name", repoName,
                    "description", "Assessment repository",
                    "private", true,
                    "auto_init", true,
                    "has_downloads", false,
                    "has_issues", true,
                    "has_wiki", true,
                    "isTemplate", true);

            log.info("Creating repo '{}' with token: {}...", repoName, githubAccessToken.substring(0, Math.min(10, token.length())));

            return webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + githubAccessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Delphi-App/1.0")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                    return response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("GitHub API error - Status: {}, Body: {}", response.statusCode(), errorBody);
                            return Mono.error(new RuntimeException(
                                String.format("GitHub API error %d: %s", response.statusCode().value(), errorBody)
                            ));
                        });
                })
                .bodyToMono(GithubRepoContents.class)
                .block();
        } catch (RestClientException e) {
            log.error("RestClient error creating repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
        catch (Exception e) {
            log.error("Error creating org repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
    }

    // Test installation token with existing repository operations
    // public Mono<Map<String, Object>> testInstallationToken(String installationToken, String owner, String repo) {
    //     try {
    //         log.info("Testing installation token with repo '{}/{}'", owner, repo);
            
    //         // Test getting repository contents
    //         return webClient.get()
    //             .uri("https://api.github.com/repos/{owner}/{repo}/contents", owner, repo)
    //             .header("Authorization", "Bearer " + installationToken)
    //             .header("Accept", "application/vnd.github.v3+json")
    //             .header("User-Agent", "Delphi-App/1.0")
    //             .retrieve()
    //             .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
    //                 return response.bodyToMono(String.class)
    //                     .flatMap(errorBody -> {
    //                         log.error("GitHub API error testing installation - Status: {}, Body: {}", response.statusCode(), errorBody);
    //                         return Mono.error(new RuntimeException(
    //                             String.format("GitHub API error %d: %s", response.statusCode().value(), errorBody)
    //                         ));
    //                     });
    //             })
    //             .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    //     } catch (RestClientException e) {
    //         log.error("RestClient error testing installation: {}", e.getMessage(), e);
    //         throw new RuntimeException("Error testing installation: " + e.getMessage(), e);
    //     }
    // }

    // public Mono<String> addContributor(String token, String owner, String repo, String username) {
    //     try {
    //         String url = String.format("https://api.github.com/repos/%s/%s/collaborators/%s", owner, repo, username);

    //         //HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
    //         return webClient.put()
    //             .uri(url)
    //             .header("Authorization", "token " + token)
    //             .retrieve()
    //             .bodyToMono(String.class);
    //     } catch (RestClientException e) {
    //         throw new RuntimeException("Error adding contributor: " + e.getMessage());
    //     }
    // }

    public Mono<GithubFile> addFileToRepo(String token, String owner, String repo, String path,
            String branch, String content, String commitMessage) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "branch", branch,
                    "author", author);

            //HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return webClient.put()
                .uri(url)
                .header("Authorization", "token " + token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GithubFile.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error adding file to repo: " + e.getMessage());
        }
    }

    public Mono<GithubRepoContents> getRepoContents(String token, String owner, String repo,
            String path, String branch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
            if (branch != null) {
                url += "?ref=" + branch;
            }

            return webClient.get()
                .uri(url)
                .header("Authorization", "token " + token)
                .retrieve()
                .bodyToMono(GithubRepoContents.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting repo contents: " + e.getMessage());
        }
    }

    public Mono<List<GithubRepoBranch>> getRepoBranches(String token, String owner, String repo) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);

            return webClient.get()
                .uri(url)
                .header("Authorization", "token " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GithubRepoBranch>>() {});
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting repo branches: " + e.getMessage());
        }
    }

    public Mono<GithubReference> addBranch(String token, String owner, String repo, String branchName,
            String baseBranch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);

            // Get the SHA of the base branch first
            Mono<GithubBranchDetails> branchResponse = getBranchDetails(token, owner, repo, baseBranch);
            GithubBranchDetails branchBody = branchResponse.block();
            if (branchBody == null) {
                throw new RuntimeException("Failed to get base branch details");
            }

            String sha = branchBody.getCommit().getSha();

            Map<String, Object> body = Map.of(
                    "ref", "refs/heads/" + branchName,
                    "sha", sha);

            return webClient.post()
                .uri(url)
                .header("Authorization", "token " + token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GithubReference.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error adding branch: " + e.getMessage());
        }
    }

    public Mono<GithubFile> editFile(String token, String owner, String repo, String path, String content,
            String commitMessage, String sha) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "sha", sha,
                    "author", author);

            return webClient.put()
                .uri(url)
                .header("Authorization", "token " + token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GithubFile.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error editing file: " + e.getMessage());
        }
    }

    public Mono<String> deleteFile(String token, String owner, String repo, String path,
            String commitMessage, String sha) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            // Map<String, Object> body = Map.of(
            //         "message", commitMessage,
            //         "sha", sha,
            //         "author", author);

            return webClient.delete()
                .uri(url)
                .header("Authorization", "token " + token)
                //.bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }

    public Mono<GithubBranchDetails> getBranchDetails(String token, String owner, String repo, String branch) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/branches/%s", owner, repo, branch);

            return webClient.get()
                .uri(url)
                .header("Authorization", "token " + token)
                .retrieve()
                .bodyToMono(GithubBranchDetails.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting branch details: " + e.getMessage());
        }
    }

    public Mono<Map<String, Object>> getCommitDetails(String token, String owner, String repo, String commit) {
        try {
            String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, commit);

            return webClient.get()
                .uri(url)
                .header("Authorization", "token " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
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
    // public String getBranchSha(String token, String owner, String repo,
    // String branch) {
    // ResponseEntity<Map> response = getBranchDetails(token, owner, repo,
    // branch);
    // Map<String, Object> body = response.getBody();
    // if (body != null) {
    // Map<String, Object> commit = (Map<String, Object>) body.get("commit");
    // return (String) commit.get("sha");
    // }
    // throw new RuntimeException("Failed to get branch SHA");
    // }

}
