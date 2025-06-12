package com.delphi.delphi.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth/github")
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    public void redirectToGitHub(HttpServletResponse response) throws IOException {
        String redirectUri = URLEncoder.encode("http://localhost:8080/oauth/callback", StandardCharsets.UTF_8);
        String githubAuthUrl = "https://github.com/login/oauth/authorize?client_id=" + clientId + "&redirect_uri="
                + redirectUri + "&scope=repo,user";
        response.sendRedirect(githubAuthUrl);
    }

    @PostMapping("/oauth/callback")
    public ResponseEntity<?> githubCallback(@RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        String accessToken = (String) response.getBody().get("access_token");

        // Store access token securely (e.g. in DB or session)
        return ResponseEntity.ok("Access token: " + accessToken);
    }

}
