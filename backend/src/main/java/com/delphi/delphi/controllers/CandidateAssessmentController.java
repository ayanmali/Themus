package com.delphi.delphi.controllers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.FetchAssessmentWithAttemptsDto;
import com.delphi.delphi.dtos.StartAssessmentDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.Candidate;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.CandidateService;
import com.delphi.delphi.services.EncryptionService;
import com.delphi.delphi.services.GithubService;
import com.delphi.delphi.utils.AttemptStatus;

/*
 * API endpoints for the candidate as they are taking the assessment
 * include recording API endpoints here as well
 * unauthenticated endpoints
 */
@RestController
@RequestMapping("/api/assessments/live")
public class CandidateAssessmentController {

    private final EncryptionService encryptionService;

    private final AssessmentService assessmentService;

    private final RedisService redisService;
    private final GithubService githubService;
    private final CandidateService candidateService;
    private final String tokenCacheKeyPrefix = "candidate_github_token:";
    private final String usernameCacheKeyPrefix = "candidate_github_username:";
    private final String githubCacheKeyPrefix = "github_install_url_random_string:";
    private final String appInstallBaseUrl;

    public CandidateAssessmentController(RedisService redisService, GithubService githubService, AssessmentService assessmentService, EncryptionService encryptionService, CandidateService candidateService, @Value("${github.app.name}") String githubAppName) {
        this.redisService = redisService;
        this.githubService = githubService;
        this.assessmentService = assessmentService;
        this.encryptionService = encryptionService;
        this.candidateService = candidateService;
        this.appInstallBaseUrl = String.format("https://github.com/apps/%s/installations/new", githubAppName);
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<?> fetchAssessmentWithAttempts(@PathVariable Long assessmentId) {
        Assessment assessment = assessmentService.getAssessmentById(assessmentId).orElseThrow(() -> new RuntimeException("Assessment not found"));
        return ResponseEntity.ok(new FetchAssessmentWithAttemptsDto(assessment));
    }

    // for candidates to generate a github install url
    @PostMapping("/github/generate-install-url")
    public ResponseEntity<?> generateGitHubInstallUrl(@RequestParam String email) {
        try {
            String randomString = UUID.randomUUID().toString();
            redisService.setWithExpiration(githubCacheKeyPrefix + email, randomString, 10, TimeUnit.MINUTES);
            String installUrl = String.format("%s?state=%s_candidate_%s", appInstallBaseUrl, randomString, email);
            return ResponseEntity.ok(installUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating GitHub install URL: " + e.getMessage());
        }
    }

    @GetMapping("/can-take-assessment")
    public ResponseEntity<?> canTakeAssessment(@RequestParam Long assessmentId, @RequestParam String email) {
        // TODO: check if this email address corresponds to a valid candidate attempt in the DB
        // if it does, then we can just redirect to the assessment page
        Assessment assessment = assessmentService.getAssessmentById(assessmentId).orElseThrow(() -> new RuntimeException("Assessment not found"));
        Candidate candidate = candidateService.getCandidateByEmail(email).orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (assessment.getCandidateAttempts().stream().anyMatch(attempt -> attempt.getCandidate().getId().equals(candidate.getId()) && attempt.getStatus().equals(AttemptStatus.INVITED))) {
            return ResponseEntity.ok(
                Map.of("result", true,
                "attemptId", assessment.getCandidateAttempts().stream().filter(attempt -> attempt.getCandidate().getId().equals(candidate.getId()) && attempt.getStatus().equals(AttemptStatus.INVITED)).findFirst().get().getId())
            );
        }
        return ResponseEntity.ok(
            Map.of("result", false)
        );
    }

    @GetMapping("/has-valid-github-token")
    public ResponseEntity<?> hasValidGithubToken(@RequestParam String email) {
        try {
            Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + email);
    
            // get a new token if the candidate doesn't have one or if the token is invalid
            if (candidateGithubToken == null || githubService.validateGithubCredentials(encryptionService.decrypt(candidateGithubToken.toString())) == null) {
                return ResponseEntity.ok(Map.of("result", false));
            }
            return ResponseEntity.ok(Map.of("result", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error checking if candidate has valid Github token: " + e.getMessage());
        }
    }

    /*
     * Create candidate repo from the assessment template repo
     */
    @PostMapping("/start")
    public ResponseEntity<?> createCandidateRepo(@RequestBody StartAssessmentDto startAssessmentDto) {
        try {
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
        String githubAccessToken = encryptionService.decrypt(candidateGithubToken.toString());
        String githubUsername = candidateGithubUsername.toString();

        githubService.createPersonalRepoFromTemplate(githubAccessToken, templateOwner, templateRepoName, repoName);

        return ResponseEntity.ok("Candidate repository created: https://github.com/" + githubUsername + "/" + repoName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting assessment: " + e.getMessage());
        }
    }
}
