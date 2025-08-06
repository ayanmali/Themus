package com.delphi.delphi.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.StartAssessmentDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.GithubService;

/*
 * API endpoints for the candidate as they are taking the assessment
 * include recording API endpoints here as well
 * unauthenticated endpoints
 */
@RestController
@RequestMapping("/api/assessments/live")
public class CandidateAssessmentController {

    private final AssessmentService assessmentService;

    private final RedisService redisService;
    private final GithubService githubService;
    private final String tokenCacheKeyPrefix = "candidate_github_token:";
    private final String usernameCacheKeyPrefix = "candidate_github_username:";

    public CandidateAssessmentController(RedisService redisService, GithubService githubService, AssessmentService assessmentService) {
        this.redisService = redisService;
        this.githubService = githubService;
        this.assessmentService = assessmentService;
    }
    
    /*
     * Storing the candidate's GitHub token
     */
    @PostMapping("/github/callback")
    public ResponseEntity<?> githubCallback(@RequestParam String code, @RequestParam String state) {
        String email = state.split("_")[1];
        Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + email);

        // get a new token if the candidate doesn't have one or if the token is invalid
        if (candidateGithubToken == null || githubService.validateGithubCredentials(candidateGithubToken.toString()) == null) {
            // request a token from github api
            Map<String, Object> accessTokenResponse = githubService.getAccessToken(code, true);
            String githubAccessToken = (String) accessTokenResponse.get("access_token");
            String githubUsername = (String) accessTokenResponse.get("login");
            
            // store the token and usernamein redis
            redisService.set(tokenCacheKeyPrefix + email, githubAccessToken);
            redisService.set(usernameCacheKeyPrefix + email, githubUsername);
            return ResponseEntity.ok("Github account connected: " + githubUsername);
        }

        return ResponseEntity.ok("Github account already connected. You may close this tab.");
    }

    /*
     * Create candidate repo from the assessment template repo
     */
    @PostMapping("/start")
    public ResponseEntity<?> startAssessment(@RequestBody StartAssessmentDto startAssessmentDto) {
        String candidateEmail = startAssessmentDto.getCandidateEmail();
        Long assessmentId = startAssessmentDto.getAssessmentId();
        String languageOption = startAssessmentDto.getLanguageOption();
        String repoName = "assessment-" + assessmentId + "-" + candidateEmail.split("@")[0];

        // TODO: store repository owner in assessment entity
        Assessment assessment = assessmentService.getAssessmentById(assessmentId).orElseThrow(() -> new RuntimeException("Assessment not found"));
        String templateOwner = assessment.getUser().getGithubUsername();
        String templateRepoName = assessment.getGithubRepoName();
        
        // check if the candidate has a github token
        Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + candidateEmail);
        Object candidateGithubUsername = redisService.get(usernameCacheKeyPrefix + candidateEmail);

        if (candidateGithubToken == null || candidateGithubUsername == null || githubService.validateGithubCredentials(candidateGithubToken.toString()) == null) {
            return ResponseEntity.ok("Github account not connected. Please connect your Github account to start the assessment.");
        }
        String githubAccessToken = candidateGithubToken.toString();
        String githubUsername = candidateGithubUsername.toString();

        githubService.createPersonalRepoFromTemplate(githubAccessToken, templateOwner, templateRepoName, repoName);

        return ResponseEntity.ok("Assessment started: https://github.com/" + githubUsername + "/" + repoName);
    }
}
