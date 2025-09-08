package com.delphi.delphi.controllers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.EncryptionService;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.git.GithubAccountType;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final EncryptionService encryptionService;

    // private final String appClientDomain;
    // private final String appEnv;

    private final GithubService githubService;

    private final UserService userService;

    private final RedisService redisService;

    private final String appInstallBaseUrl;

    private final String tokenCacheKeyPrefix = "candidate_github_token:";
    private final String usernameCacheKeyPrefix = "candidate_github_username:";
    private final String githubCacheKeyPrefix = "github_install_url_random_string:";

    // github client id and secret
    // private final String clientId;

    // private final String clientSecret;

    private final Logger log = LoggerFactory.getLogger(UserController.class);

    // private final String TOKEN_URL =
    // "https://github.com/login/oauth/access_token";

    public UserController(UserService userService,
            /*
             * @Value("${spring.security.oauth2.client.registration.github.client-id}")
             * String clientId,
             * 
             * @Value("${spring.security.oauth2.client.registration.github.client-secret}")
             * String clientSecret,
             */
            @Value("${app.client-domain}") String appClientDomain,
            @Value("${app.env}") String appEnv,
            @Value("${github.app.name}") String githubAppName,
            GithubService githubService,
            RedisService redisService, EncryptionService encryptionService) {
        this.userService = userService;
        // this.clientId = clientId;
        // this.clientSecret = clientSecret;
        this.githubService = githubService;
        // this.appClientDomain = appClientDomain;
        // this.appEnv = appEnv;
        this.redisService = redisService;
        this.appInstallBaseUrl = String.format("https://github.com/apps/%s/installations/new", githubAppName);
        this.encryptionService = encryptionService;
    }

    private UserCacheDto getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        log.info("Getting authentication context...");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication context: {}", authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("User details: {}", userDetails);
        return userDetails.getUsername();
    }

    // for the client to check if it is authenticated
    @GetMapping("/is-authenticated")
    public ResponseEntity<FetchUserDto> isAuthenticated() {
        log.info("Checking if user is authenticated");
        return ResponseEntity.ok(new FetchUserDto(getCurrentUser()));
    }

    // for the client to check if it is authenticated
    @GetMapping("/is-connected-github")
    public ResponseEntity<?> isConnectedGithub(HttpServletResponse response) {
        try {
            log.info("Usercontroller - Checking if user is connected to github");
            // check if github credentials exist in DB
            UserCacheDto user = getCurrentUser();
            log.info("Usercontroller - User: {}", user);
            if (!userService.connectedGithub(user)) {
                log.info("Github credentials not found in DB");
                return ResponseEntity.ok(Map.of("redirectUrl", userService.generateGitHubInstallUrl(user.getEmail()), 
                                                "requiresRedirect", true));
            }
            log.info("Usercontroller - Validating github credentials...");
            // check if github credentials are valid
            Map<String, Object> githubCredentialsValid = githubService.validateGithubCredentials(user.getGithubAccessToken());

            log.info("Github credentials valid: {}", githubCredentialsValid);
            if (githubCredentialsValid == null) {
                return ResponseEntity.ok(Map.of("redirectUrl", userService.generateGitHubInstallUrl(user.getEmail()), 
                                                "requiresRedirect", true));
            }

            return ResponseEntity.ok(new FetchUserDto(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking if user is connected to github: " + e.getMessage());
        }
    }

    // Create a new user
    @PostMapping("/new")
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserDto newUserDto) {
        try {
            User user = new User();
            user.setName(newUserDto.getName());
            user.setEmail(newUserDto.getEmail());
            user.setOrganizationName(newUserDto.getOrganizationName());
            user.setPassword(newUserDto.getPassword());

            UserCacheDto createdUser = userService.createUser(user);
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
            UserCacheDto user = userService.getUserByIdOrThrow(id);
            if (user != null) {
                return ResponseEntity.ok(new FetchUserDto(user));
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
            UserCacheDto user = userService.getUserByEmail(email);
            return ResponseEntity.ok(new FetchUserDto(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user: " + e.getMessage());
        }
    }

    // Get all users with pagination and filtering
    // @GetMapping("/filter")
    // public ResponseEntity<?> getAllUsers(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size,
    //         @RequestParam(defaultValue = "id") String sortBy,
    //         @RequestParam(defaultValue = "asc") String sortDirection,
    //         @RequestParam(required = false) String name,
    //         @RequestParam(required = false) String organizationName,
    //         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
    //         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore) {
    //     try {
    //         Sort sort = sortDirection.equalsIgnoreCase("desc")
    //                 ? Sort.by(sortBy).descending()
    //                 : Sort.by(sortBy).ascending();

    //         Pageable pageable = PageRequest.of(page, size, sort);
    //         List<UserCacheDto> users = userService.getUsersWithFilters(name, organizationName, createdAfter, createdBefore,
    //                 pageable);
    //         List<FetchUserDto> userDtos = users.stream()
    //                 .map(FetchUserDto::new)
    //                 .collect(Collectors.toList());

    //         return ResponseEntity.ok(userDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error retrieving users: " + e.getMessage());
    //     }
    // }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody NewUserDto userUpdates) {
        try {
            User updateUser = new User();
            updateUser.setName(userUpdates.getName());
            updateUser.setEmail(userUpdates.getEmail());
            updateUser.setOrganizationName(userUpdates.getOrganizationName());
            updateUser.setPassword(userUpdates.getPassword());

            UserCacheDto updatedUser = userService.updateUser(id, updateUser);
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
    // @GetMapping("/search/organization")
    // public ResponseEntity<?> searchUsersByOrganization(
    //         @RequestParam String organizationName,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         List<UserCacheDto> users = userService.searchUsersByOrganization(organizationName, pageable);
    //         List<FetchUserDto> userDtos = users.stream()
    //                 .map(FetchUserDto::new)
    //                 .collect(Collectors.toList());

    //         return ResponseEntity.ok(userDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error searching users: " + e.getMessage());
    //     }
    // }

    // Search users by name
    // @GetMapping("/search/name")
    // public ResponseEntity<?> searchUsersByName(
    //         @RequestParam String name,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         List<UserCacheDto> users = userService.searchUsersByName(name, pageable);
    //         List<FetchUserDto> userDtos = users.stream()
    //                 .map(FetchUserDto::new)
    //                 .collect(Collectors.toList());

    //         return ResponseEntity.ok(userDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error searching users: " + e.getMessage());
    //     }
    // }

    // Get users created within date range
    // @GetMapping("/created-between")
    // public ResponseEntity<?> getUsersCreatedBetween(
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
    //         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         List<UserCacheDto> users = userService.getUsersCreatedBetween(startDate, endDate, pageable);
    //         List<FetchUserDto> userDtos = users.stream()
    //                 .map(FetchUserDto::new)
    //                 .collect(Collectors.toList());

    //         return ResponseEntity.ok(userDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error retrieving users: " + e.getMessage());
    //     }
    // }

    // Get users with active assessments
    // @GetMapping("/with-active-assessments")
    // public ResponseEntity<?> getUsersWithActiveAssessments(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     try {
    //         Pageable pageable = PageRequest.of(page, size);
    //         List<UserCacheDto> users = userService.getUsersWithActiveAssessments(pageable);
    //         List<FetchUserDto> userDtos = users.stream()
    //                 .map(FetchUserDto::new)
    //                 .collect(Collectors.toList());

    //         return ResponseEntity.ok(userDtos);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error retrieving users: " + e.getMessage());
    //     }
    // }

    // Count users by organization
    // @GetMapping("/count/organization")
    // public ResponseEntity<?> countUsersByOrganization(@RequestParam String organizationName) {
    //     try {
    //         Long count = userService.countUsersByOrganization(organizationName);
    //         return ResponseEntity.ok(count);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error counting users: " + e.getMessage());
    //     }
    // }

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

    // for users to generate a github install url
    @PostMapping("/github/generate-install-url")
    public ResponseEntity<?> generateGitHubInstallUrl() {
        try {
            UserCacheDto user = getCurrentUser();
            String randomString = UUID.randomUUID().toString();
            redisService.setWithExpiration(githubCacheKeyPrefix + user.getEmail(), randomString, 10, TimeUnit.MINUTES);
            String installUrl = String.format("%s?state=%s_user_%s", appInstallBaseUrl, randomString, user.getEmail());
            return ResponseEntity.ok(Map.of("url", installUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating GitHub install URL: " + e.getMessage());
        }
    }

    // unauthenticated endpoint
    // @GetMapping("/github/callback")
    // public ResponseEntity<?> callback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) {
    //     String userOrCandidate = state.split("_")[0].toLowerCase();
    //     switch (userOrCandidate) {
    //         case "user" -> {
    //             // authenticated endpoint
    //             response.setHeader("Location", "http://localhost:8080/api/users/github/callback/user?code=" + code);
    //             response.setStatus(302);
    //             return ResponseEntity.status(HttpStatus.FOUND).build();
    //         }
    //         case "candidate" -> {
    //             // unauthenticated endpoint
    //             response.setHeader("Location", "http://localhost:8080/api/users/github/callback/candidate?code=" + code);
    //             response.setStatus(302);
    //             return ResponseEntity.status(HttpStatus.FOUND).build();
    //         }
    //         default -> throw new IllegalArgumentException("Invalid state: " + state + " - state must start with user_ or candidate_");
    //     }
    // }

    /*
     * TODO: there is a bug where if the user uninstalls the github app, the github API /user method returns 200 when using the access token stored in the DB but that access token won't work for other requests
     * I hate github holy sh
     */
    @GetMapping("/github/callback")
    public ResponseEntity<?> callbackRouter(@RequestParam String code, @RequestParam String state) {
        if (state == null) {
            return ResponseEntity.badRequest().body("State query parameter is required");
        }
        String userOrCandidate;
        if (state.contains("_user_")) {
            userOrCandidate = "user";
        } else if (state.contains("_candidate_")) {
            userOrCandidate = "candidate";
        } else {
            throw new IllegalArgumentException("Invalid state: " + state + " - state must start with user_ or candidate_");
        }
        String providedRandomString = state.split("_" + userOrCandidate + "_")[0];
        String providedEmail = state.split("_" + userOrCandidate + "_")[1];

        // check if the random string passed into the state parameter is valid for the email address
        Object redisRandomString = redisService.get(githubCacheKeyPrefix + providedEmail);
        if (redisRandomString == null || !redisRandomString.toString().equals(providedRandomString)) {
            log.error("Invalid random string: {} for email: {}", providedRandomString, providedEmail + " - state: " + state);
            return ResponseEntity.badRequest().body("Invalid random string in state query parameter: " + providedRandomString + " for email: " + providedEmail + " - state: " + state);
        }

        switch (userOrCandidate) {
            case "user" -> {
                return userCallback(code, providedEmail);
            }
            case "candidate" -> {
                return candidateCallback(code, providedEmail);
            }
            default -> {
                return ResponseEntity.badRequest().body("Invalid state query parameter. Either \"_user_\" or \"_candidate_\" must be present in the state parameter.");
            }
        }
    }

    /*
     * Storing the user's GitHub token in the DB
     * Makes a POST request to the GitHub API using the provided code to get an access token.
     */
    private ResponseEntity<?> userCallback(String code, String providedEmail) {

        // Map<String, String> map = new HashMap<>();
        // map.put("access_token", githubResponse.get("access_token"));
        // map.put("refresh_token", githubResponse.get("refresh_token"));
        // map.put("token_type", githubResponse.get("token_type"));
        // map.put("expires_in", githubResponse.get("expires_in"));
        // map.put("status", "githubResponse");
        try {
            UserCacheDto user = userService.getUserByEmail(providedEmail);
            log.info("Current user: {}", user.getEmail());
            Map<String, Object> accessTokenResponse = githubService.getAccessToken(code);
            String githubAccessToken = (String) accessTokenResponse.get("access_token");

            log.info("Obtaining github credentials for user: {}", user.getEmail());
            Map<String, Object> githubCredentialsResponse = githubService.validateGithubCredentials(githubAccessToken);
            String githubUsername = (String) githubCredentialsResponse.get("login");
            String accountType = (String) githubCredentialsResponse.get("type");

            log.info("--------------------------------GITHUB ACCESS TOKEN OBTAINED: " + githubAccessToken
                    + " --------------------------------");
            GithubAccountType githubAccountType = accountType.toLowerCase()
                    .equals("user") ? GithubAccountType.USER : GithubAccountType.ORG;

            log.info("Updating github credentials for user: {}", user.getEmail());
            userService.updateGithubCredentials(user.getId(), githubAccessToken, githubUsername, githubAccountType);

            log.info("Github credentials updated for user: {}", user.getEmail());
            return ResponseEntity.ok("Github account connected: " + githubUsername);
        } catch (Exception e) {
            log.error("Error updating GitHub access token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error connecting Github account: " + e.getMessage());
        }
    }

    /*
     * Storing the candidate's GitHub token in Redis
     * Makes a POST request to the GitHub API using the provided code to get an access token.
     */
    private ResponseEntity<?> candidateCallback(String code, String email) {
        try {
        Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + email);

        // get a new token if the candidate doesn't have one or if the token is invalid
        if (candidateGithubToken == null || githubService.validateGithubCredentials(encryptionService.decrypt(candidateGithubToken.toString())) == null) {
            // request a token from github api
            Map<String, Object> accessTokenResponse = githubService.getAccessToken(code);
            String githubAccessToken = (String) accessTokenResponse.get("access_token");
            // get candidate's github username
            // TODO: store github username and/or encrypted github token in DB candidate entity
            Map<String, Object> githubCredentialsResponse = githubService.validateGithubCredentials(githubAccessToken);
            String githubUsername = (String) githubCredentialsResponse.get("login");
            
            // store the token and username in redis

            redisService.set(tokenCacheKeyPrefix + email, encryptionService.encrypt(githubAccessToken));
            redisService.set(usernameCacheKeyPrefix + email, githubUsername);
            return ResponseEntity.ok("Github account connected: " + githubUsername);
        }

        return ResponseEntity.ok("Github account already connected. You may close this tab.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error connecting Github account: " + e.getMessage());
        }
    }

    // Webhook endpoint
    // @PostMapping("/github/webhook")
    // public ResponseEntity<String> handleWebhook(@RequestBody String payload,
    // @RequestHeader("X-GitHub-Event") String event,
    // @RequestHeader("X-Hub-Signature-256") String signature) {

    // // Verify webhook signature here for security
    // if (!verifyWebhookSignature(payload, signature)) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid
    // signature");
    // }

    // // Handle different event types
    // switch (event) {
    // case "push" -> handlePushEvent(payload);
    // case "installation" -> handleInstallationEvent(payload);
    // default -> System.out.println("Unhandled event: " + event);
    // }

    // return ResponseEntity.ok("Webhook processed");
    // }

    // private boolean verifyWebhookSignature(String payload, String signature) {
    // // TODO: Implement webhook signature verification
    // return true;
    // }

    // private void handlePushEvent(String payload) {
    // // Process push event
    // //System.out.println("Push event received: " + payload);
    // }

    // private void handleInstallationEvent(String payload) {
    // // Process installation event
    // log.info("Installation event received: " + payload);
    // // TODO: Process installation event

    // // System.out.println("Installation event received: " + payload);
    // }

}