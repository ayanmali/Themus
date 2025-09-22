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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.delphi.delphi.utils.Constants;
import com.delphi.delphi.utils.git.GithubBranchDetails;
import com.delphi.delphi.utils.git.GithubFile;
import com.delphi.delphi.utils.git.GithubFileResponse;
import com.delphi.delphi.utils.git.GithubReference;
import com.delphi.delphi.utils.git.GithubRepoBranch;
import com.delphi.delphi.utils.git.GithubRepoContents;
import com.delphi.delphi.utils.git.GithubRepoInvitation;

import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

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
 * github.com/apps/themus-assessments
 */

public class GithubService {

    private final EncryptionService encryptionService;

    private final Logger log = LoggerFactory.getLogger(GithubService.class);

    // private final ChatMessageRepository chatMessageRepository;
    private final String appId;
    private final String clientId;
    private final String clientSecret;
    private final String githubScope;

    // private final String candidateAppClientId;
    // private final String candidateAppClientSecret;

    private final WebClient webClient;
    private final Base64.Encoder base64Encoder;
    private final Base64.Decoder base64Decoder;
    private final String privateKeyRaw;

    private PrivateKey privateKey;

    private final String THEMUS_GITHUB_TOKEN;

    public GithubService(@Value("${github.app.app-id}") String appId,
            @Value("${github.app.client-id}") String clientId,
            @Value("${github.app.client-secret}") String clientSecret,
            @Value("${github.app.private-key}") String privateKeyRaw,
            // @Value("${github.candidate.app.client-id}") String candidateAppClientId,
            // @Value("${github.candidate.app.client-secret}") String
            // candidateAppClientSecret,
            @Value("${spring.security.oauth2.client.registration.github.scope}") String githubScope,
            @Value("${themus.github.token}") String themusGithubToken,
            EncryptionService encryptionService) {
        this.appId = appId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.privateKeyRaw = privateKeyRaw;
        this.githubScope = githubScope;
        this.THEMUS_GITHUB_TOKEN = themusGithubToken;
        // this.candidateAppClientId = candidateAppClientId;
        // this.candidateAppClientSecret = candidateAppClientSecret;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
        this.base64Encoder = Base64.getEncoder();
        this.base64Decoder = Base64.getDecoder();

        loadPrivateKey();

        // this.chatMessageRepository = chatMessageRepository;

        // this.chatMessageRepository = chatMessageRepository;
        this.encryptionService = encryptionService;

        // this.chatMessageRepository = chatMessageRepository;

        // this.chatMessageRepository = chatMessageRepository;

        // this.chatMessageRepository = chatMessageRepository;

        // this.chatMessageRepository = chatMessageRepository;
    }

    private void loadPrivateKey() {
        try {
            // Clean up the private key string - remove line continuations and normalize
            // whitespace
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
                log.error(
                        "Please convert your private key using: openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in rsa_key.pem -out pkcs8_key.pem");
                log.error(
                        "Then update your application.properties to use the PKCS#8 key (should start with -----BEGIN PRIVATE KEY-----)");
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
                throw new RuntimeException(
                        "Unknown private key format. Expected PKCS#8 format starting with -----BEGIN PRIVATE KEY-----");
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
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
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
                                log.error("Installation info error - Status: {}, Body: {}", response.statusCode(),
                                        errorBody);
                                return Mono.error(new RuntimeException(
                                        String.format("Installation info error %d: %s", response.statusCode().value(),
                                                errorBody)));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    // OAuth flow - redirect user to GitHub
    public String getAuthorizationUrl(String state) {
        return String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&scope=%s&state=%s",
                clientId, githubScope, state);
    }

    // Exchange code for github app user access token
    public Map<String, Object> getAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        // TODO: figure out how to get new access token from refresh token
        // params.add("grant_type", "authorization_code");
        // params.add("refresh_token", refreshToken);

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
                                        String.format("Access token error %d: %s", resp.statusCode().value(),
                                                errorBody)));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("Failed to get access token: " + response);
        }

        return response;
    }

    /*
     * Uses the user's user access token to get their user information from the
     * GitHub API (if the token is valid)
     * MAKE SURE TO USE THE DECRYPTED ACCESS TOKEN, NOT THE ENCRYPTED ONE
     */
    public Map<String, Object> validateGithubCredentials(String githubAccessToken) {
        try {
            if (githubAccessToken == null) {
                return null;
            }
            // decrypting the access token if it is encrypted
            String token = githubAccessToken;
            if (!githubAccessToken.startsWith("ghu_") && !githubAccessToken.startsWith("gho_")) {
                token = encryptionService.decrypt(githubAccessToken);
            }
            String uri = "/user";
            // String token = encryptionService.decrypt(user.getGithubAccessToken());
            Map<String, Object> result = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        return response.bodyToMono(Map.class)
                                .flatMap(errorBody -> {
                                    log.error("Error checking github credentials w/ Github API - Status: {}, Body: {}",
                                            response.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("GitHub API error %d: %s", response.statusCode().value(),
                                                    errorBody)));
                                });
                    })
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (result == null) {
                // throw new RuntimeException("Github credentials are null");
                return null;
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
    // return webClient
    // .get()
    // .uri("https://api.github.com/user")
    // .header("Authorization", "token " + token)
    // .header("Accept", "application/vnd.github.v3+json")
    // .header("User-Agent", "Delphi-App/1.0")
    // .retrieve()
    // .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
    // response -> {
    // return response.bodyToMono(String.class)
    // .flatMap(errorBody -> {
    // log.error("Token validation error - Status: {}, Body: {}",
    // response.statusCode(), errorBody);
    // return Mono.error(new RuntimeException(
    // String.format("Token validation error %d: %s", response.statusCode().value(),
    // errorBody)
    // ));
    // });
    // })
    // .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    // }

    // Check token scopes
    // public Mono<String> getTokenScopes(String token) {
    // return webClient
    // .get()
    // .uri("https://api.github.com/user")
    // .header("Authorization", "token " + token)
    // .header("Accept", "application/vnd.github.v3+json")
    // .header("User-Agent", "Delphi-App/1.0")
    // .retrieve()
    // .toEntity(String.class)
    // .map(response -> {
    // String scopes = response.getHeaders().getFirst("X-OAuth-Scopes");
    // log.info("Token scopes: {}", scopes);
    // return scopes != null ? scopes : "No scopes found";
    // })
    // .onErrorReturn("Error retrieving scopes");
    // }

    public GithubRepoContents createPersonalRepo(String token, String owner, String repoName) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            log.info("Creating personal repo with token: {}...", githubAccessToken);
            // For GitHub App installation tokens, we need to create repo in the
            // installation's account
            String url = "https://api.github.com/user/repos";

            Map<String, Object> body = Map.of(
                    "name", repoName,
                    "description", "Assessment repository",
                    "private", true,
                    "auto_init", false,
                    "is_template", true);

            log.info("Creating repo '{}' with token: {}...", repoName,
                    githubAccessToken.substring(0, Math.min(10, token.length())));

            GithubRepoContents repo = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Themus-App/1.0")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        log.error("Error creating personal repo - Status: {}, Body: {}", response.statusCode(),
                                response.bodyToMono(String.class).block());
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("GitHub API error - Status: {}, Body: {}", response.statusCode(),
                                            errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("GitHub API error %d: %s", response.statusCode().value(),
                                                    errorBody)));
                                });
                    })
                    .bodyToMono(GithubRepoContents.class)
                    .block();

            if (repo == null) {
                throw new RuntimeException(
                        "Failed to create personal repo: repo details from Github API response is null");
            }

            // add the Themus GitHub account as collaborator to the repo
            // necessary so the candidate repos can be created in the Themus GitHub org
            // account
            addCollaborator(githubAccessToken, owner, repoName, Constants.THEMUS_USERNAME);

            // Get all invitations for the repo
            List<GithubRepoInvitation> invitations = getInvitations(githubAccessToken, owner, repoName);
            // Accept all invitations
            for (GithubRepoInvitation invitation : invitations) {
                if (invitation.getInvitee().getLogin().equals(Constants.THEMUS_USERNAME)) {
                    acceptInvitation(THEMUS_GITHUB_TOKEN, invitation.getId());
                }
            }

            return repo;
        } catch (RestClientException e) {
            log.error("RestClient error creating repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating personal repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a repository on the official Themus GitHub account
     * 
     * @param templateRepoName
     * @param repoName
     * @return
     */
    public GithubRepoContents createCandidateRepo(String userGithubUsername, String templateRepoName, String repoName) {
        GithubRepoContents repo = createPersonalRepoFromTemplate(THEMUS_GITHUB_TOKEN, userGithubUsername, templateRepoName, repoName);
        // create a branch for the candidate to work in
        addBranch(THEMUS_GITHUB_TOKEN, Constants.THEMUS_ORG_NAME, repoName, "assessment", "main");
        return repo;
    }

    /**
     * Adds the candidate as a contributor to the repository (owned by the Themus
     * account)
     * 
     * @param repoName
     * @param candidateGithubUsername
     */
    public void addCollaboratorToCandidateRepo(String repoName, String candidateGithubUsername, String candidateGithubToken) {
        addCollaborator(THEMUS_GITHUB_TOKEN, Constants.THEMUS_ORG_NAME, repoName, candidateGithubUsername);
        try {
            // Get all invitations for the repo
            String githubAccessToken = candidateGithubToken;
            if (!candidateGithubToken.startsWith("ghu_") && !candidateGithubToken.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(candidateGithubToken);
            }
            List<GithubRepoInvitation> invitations = getInvitations(THEMUS_GITHUB_TOKEN, Constants.THEMUS_ORG_NAME,
                    repoName);
            // Accept all invitations
            for (GithubRepoInvitation invitation : invitations) {
                if (invitation.getInvitee().getLogin().equals(candidateGithubUsername)) {
                    acceptInvitation(githubAccessToken, invitation.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error adding collaborator to candidate repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error adding collaborator to candidate repo: " + e.getMessage(), e);
        }
    }

    public void addCollaborator(String token, String owner, String repo, String username) {
        try {
            log.info("Adding collaborator {} to repo {}/{}", username, owner, repo);
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            String url = String.format("https://api.github.com/repos/%s/%s/collaborators/%s", owner, repo, username);
            webClient.put()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (RestClientException e) {
            log.error("RestClient error adding contributor: {}", e.getMessage(), e);
            throw new RuntimeException("Error adding contributor: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error adding contributor: {}", e.getMessage(), e);
            throw new RuntimeException("Error adding contributor: " + e.getMessage(), e);
        }
    }

    public GithubRepoContents createPersonalRepoFromTemplate(String token, String templateOwner,
            String templateRepoName, String repoName) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            log.info("Creating personal repo from template with token: {}...", githubAccessToken);
            log.info("Template owner: {}", templateOwner);
            log.info("Template repo name: {}", templateRepoName);
            log.info("Repo name: {}", repoName);
            // For GitHub App installation tokens, we need to create repo in the
            // installation's account
            String url = String.format("https://api.github.com/repos/%s/%s/generate", templateOwner, templateRepoName);

            Map<String, Object> body = Map.of(
                    "owner", Constants.THEMUS_ORG_NAME,
                    "name", repoName,
                    "description", "Candidate repository",
                    "private", true,
                    "include_all_branches", true);

            log.info("Creating candidate repo '{}' from template repo '{}' with token: {}...", repoName,
                    templateRepoName, githubAccessToken.substring(0, Math.min(10, token.length())));

            GithubRepoContents repo = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Themus-App/1.0")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        log.error("Error creating personal repo - Status: {}, Body: {}", response.statusCode(),
                                response.bodyToMono(String.class).block());
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("GitHub API error - Status: {}, Body: {}", response.statusCode(),
                                            errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("GitHub API error %d: %s", response.statusCode().value(),
                                                    errorBody)));
                                });
                    })
                    .bodyToMono(GithubRepoContents.class)
                    .block();

            if (repo == null) {
                throw new RuntimeException(
                        "Failed to create personal repo: repo details from Github API response is null");
            }

            return repo;
        } catch (RestClientException e) {
            log.error("RestClient error creating repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating personal repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
    }

    public GithubRepoContents createOrgRepo(String token, String orgName, String repoName) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            // For GitHub App installation tokens, we need to create repo in the
            // installation's account
            String url = String.format("https://api.github.com/orgs/%s/repos", orgName);

            Map<String, Object> body = Map.of(
                    "name", repoName,
                    "description", "Assessment repository",
                    "private", true,
                    "auto_init", true,
                    "has_downloads", false,
                    "has_issues", true,
                    "has_wiki", true,
                    "is_template", true);

            log.info("Creating repo '{}' with token: {}...", repoName,
                    githubAccessToken.substring(0, Math.min(10, token.length())));

            GithubRepoContents repo = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Themus-App/1.0")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("GitHub API error - Status: {}, Body: {}", response.statusCode(),
                                            errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("GitHub API error %d: %s", response.statusCode().value(),
                                                    errorBody)));
                                });
                    })
                    .bodyToMono(GithubRepoContents.class)
                    .block();

            if (repo == null) {
                throw new RuntimeException("Failed to create org repo: repo details from Github API response is null");
            }

            // adding the Themus org as a contributor to the repo
            addCollaborator(githubAccessToken, orgName, repoName, Constants.THEMUS_USERNAME);

            // Get all invitations for the repo
            List<GithubRepoInvitation> invitations = getInvitations(githubAccessToken, orgName, repoName);
            // Accept all invitations
            for (GithubRepoInvitation invitation : invitations) {
                if (invitation.getInvitee().getLogin().equals(Constants.THEMUS_USERNAME)) {
                    acceptInvitation(THEMUS_GITHUB_TOKEN, invitation.getId());
                }
            }

            return repo;
        } catch (RestClientException e) {
            log.error("RestClient error creating repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating org repo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating repo: " + e.getMessage(), e);
        }
    }

    public List<GithubRepoInvitation> getInvitations(String token, String owner, String repo) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            // For GitHub App installation tokens, we need to create repo in the
            // installation's account
            String url = String.format("https://api.github.com/repos/%s/%s/invitations", owner, repo);
            return webClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Themus-App/1.0")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GithubRepoInvitation>>() {
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error getting invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting invitation: " + e.getMessage(), e);
        }
    }

    public String acceptInvitation(String token, Long invitationId) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }
            // For GitHub App installation tokens, we need to create repo in the
            // installation's account
            String url = String.format("https://api.github.com/user/repository_invitations/%s", invitationId);
            return webClient.patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Themus-App/1.0")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error accepting invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Error accepting invitation: " + e.getMessage(), e);
        }
    }

    // Test installation token with existing repository operations
    // public Mono<Map<String, Object>> testInstallationToken(String
    // installationToken, String owner, String repo) {
    // try {
    // log.info("Testing installation token with repo '{}/{}'", owner, repo);

    // // Test getting repository contents
    // return webClient.get()
    // .uri("https://api.github.com/repos/{owner}/{repo}/contents", owner, repo)
    // .header("Authorization", "Bearer " + installationToken)
    // .header("Accept", "application/vnd.github.v3+json")
    // .header("User-Agent", "Delphi-App/1.0")
    // .retrieve()
    // .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
    // response -> {
    // return response.bodyToMono(String.class)
    // .flatMap(errorBody -> {
    // log.error("GitHub API error testing installation - Status: {}, Body: {}",
    // response.statusCode(), errorBody);
    // return Mono.error(new RuntimeException(
    // String.format("GitHub API error %d: %s", response.statusCode().value(),
    // errorBody)
    // ));
    // });
    // })
    // .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    // } catch (RestClientException e) {
    // log.error("RestClient error testing installation: {}", e.getMessage(), e);
    // throw new RuntimeException("Error testing installation: " + e.getMessage(),
    // e);
    // }
    // }

    // public String addCollaborator(String token, String owner, String repo, String
    // username) {
    // try {
    // String url =
    // String.format("https://api.github.com/repos/%s/%s/collaborators/%s", owner,
    // repo, username);

    // //HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
    // return webClient.put()
    // .uri(url)
    // .header("Authorization", "token " + token)
    // .retrieve()
    // .bodyToMono(String.class)
    // .block();
    // } catch (RestClientException e) {
    // throw new RuntimeException("Error adding contributor: " + e.getMessage());
    // }
    // }

    public Mono<GithubFile> addFileToRepo(String token, String owner, String repo, String path,
            String branch, String content, String commitMessage) {
        try {
            log.info("ADD FILE REQUEST:");
            log.info("TOKEN: {}", token);
            log.info("OWNER: {}", owner);
            log.info("REPO: {}", repo);
            log.info("PATH: {}", path);
            log.info("BRANCH: {}", branch);
            log.info("CONTENT: {}", content);
            log.info("COMMIT MESSAGE: {}", commitMessage);

            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            log.info("Github token decrypted");

            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            log.info("Sending request to: {}", url);
            String base64Content = encodeToBase64(content);

            log.info("Content encoded to base64");

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "author", Constants.AUTHOR);
            if (branch != null) {
                body.put("branch", branch);
            }

            log.info("Body created");

            // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return webClient.put()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        log.error("Github API Error - Status: {}, Body: {}", response.statusCode(),
                                response.bodyToMono(String.class).block());
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("GitHub API error - Status: {}, Body: {}", response.statusCode(),
                                            errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("GitHub API error %d: %s", response.statusCode().value(),
                                                    errorBody)));
                                });
                    })
                    .bodyToMono(GithubFileResponse.class)
                    .map(response -> response.getContent());
        } catch (RestClientException e) {
            throw new RuntimeException("Error making request to add file to repo: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error adding file to repo: " + e.getMessage());
        }
    }

    public GithubRepoContents getRepoContents(String token, String owner, String repo,
            String path, String branch) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
            if (branch != null) {
                url += "?ref=" + branch;
            }

            GithubRepoContents repoContentsResponse = webClient.get()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    .retrieve()
                    .bodyToMono(GithubRepoContents.class).block();

            if (repoContentsResponse != null) {
                if (repoContentsResponse.getType().equals("file")) {
                    repoContentsResponse.setContent(decodeFromBase64(repoContentsResponse.getContent()));
                }
                return repoContentsResponse;
            }
            return null;
        } catch (RestClientException e) {
            throw new RuntimeException("Error making request to get repo contents: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error getting repo contents: " + e.getMessage());
        }
    }

    public Mono<List<GithubRepoBranch>> getRepoBranches(String token, String owner, String repo) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);

            return webClient.get()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GithubRepoBranch>>() {
                    });
        } catch (RestClientException e) {
            throw new RuntimeException("Error making request to get repo branches: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error getting repo branches: " + e.getMessage());
        }
    }

    public Mono<GithubReference> addBranch(String token, String owner, String repo, String branchName,
            String baseBranch) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/git/refs", owner, repo);

            // Get the SHA of the base branch first
            Mono<GithubBranchDetails> branchResponse = getBranchDetails(githubAccessToken, owner, repo, baseBranch);
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
                    .header("Authorization", "token " + githubAccessToken)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GithubReference.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error making request to add branch: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error adding branch: " + e.getMessage());
        }
    }

    public Mono<GithubFile> editFile(String token, String owner, String repo, String path, String content,
            String commitMessage, String sha) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            String base64Content = encodeToBase64(content);

            Map<String, Object> body = Map.of(
                    "message", commitMessage,
                    "content", base64Content,
                    "sha", sha,
                    "author", Constants.AUTHOR);

            return webClient.put()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GithubFileResponse.class)
                    .map(response -> response.getContent());
        } catch (RestClientException e) {
            throw new RuntimeException("Error making request to edit file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error editing file: " + e.getMessage());
        }
    }

    public Mono<String> deleteFile(String token, String owner, String repo, String path,
            String commitMessage, String sha) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

            // Map<String, Object> body = Map.of(
            // "message", commitMessage,
            // "sha", sha,
            // "author", author);

            return webClient.delete()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    // .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }

    public Mono<GithubBranchDetails> getBranchDetails(String token, String owner, String repo, String branch) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/branches/%s", owner, repo, branch);

            return webClient.get()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    .retrieve()
                    .bodyToMono(GithubBranchDetails.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting branch details: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error getting branch details: " + e.getMessage());
        }
    }

    public Mono<Map<String, Object>> getCommitDetails(String token, String owner, String repo, String commit) {
        try {
            String githubAccessToken = token;
            if (!token.startsWith("ghu_") && !token.startsWith("gho_")) {
                githubAccessToken = encryptionService.decrypt(token);
            }

            String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, commit);

            return webClient.get()
                    .uri(url)
                    .header("Authorization", "token " + githubAccessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting commit details: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error getting commit details: " + e.getMessage());
        }
    }

    // TODO: see if this fixes the duplicate message bug
    // public ChatMessage sendMessageToUser(String text, Long assessmentId, String
    // model) {
    // try {
    // log.info("--------------------------------");
    // log.info("SENDING MESSAGE TO USER - GITHUB SERVICE:");
    // log.info("Message: {}", text.substring(0, Math.min(text.length(), 100)) +
    // "...");
    // log.info("Assessment ID: {}", assessmentId);
    // log.info("Model: {}", model);
    // log.info("--------------------------------");
    // Assessment assessment = assessmentRepository.findById(assessmentId)
    // .orElseThrow(() -> new Exception("Assessment not found with id: " +
    // assessmentId));
    // ChatMessage chatMessage = new ChatMessage("TOOL CALL -- " + text, List.of(),
    // assessment, MessageType.ASSISTANT, model);
    // ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
    // assessment.addMessage(savedChatMessage);
    // assessmentRepository.save(assessment);
    // return savedChatMessage;
    // } catch (Exception e) {
    // throw new RuntimeException("Error sending message: " + e.getMessage());
    // }
    // }

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

    // GitHub API requires base64-encoded content for file contents
    private String encodeToBase64(String content) {
        return base64Encoder.encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeFromBase64(String content) {
        return new String(base64Decoder.decode(content), StandardCharsets.UTF_8);
    }

}
