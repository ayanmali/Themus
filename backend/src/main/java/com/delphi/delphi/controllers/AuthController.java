package com.delphi.delphi.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.components.GithubClient;
import com.delphi.delphi.services.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    private final String TOKEN_URL = "https://github.com/login/oauth/access_token";

    private final UserService userService;

    private final GithubClient githubClient;

    private final RestTemplate restTemplate;

    public AuthController(RestTemplate restTemplate, UserService userService, GithubClient githubClient) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.githubClient = githubClient;
    }

    // public void redirectToGitHub(HttpServletResponse response) throws IOException {
    //     String redirectUri = URLEncoder.encode("http://localhost:8080/oauth/callback", StandardCharsets.UTF_8);
    //     String githubAuthUrl = "https://github.com/login/oauth/authorize?client_id=" + clientId + "&redirect_uri="
    //             + redirectUri + "&scope=repo,user";
    //     response.sendRedirect(githubAuthUrl);
    // }

    @GetMapping("/oauth/github/callback")
    /*
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
        
        // TODO: Store user information and access token securely in database
        // You can now use your UserService to create or update the user
        
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

}
