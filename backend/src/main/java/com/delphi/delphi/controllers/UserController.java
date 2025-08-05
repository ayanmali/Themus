package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
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
import org.springframework.web.servlet.ModelAndView;

import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.git.GithubAccountType;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final String appClientDomain;
    private final String appEnv;
    private final String appInstallUrl;

    private final GithubService githubService;

    private final UserService userService;

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
            GithubService githubService) {
        this.userService = userService;
        // this.clientId = clientId;
        // this.clientSecret = clientSecret;
        this.githubService = githubService;
        this.appClientDomain = appClientDomain;
        this.appEnv = appEnv;

        this.appInstallUrl = String.format("https://github.com/app/%s/installations/new", githubAppName);
    }

    private User getCurrentUser() {
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
            log.info("Checking if user is connected to github");
            User user = getCurrentUser();
            if (user.getGithubAccessToken() == null) {
                response.setHeader("Location", appInstallUrl);
                response.setStatus(302);
                return ResponseEntity.status(HttpStatus.FOUND).build();
            }
            Map<String, Object> githubCredentialsValid = githubService.validateGithubCredentials(user, user.getGithubAccessToken());

            if (!userService.connectedGithub(user) || githubCredentialsValid == null) {
                response.setHeader("Location", appInstallUrl);
                response.setStatus(302);
                return ResponseEntity.status(HttpStatus.FOUND).build();
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
            Page<User> users = userService.getUsersWithFilters(name, organizationName, createdAfter, createdBefore,
                    pageable);
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

    @GetMapping("/github/callback")
    /*
     * This endpoint is automatically called by GitHub after the user has
     * authenticated.
     * Sends a POST request to the GitHub API to get an access token.
     * The access token is used to authenticate the user with the GitHub API.
     * The access token is stored in the database.
     * The access token is used to authenticate the user with the GitHub API.
     * TODO: make this endpoint require authentication
     */
    public ModelAndView callback(@RequestParam String code) {

        // Map<String, String> map = new HashMap<>();
        // map.put("access_token", githubResponse.get("access_token"));
        // map.put("refresh_token", githubResponse.get("refresh_token"));
        // map.put("token_type", githubResponse.get("token_type"));
        // map.put("expires_in", githubResponse.get("expires_in"));
        // map.put("status", "githubResponse");
        try {
            log.info("Getting current user...");
            User user = getCurrentUser();
            log.info("Current user: {}", user.getEmail());
            Map<String, Object> accessTokenResponse = githubService.getAccessToken(code);
            String githubAccessToken = (String) accessTokenResponse.get("access_token");

            log.info("Obtaining github credentials for user: {}", user.getEmail());
            Map<String, Object> githubCredentialsResponse = githubService.validateGithubCredentials(user,
            githubAccessToken);
            String githubUsername = (String) githubCredentialsResponse.get("login");
            String accountType = (String) githubCredentialsResponse.get("type");

            log.info("--------------------------------GITHUB ACCESS TOKEN OBTAINED: " + githubAccessToken + " --------------------------------");
            GithubAccountType githubAccountType = accountType.toLowerCase()
                    .equals("user") ? GithubAccountType.USER : GithubAccountType.ORG;

            log.info("Updating github credentials for user: {}", user.getEmail());
            userService.updateGithubCredentials(user, githubAccessToken, githubUsername, githubAccountType);

            log.info("Github credentials updated for user: {}", user.getEmail());
            return new ModelAndView(String.format("redirect:%s://%s/assessments",
                    appEnv.equals("dev") ? "http" : "https", appClientDomain));
        } catch (Exception e) {
            log.error("Error updating GitHub access token: " + e.getMessage());
            return new ModelAndView(
                    String.format("redirect:%s://%s/login", appEnv.equals("dev") ? "http" : "https", appClientDomain));
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