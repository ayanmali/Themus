package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.entities.GithubAppInstallation;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.GithubAppService;
import com.delphi.delphi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

     // github client id and secret (for OAuth Apps)
     private final String clientId;
 
     private final String clientSecret;

     private final RestTemplate restTemplate;

     private final Logger log = LoggerFactory.getLogger(UserController.class);
 
     private final String TOKEN_URL = "https://github.com/login/oauth/access_token";

     // GitHub App service
     private final GithubAppService githubAppService;

    public UserController(UserService userService, 
                         @Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId, 
                         @Value("${spring.security.oauth2.client.registration.github.client-secret}") String clientSecret, 
                         RestTemplate restTemplate,
                         GithubAppService githubAppService) {
        this.userService = userService;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = restTemplate;
        this.githubAppService = githubAppService;
    }

    private User getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    // for the client to check if it is authenticated
    @GetMapping("/is-authenticated")
    public ResponseEntity<FetchUserDto> isAuthenticated() {
        log.info("Checking if user is authenticated");
        return ResponseEntity.ok(new FetchUserDto(getCurrentUser()));
    }

    @GetMapping("/dashboard")
    
    // Create a new user
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserDto newUserDto) {
        try {
            User user = new User();
            user.setName(newUserDto.getName());
            user.setEmail(newUserDto.getEmail());
            user.setOrganizationName(newUserDto.getOrganizationName());
            user.setPassword(newUserDto.getPassword());
            
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchUserDto(createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
        }
    }
    
    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new FetchUserDto(user.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user: " + e.getMessage());
        }
    }
    
    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(new FetchUserDto(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user: " + e.getMessage());
        }
    }
    
    // Get all users with pagination and filtering
    @GetMapping("/filter")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String organizationName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> users = userService.getUsersWithFilters(name, organizationName, createdAfter, createdBefore, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving users: " + e.getMessage());
        }
    }
    
    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody NewUserDto userUpdates) {
        try {
            User updateUser = new User();
            updateUser.setName(userUpdates.getName());
            updateUser.setEmail(userUpdates.getEmail());
            updateUser.setOrganizationName(userUpdates.getOrganizationName());
            updateUser.setPassword(userUpdates.getPassword());
            
            User updatedUser = userService.updateUser(id, updateUser);
            return ResponseEntity.ok(new FetchUserDto(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating user: " + e.getMessage());
        }
    }
    
    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting user: " + e.getMessage());
        }
    }
    
    // Search users by organization
    @GetMapping("/search/organization")
    public ResponseEntity<?> searchUsersByOrganization(
            @RequestParam String organizationName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsersByOrganization(organizationName, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching users: " + e.getMessage());
        }
    }
    
    // Search users by name
    @GetMapping("/search/name")
    public ResponseEntity<?> searchUsersByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsersByName(name, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching users: " + e.getMessage());
        }
    }
    
    // Get users created within date range
    @GetMapping("/created-between")
    public ResponseEntity<?> getUsersCreatedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersCreatedBetween(startDate, endDate, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving users: " + e.getMessage());
        }
    }
    
    // Get users with active assessments
    @GetMapping("/with-active-assessments")
    public ResponseEntity<?> getUsersWithActiveAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersWithActiveAssessments(pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving users: " + e.getMessage());
        }
    }
    
    // Count users by organization
    @GetMapping("/count/organization")
    public ResponseEntity<?> countUsersByOrganization(@RequestParam String organizationName) {
        try {
            Long count = userService.countUsersByOrganization(organizationName);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error counting users: " + e.getMessage());
        }
    }
    
    // Change password
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        try {
            userService.changePassword(id, currentPassword, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error changing password: " + e.getMessage());
        }
    }
    
    // Reset password (admin function)
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        try {
            userService.resetPassword(id, newPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error resetting password: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error resetting password: " + e.getMessage());
        }
    }
    
    // Check if email exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        try {
            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // Initiate GitHub OAuth flow
    @GetMapping("/github/login")
    public ResponseEntity<?> initiateGithubOAuth(@RequestParam(required = false) String redirectUri) {
        try {
            String scope = "user:email"; // Required scopes for user info and email access
            String state = java.util.UUID.randomUUID().toString(); // CSRF protection
            
            // Build GitHub OAuth URL
            String githubAuthUrl = String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&scope=%s&state=%s",
                clientId, scope, state
            );
            
            if (redirectUri != null && !redirectUri.isEmpty()) {
                githubAuthUrl += "&redirect_uri=" + redirectUri;
            }
            
            log.info("Initiating GitHub OAuth flow with URL: {}", githubAuthUrl);
            
            return ResponseEntity.ok(Map.of(
                "authUrl", githubAuthUrl,
                "state", state
            ));
        } catch (Exception e) {
            log.error("Error initiating GitHub OAuth: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error initiating GitHub OAuth: " + e.getMessage());
        }
    }

    // Test GitHub configuration
    @GetMapping("/github/test-config")
    public ResponseEntity<?> testGithubConfig() {
        try {
            // Check if client credentials are configured
            if (clientId == null || clientId.isEmpty()) {
                return ResponseEntity.badRequest().body("GitHub client ID is not configured");
            }
            if (clientSecret == null || clientSecret.isEmpty()) {
                return ResponseEntity.badRequest().body("GitHub client secret is not configured");
            }
            
            // Test with a dummy code to see what GitHub returns
            log.info("GitHub OAuth configuration test:");
            log.info("Client ID: {}", clientId.substring(0, Math.min(clientId.length(), 10)) + "...");
            log.info("Client Secret configured: {}", !clientSecret.isEmpty());
            log.info("Token URL: {}", TOKEN_URL);
            
            return ResponseEntity.ok(Map.of(
                "clientIdConfigured", !clientId.isEmpty(),
                "clientSecretConfigured", !clientSecret.isEmpty(),
                "tokenUrl", TOKEN_URL,
                "message", "GitHub OAuth configuration appears valid"
            ));
        } catch (Exception e) {
            log.error("Error testing GitHub config: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error testing GitHub configuration: " + e.getMessage());
        }
    }

    // @GetMapping("/oauth/github/callback")
    @GetMapping("/github/callback")
    /*
     * This endpoint is automatically called by GitHub after the user has
     * authenticated.
     * Sends a POST request to the GitHub API to get an access token.
     * The access token is used to authenticate the user with the GitHub API.
     * The access token is stored in the database.
     * The access token is used to authenticate the user with the GitHub API.
     */
    public ResponseEntity<?> githubCallback(@RequestParam String code, @RequestParam(required = false) String state) {
        try {
            log.info("Processing GitHub callback with code: {}", code.substring(0, Math.min(code.length(), 10)) + "...");
            
            // Exchange authorization code for access token
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // GitHub expects form data
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            log.info("Requesting access token from GitHub");
            ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get access token. HTTP Status: {}", response.getStatusCode());
                return ResponseEntity.badRequest().body("Failed to get access token from GitHub");
            }

            Map<String, Object> body = response.getBody();
            log.info("Token exchange response: {}", body);
            
            if (body == null) {
                log.error("Token exchange response body is null");
                return ResponseEntity.badRequest().body("Failed to get access token: empty response");
            }
            
            // Check for error in response
            if (body.containsKey("error")) {
                String error = (String) body.get("error");
                String errorDescription = (String) body.get("error_description");
                log.error("GitHub OAuth error: {} - {}", error, errorDescription);
                return ResponseEntity.badRequest().body("GitHub OAuth error: " + error + " - " + errorDescription);
            }
            
            String accessToken = (String) body.get("access_token");
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("Access token is null or empty. Response body: {}", body);
                return ResponseEntity.badRequest().body("Failed to get access token: token is null or empty");
            }
            
            log.info("Successfully obtained access token");

            // Get user information from GitHub API
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            userHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);
            
            log.info("Requesting user information from GitHub API");
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    userRequest,
                    Map.class);

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get user information. HTTP Status: {}, Body: {}", 
                         userResponse.getStatusCode(), userResponse.getBody());
                return ResponseEntity.badRequest().body("Failed to get user information from GitHub API. Status: " + userResponse.getStatusCode());
            }

            Map<String, Object> userBody = userResponse.getBody();
            if (userBody == null) {
                log.error("User information response body is null");
                return ResponseEntity.badRequest().body("Failed to get user information: empty response");
            }

            String githubUsername = (String) userBody.get("login");
            String name = (String) userBody.get("name");
            String email = (String) userBody.get("email");
            
            log.info("Successfully retrieved user information for GitHub user: {}", githubUsername);

            // Updating user's github credentials in DB
            User user = getCurrentUser();
            userService.updateGithubCredentials(user.getId(), accessToken, githubUsername);
            
            log.info("Successfully updated GitHub credentials for user: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "username", githubUsername,
                    "name", name != null ? name : githubUsername,
                    "email", email != null ? email : ""));
        } catch (Exception e) {
            log.error("Error in GitHub callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing GitHub callback: " + e.getMessage());
        }
    }

    // ============= GITHUB APP ENDPOINTS =============

    // Get GitHub App installation URL
    @GetMapping("/github-app/install")
    public ResponseEntity<?> getGithubAppInstallUrl(@RequestParam(required = false) String redirectUri) {
        try {
            // GitHub App installation URL
            String installUrl = String.format(
                "https://github.com/apps/YOUR_APP_NAME/installations/new"
            );
            
            if (redirectUri != null && !redirectUri.isEmpty()) {
                installUrl += "?state=" + java.util.Base64.getEncoder().encodeToString(redirectUri.getBytes());
            }
            
            log.info("Providing GitHub App installation URL: {}", installUrl);
            
            return ResponseEntity.ok(Map.of(
                "installUrl", installUrl,
                "message", "Redirect user to this URL to install the GitHub App"
            ));
        } catch (Exception e) {
            log.error("Error generating GitHub App install URL: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating GitHub App install URL: " + e.getMessage());
        }
    }

    // Handle GitHub App installation callback (webhook)
    @PostMapping("/github-app/installation")
    public ResponseEntity<?> handleGithubAppInstallation(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received GitHub App installation event: {}", payload);
            
            String action = (String) payload.get("action");
            Map<String, Object> installation = (Map<String, Object>) payload.get("installation");
            Map<String, Object> account = (Map<String, Object>) installation.get("account");
            
            Long installationId = ((Number) installation.get("id")).longValue();
            String accountLogin = (String) account.get("login");
            Long accountId = ((Number) account.get("id")).longValue();
            String accountType = (String) account.get("type");
            String repositorySelection = (String) installation.get("repository_selection");
            
            // For now, associate with the currently authenticated user
            // In a real implementation, you might use a state parameter or separate flow
            User currentUser = getCurrentUser();
            
            switch (action) {
                case "created":
                    // New installation
                    GithubAppInstallation newInstallation = githubAppService.createOrUpdateInstallation(
                        installationId, accountLogin, accountId, accountType, 
                        repositorySelection, payload.toString(), currentUser
                    );
                    
                    log.info("Created GitHub App installation for user {} and account {}", 
                            currentUser.getEmail(), accountLogin);
                    
                    return ResponseEntity.ok(Map.of(
                        "message", "GitHub App installed successfully",
                        "installationId", installationId,
                        "accountLogin", accountLogin
                    ));
                    
                case "deleted":
                    // Installation removed
                    githubAppService.removeInstallation(installationId);
                    log.info("Removed GitHub App installation {} for account {}", installationId, accountLogin);
                    
                    return ResponseEntity.ok(Map.of(
                        "message", "GitHub App installation removed",
                        "installationId", installationId
                    ));
                    
                case "suspend":
                    // Installation suspended
                    githubAppService.suspendInstallation(installationId);
                    log.info("Suspended GitHub App installation {} for account {}", installationId, accountLogin);
                    
                    return ResponseEntity.ok(Map.of(
                        "message", "GitHub App installation suspended",
                        "installationId", installationId
                    ));
                    
                default:
                    log.warn("Unhandled GitHub App installation action: {}", action);
                    return ResponseEntity.ok(Map.of("message", "Event received but not handled"));
            }
            
        } catch (Exception e) {
            log.error("Error handling GitHub App installation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error handling GitHub App installation: " + e.getMessage());
        }
    }

    // Test GitHub App configuration
    @GetMapping("/github-app/test-config")
    public ResponseEntity<?> testGithubAppConfig() {
        try {
            boolean isValid = githubAppService.isConfigurationValid();
            
            if (!isValid) {
                return ResponseEntity.badRequest().body("GitHub App configuration is invalid");
            }
            
            // Get app info
            Map<String, Object> appInfo = githubAppService.getAppInfo();
            String appName = (String) appInfo.get("name");
            String appSlug = (String) appInfo.get("slug");
            
            return ResponseEntity.ok(Map.of(
                "configurationValid", true,
                "appName", appName,
                "appSlug", appSlug,
                "message", "GitHub App configuration is valid"
            ));
        } catch (Exception e) {
            log.error("Error testing GitHub App config: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error testing GitHub App configuration: " + e.getMessage());
        }
    }

    // Get user's GitHub App installations
    @GetMapping("/github-app/installations")
    public ResponseEntity<?> getUserGithubAppInstallations() {
        try {
            User currentUser = getCurrentUser();
            List<GithubAppInstallation> installations = githubAppService.getUserInstallations(currentUser);
            
            return ResponseEntity.ok(installations.stream().map(installation -> Map.of(
                "installationId", installation.getInstallationId(),
                "accountLogin", installation.getAccountLogin(),
                "accountType", installation.getAccountType(),
                "repositorySelection", installation.getRepositorySelection(),
                "createdDate", installation.getCreatedDate(),
                "suspended", installation.isSuspended()
            )).toList());
        } catch (Exception e) {
            log.error("Error retrieving user GitHub App installations: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving GitHub App installations: " + e.getMessage());
        }
    }

    // Get installation access token for GitHub API calls
    @GetMapping("/github-app/installation/{installationId}/token")
    public ResponseEntity<?> getInstallationToken(@PathVariable Long installationId) {
        try {
            User currentUser = getCurrentUser();
            
            // Verify that the user has access to this installation
            List<GithubAppInstallation> userInstallations = githubAppService.getUserInstallations(currentUser);
            boolean hasAccess = userInstallations.stream()
                .anyMatch(inst -> inst.getInstallationId().equals(installationId));
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied to installation " + installationId);
            }
            
            String accessToken = githubAppService.getInstallationAccessToken(installationId);
            
            return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "installationId", installationId,
                "message", "Installation token retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving installation token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving installation token: " + e.getMessage());
        }
    }

    // Manual installation setup (alternative to webhook)
    @PostMapping("/github-app/setup")
    public ResponseEntity<?> setupGithubAppInstallation(
            @RequestParam Long installationId,
            @RequestParam String accountLogin) {
        try {
            User currentUser = getCurrentUser();
            
            // Verify installation exists and get details from GitHub
            List<Map<String, Object>> allInstallations = githubAppService.getAppInstallations();
            
            Optional<Map<String, Object>> installationOpt = allInstallations.stream()
                .filter(inst -> ((Number) inst.get("id")).longValue() == installationId)
                .findFirst();
            
            if (installationOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Installation " + installationId + " not found");
            }
            
            Map<String, Object> installation = installationOpt.get();
            Map<String, Object> account = (Map<String, Object>) installation.get("account");
            
            Long accountId = ((Number) account.get("id")).longValue();
            String accountType = (String) account.get("type");
            String repositorySelection = (String) installation.get("repository_selection");
            
            // Create the installation record
            GithubAppInstallation appInstallation = githubAppService.createOrUpdateInstallation(
                installationId, accountLogin, accountId, accountType, 
                repositorySelection, installation.toString(), currentUser
            );
            
            log.info("Manually set up GitHub App installation {} for user {}", 
                    installationId, currentUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "message", "GitHub App installation set up successfully",
                "installationId", installationId,
                "accountLogin", accountLogin
            ));
            
        } catch (Exception e) {
            log.error("Error setting up GitHub App installation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error setting up GitHub App installation: " + e.getMessage());
        }
    }
} 