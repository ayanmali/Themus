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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.components.GithubClient;
import com.delphi.delphi.components.JwtService;
import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.dtos.PasswordLoginDto;
import com.delphi.delphi.entities.RefreshToken;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.RefreshTokenService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.exceptions.TokenRefreshException;
import com.delphi.delphi.utils.git.GithubFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    private final GithubClient githubClient;

    private final JwtService jwtService;

    private final long jwtAccessExpiration;

    private final String appEnv;

    private final String appDomain;

    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(RestTemplate restTemplate, UserService userService, GithubClient githubClient,
            JwtService jwtService, RefreshTokenService refreshTokenService,
            AuthenticationManager authenticationManager, @Value("${jwt.access.expiration}") long jwtAccessExpiration,
            @Value("${app.env}") String appEnv, @Value("${app.domain}") String appDomain) {
        this.userService = userService;
        this.githubClient = githubClient;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtAccessExpiration = jwtAccessExpiration;
        this.appEnv = appEnv;
        this.appDomain = appDomain;
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

    @GetMapping("/test")
    public ResponseEntity<GithubFile> test() {
        return githubClient.addFileToRepo("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", "ayanmali",
                "my-new-repo-from-delphi", "test2.txt", null, "\nHello, worldddd!\nballs", "third commit");
    }

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
