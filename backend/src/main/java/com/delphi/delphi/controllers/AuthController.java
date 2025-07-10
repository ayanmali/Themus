package com.delphi.delphi.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.components.GithubClient;
import com.delphi.delphi.components.JwtService;
import com.delphi.delphi.dtos.AuthResponseDto;
import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.dtos.PasswordLoginDto;
import com.delphi.delphi.entities.RefreshToken;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.RefreshTokenService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.utils.exceptions.TokenRefreshException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    // github client id and secret
    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    private final String TOKEN_URL = "https://github.com/login/oauth/access_token";

    private final UserService userService;

    private final GithubClient githubClient;

    private final RestTemplate restTemplate;

    private final JwtService jwtService;

    public AuthController(RestTemplate restTemplate, UserService userService, GithubClient githubClient, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.githubClient = githubClient;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/oauth/github/callback")
    /*
     * This endpoint is automatically called by GitHub after the user has authenticated.
     * Sends a POST request to the GitHub API to get an access token.
     * The access token is used to authenticate the user with the GitHub API.
     * The access token is stored in the database.
     * The access token is used to authenticate the user with the GitHub API.
     */
    public ResponseEntity<?> githubCallback(@RequestParam String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null) {
            return ResponseEntity.badRequest().body("Failed to get access token");
        }
        String accessToken = (String) body.get("access_token");


        // Get user information from GitHub API
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
            "https://api.github.com/user", 
            HttpMethod.GET, 
            userRequest, 
            Map.class
        );
        
        Map<String, Object> userBody = userResponse.getBody();
        if (userBody == null) {
            return ResponseEntity.badRequest().body("Failed to get user information");
        }
        
        String githubUsername = (String) userBody.get("login");
        String name = (String) userBody.get("name");
        String email = (String) userBody.get("email");
        
        return ResponseEntity.ok(Map.of(
            "access_token", accessToken,
            "username", githubUsername,
            "name", name != null ? name : githubUsername,
            "email", email != null ? email : ""
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return githubClient.addFileToRepo("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", "ayanmali", "my-new-repo-from-delphi", "test2.txt", null, "\nHello, worldddd!\nballs", "third commit");
    }

    
    @PostMapping("/signup/email")
    public ResponseEntity<?> registerEmail(@Valid @RequestBody NewUserDto newUserDto) {
        User user = new User();
        user.setName(newUserDto.getName());
        user.setEmail(newUserDto.getEmail());
        user.setOrganizationName(newUserDto.getOrganizationName());
        user.setPassword(newUserDto.getPassword()); // sets the raw password -- sefvice method encrypts it
        userService.createUser(user);

        return ResponseEntity.ok(new FetchUserDto(user));
    }

    @PostMapping("/login/email")
    public ResponseEntity<?> loginEmail(@RequestBody PasswordLoginDto passwordLoginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(passwordLoginDto.getEmail(), passwordLoginDto.getPassword())
        );
        
        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken, userDetails.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        try {
            // verify refresh token
            RefreshToken refreshTokenEntity = refreshTokenService.verifyRefreshToken(refreshToken);
            if (refreshTokenEntity.isUsed() || refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
                refreshTokenService.deleteRefreshToken(refreshTokenEntity);
                throw new TokenRefreshException("Refresh token expired or used");
            }

            // Mark current token as used
            refreshTokenEntity.setUsed(true);
            refreshTokenService.save(refreshTokenEntity);

            User user = refreshTokenEntity.getUser();
            UserDetails userDetails = userService.getUserByEmail(user.getEmail())
                                        .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            // String newRefreshToken = jwtService.generateRefreshToken(userDetails);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            
            return ResponseEntity.ok(new AuthResponseDto(newAccessToken, newRefreshToken.getToken(), userDetails.getUsername()));
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.verifyRefreshToken(refreshToken);
        refreshTokenService.deleteRefreshToken(refreshTokenEntity);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

}
