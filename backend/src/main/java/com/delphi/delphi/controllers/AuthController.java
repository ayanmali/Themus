package com.delphi.delphi.controllers;

import java.time.Instant;
import java.util.Map;

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

    private final UserService userService;

    private final GithubClient githubClient;

    private final JwtService jwtService;

    public AuthController(RestTemplate restTemplate, UserService userService, GithubClient githubClient,
            JwtService jwtService, RefreshTokenService refreshTokenService,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.githubClient = githubClient;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return githubClient.addFileToRepo("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", "ayanmali",
                "my-new-repo-from-delphi", "test2.txt", null, "\nHello, worldddd!\nballs", "third commit");
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
                new UsernamePasswordAuthenticationToken(passwordLoginDto.getEmail(), passwordLoginDto.getPassword()));

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

            return ResponseEntity
                    .ok(new AuthResponseDto(newAccessToken, newRefreshToken.getToken(), userDetails.getUsername()));
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
