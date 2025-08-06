package com.delphi.delphi.controllers;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.services.GithubService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * API endpoints for the candidate as they are taking the assessment
 * include recording API endpoints here as well
 * unauthenticated endpoints
 */
@RestController
@RequestMapping("/api/assessments/live")
public class CandidateAssessmentController {

    private final RedisService redisService;
    private final GithubService githubService;
    private final String cacheKeyPrefix = "candidate_github_token:";

    public CandidateAssessmentController(RedisService redisService, GithubService githubService) {
        this.redisService = redisService;
        this.githubService = githubService;
    }
    
    /*
     * Storing the candidate's GitHub token
     */
    @PostMapping("/github/callback")
    public ResponseEntity<?> githubCallback(@RequestParam String code, @RequestParam String state) {
        String email = state.split("_")[1];
        Object candidateGithubToken = redisService.get(cacheKeyPrefix + email);

        if (candidateGithubToken == null) {
            // request a token from github api
            Map<String, Object> accessTokenResponse = githubService.getAccessToken(code, true);
            String githubAccessToken = (String) accessTokenResponse.get("access_token");
            // store the token in redis
            redisService.set(cacheKeyPrefix + email, githubAccessToken);
            return ResponseEntity.ok("Github account connected. You may close this tab.");
        }

        return ResponseEntity.ok("Github account already connected. You may close this tab.");
    }

    /*
     * Create candidate repo from the assessment template repo
     */
    @PostMapping("/create-repo")
    public ResponseEntity<?> startAssessment(@RequestBody NewCandidateAssessmentDto newCandidateAssessmentDto) {
        return ResponseEntity.ok("Assessment started");
    }
}
