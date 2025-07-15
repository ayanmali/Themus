package com.delphi.delphi.controllers;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.JwtService;
import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.dtos.PasswordLoginDto;
import com.delphi.delphi.entities.RefreshToken;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.services.RefreshTokenService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.exceptions.TokenRefreshException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GithubService githubService;

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    private final JwtService jwtService;

    private final long jwtAccessExpiration;

    private final String appEnv;

    private final String appDomain;

    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService,
            JwtService jwtService, RefreshTokenService refreshTokenService,
            AuthenticationManager authenticationManager, @Value("${jwt.access.expiration}") long jwtAccessExpiration,
            @Value("${app.env}") String appEnv, @Value("${app.domain}") String appDomain, GithubService githubService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtAccessExpiration = jwtAccessExpiration;
        this.appEnv = appEnv;
        this.appDomain = appDomain;
        this.githubService = githubService;
    }

    private User getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        
        switch (principal) {
            case UserDetails userDetails -> {
                return userDetails.getUsername();
            }
            case String string -> {
                return string;
            }
            default -> throw new RuntimeException("Unknown principal type: " + principal.getClass());
        }
    }

    private void authenticateUser(String email, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.getUserByEmail(email);

        // Delete any existing refresh token for this user before creating a new one
        // This prevents the unique constraint violation since RefreshToken has @OneToOne with User
        refreshTokenService.deleteRefreshToken(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Set the access token in the cookie
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setMaxAge((int) (jwtAccessExpiration / 1000));
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(appEnv.equals("prod"));
        accessCookie.setPath("/");
        
        // Set the refresh token in a separate cookie
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days (same as refresh token expiration)
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(appEnv.equals("prod"));
        refreshCookie.setPath("/");
        
        // Only set domain in production to avoid issues with empty domain
        if (appEnv.equals("prod")) {
            accessCookie.setDomain(appDomain);
            refreshCookie.setDomain(appDomain);
        }
        
        accessCookie.setAttribute("SameSite", "Lax");
        refreshCookie.setAttribute("SameSite", "Lax");

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    // @GetMapping("/test")
    // public ResponseEntity<Map<String, Object>> test() {
    //     Map<String, Object> result = new HashMap<>();
        
    //     try {
    //         // Test both OAuth and GitHub App flows
            
    //         // Option 1: Test OAuth flow (if you have OAuth App set up)
    //         // String oauthToken = "YOUR_OAUTH_TOKEN_HERE"; // gho_ prefix for OAuth tokens
    //         // if (!oauthToken.equals("YOUR_OAUTH_TOKEN_HERE")) {
    //         //     log.info("Testing OAuth token...");
    //         //     Map<String, Object> userInfo = githubService.validateToken(oauthToken).block();
    //         //     result.put("oauth_user_info", userInfo);
                
    //         //     String scopes = githubService.getTokenScopes(oauthToken).block();
    //         //     result.put("oauth_scopes", scopes);
                
    //         //     if (scopes != null && scopes.contains("repo")) {
    //         //         GithubRepoContents repo = githubService.createRepo(oauthToken, "oauth-test-repo-" + System.currentTimeMillis()).block();
    //         //         result.put("oauth_repository", repo);
    //         //     }
    //         // }
            
    //         // Option 2: Test GitHub App installation flow

    //         String installationId = "75837596"; // Your installation ID
    //         if (!installationId.equals("YOUR_INSTALLATION_ID")) {
    //             log.info("Testing GitHub App installation token...");
                
    //             // First, get installation info
    //             Map<String, Object> installationInfo = githubService.getInstallationInfo(installationId).block();
    //             result.put("installation_info", installationInfo);
    //             log.info("Installation info: {}", installationInfo);
                
    //             // Then get installation token
    //             String installationToken = githubService.getInstallationToken(installationId);
    //             result.put("installation_token_prefix", installationToken.substring(0, Math.min(10, installationToken.length())));
                
    //             // Try to create repository with installation token in the installation's account
    //             String owner = "delphi-assessments"; // From installation info
    //             try {
    //                 GithubRepoContents repo = githubService.createRepoWithInstallation(installationToken, "app-test-repo-" + System.currentTimeMillis(), owner).block();
    //                 result.put("app_repository", repo);
    //             } catch (Exception e) {
    //                 log.warn("Failed to create repository: {}", e.getMessage());
    //                 result.put("create_repo_error", e.getMessage());
                    
    //                 // Try to test with an existing repository instead
    //                 try {
    //                     Map<String, Object> repoContents = githubService.testInstallationToken(installationToken, owner, "test-repo").block();
    //                     result.put("existing_repo_test", repoContents);
    //                 } catch (Exception e2) {
    //                     log.warn("Failed to test with existing repo: {}", e2.getMessage());
    //                     result.put("existing_repo_error", e2.getMessage());
    //                 }
    //             }
    //         }
            
    //         result.put("message", "Check which authentication method worked");
    //         result.put("success", true);
            
    //         return ResponseEntity.ok(result);
            
    //     } catch (Exception e) {
    //         log.error("Test failed", e);
    //         result.put("error", e.getMessage());
    //         result.put("success", false);
    //         return ResponseEntity.status(500).body(result);
    //     }
    // }

    @PostMapping("/signup/email")
    public ResponseEntity<?> registerEmail(@Valid @RequestBody NewUserDto newUserDto, HttpServletResponse response) {
        User user = new User();
        user.setName(newUserDto.getName());
        user.setEmail(newUserDto.getEmail());
        user.setOrganizationName(newUserDto.getOrganizationName());
        user.setPassword(newUserDto.getPassword()); // sets the raw password -- sefvice method encrypts it
        userService.createUser(user);

        // add auth cookie to response
        authenticateUser(newUserDto.getEmail(), newUserDto.getPassword(), response);

        return ResponseEntity.ok(new FetchUserDto(user));
    }

    @PostMapping("/login/email")
    public ResponseEntity<?> loginEmail(@RequestBody PasswordLoginDto passwordLoginDto, HttpServletResponse response) {
        // add cookies to response
        authenticateUser(passwordLoginDto.getEmail(), passwordLoginDto.getPassword(), response);

        return ResponseEntity.ok(new FetchUserDto(getCurrentUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get refresh token from cookies
            String refreshTokenValue = null;
            Cookie[] cookies = request.getCookies();
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshTokenValue = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (refreshTokenValue == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
            }

            // verify refresh token
            RefreshToken refreshTokenEntity = refreshTokenService.verifyRefreshToken(refreshTokenValue);
            if (refreshTokenEntity.isUsed() || refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
                refreshTokenService.deleteRefreshToken(refreshTokenEntity);
                // user must log in again
                // throw new TokenRefreshException("Refresh token expired or used");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired or used");
            }

            // Delete the old refresh token (implements token rotation)
            refreshTokenService.deleteRefreshToken(refreshTokenEntity);

            User user = refreshTokenEntity.getUser();
            UserDetails userDetails = userService.getUserByEmail(user.getEmail());

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

            // Set new access token cookie
            Cookie accessCookie = new Cookie("accessToken", newAccessToken);
            accessCookie.setMaxAge((int) (jwtAccessExpiration / 1000));
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(appEnv.equals("prod"));
            accessCookie.setPath("/");
            
            // Set new refresh token cookie
            Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken.getToken());
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(appEnv.equals("prod"));
            refreshCookie.setPath("/");
            
            if (appEnv.equals("prod")) {
                accessCookie.setDomain(appDomain);
                refreshCookie.setDomain(appDomain);
            }
            
            accessCookie.setAttribute("SameSite", "Lax");
            refreshCookie.setAttribute("SameSite", "Lax");

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);

            return ResponseEntity.ok(new FetchUserDto(user));
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logging out");
        try {
            // Get refresh token from cookies to delete it from database
            String refreshTokenValue = null;
            Cookie[] cookies = request.getCookies();
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshTokenValue = cookie.getValue();
                        break;
                    }
                }
            }
            
            // Delete refresh token from database if found
            if (refreshTokenValue != null) {
                try {
                    RefreshToken refreshTokenEntity = refreshTokenService.verifyRefreshToken(refreshTokenValue);
                    refreshTokenService.deleteRefreshToken(refreshTokenEntity);
                } catch (Exception e) {
                    // Continue with logout even if refresh token cleanup fails
                }
            }
            
            // Clear access token cookie
            Cookie accessCookie = new Cookie("accessToken", null);
            accessCookie.setMaxAge(0);
            log.info("Cleared access token cookie");
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(appEnv.equals("prod"));
            accessCookie.setPath("/");
            
            // Clear refresh token cookie
            Cookie refreshCookie = new Cookie("refreshToken", null);
            refreshCookie.setMaxAge(0);
            log.info("Cleared refresh token cookie");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(appEnv.equals("prod"));
            refreshCookie.setPath("/");
            
            if (appEnv.equals("prod")) {
                accessCookie.setDomain(appDomain);
                refreshCookie.setDomain(appDomain);
            }
            
            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);
            
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
        }
    }

}
