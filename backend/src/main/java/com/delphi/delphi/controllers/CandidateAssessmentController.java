// package com.delphi.delphi.controllers;

// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.TimeUnit;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.delphi.delphi.components.RedisService;
// import com.delphi.delphi.dtos.PreviewAssessmentDto;
// import com.delphi.delphi.dtos.cache.CandidateAttemptCacheDto;
// import com.delphi.delphi.dtos.cache.CandidateCacheDto;
// import com.delphi.delphi.entities.Assessment;
// import com.delphi.delphi.entities.CandidateAttempt;
// import com.delphi.delphi.services.*;
// import com.delphi.delphi.utils.enums.AttemptStatus;

// /*
//  * API endpoints for the candidate as they are taking the assessment
//  * include recording API endpoints here as well
//  * unauthenticated endpoints
//  */
// @RestController
// @RequestMapping("/api/assessments/live")
// public class CandidateAssessmentController {

//     private final CandidateAttemptService candidateAttemptService;

//     private final EncryptionService encryptionService;

//     private final AssessmentService assessmentService;

//     private final RedisService redisService;
//     private final GithubService githubService;
//     private final CandidateService candidateService;
//     private final String tokenCacheKeyPrefix = "candidate_github_token:";
//     private final String usernameCacheKeyPrefix = "candidate_github_username:";
//     private final String githubCacheKeyPrefix = "github_install_url_random_string:";
//     private final String appInstallBaseUrl;

//     public CandidateAssessmentController(RedisService redisService, GithubService githubService,
//             AssessmentService assessmentService, EncryptionService encryptionService, CandidateService candidateService,
//             @Value("${github.app.name}") String githubAppName, CandidateAttemptService candidateAttemptService) {
//         this.redisService = redisService;
//         this.githubService = githubService;
//         this.assessmentService = assessmentService;
//         this.encryptionService = encryptionService;
//         this.candidateService = candidateService;
//         this.appInstallBaseUrl = String.format("https://github.com/apps/%s/installations/new", githubAppName);
//         this.candidateAttemptService = candidateAttemptService;
//     }

//     /*
//      * Create candidate repo from the assessment template repo
//      */
//     @PostMapping("/start")
//     public ResponseEntity<?> createCandidateRepo(@RequestBody StartAssessmentDto startAssessmentDto) {
//         try {
//             String encryptedPassword = redisService.get(passwordCacheKeyPrefix + startAssessmentDto.getCandidateEmail());
//             if (!startAssessmentDto.getPassword().equals(encryptionService.decrypt(encryptedPassword))) {
//                 return ResponseEntity.badRequest().body("Invalid password");
//             }

//             CandidateCacheDto candidate = candidateService.getCandidateByEmail(startAssessmentDto.getCandidateEmail());
//             Long candidateId = candidate.getId();
//             candidate.get
//             String repoName = "assessment-" + startAssessmentDto.getAssessmentId() + "-" + String.valueOf(Instant.now().toEpochMilli());

//             CandidateAttempt updates = new CandidateAttempt();
//             updates.setLanguageChoice(startAssessmentDto.getLanguageOption());
//             updates.setGithubRepositoryLink(githubCacheKeyPrefix);(repoName);
//             updates.setStatus(AttemptStatus.STARTED);
//             updates.setStartedDate(LocalDateTime.now());

//             CandidateAttemptCacheDto candidateAttempt = candidateAttemptService.updateCandidateAttempt(candidateId, updates);
            

//             // TODO: store repository owner in assessment entity
//             Assessment assessment = assessmentService.getAssessmentById(assessmentId);
//             String templateOwner = assessment.getUser().getGithubUsername();
//             String templateRepoName = assessment.getGithubRepoName();

//             // check if the candidate has a github token
//             Object candidateGithubToken = redisService.get(tokenCacheKeyPrefix + candidateEmail);
//             Object candidateGithubUsername = redisService.get(usernameCacheKeyPrefix + candidateEmail);

//             if (candidateGithubToken == null || candidateGithubUsername == null
//                     || githubService.validateGithubCredentials(candidateGithubToken.toString()) == null) {
//                 return ResponseEntity.ok(
//                         "Github account not connected. Please connect your Github account to start the assessment.");
//             }
//             String githubAccessToken = encryptionService.decrypt(candidateGithubToken.toString());
//             String githubUsername = candidateGithubUsername.toString();

//             githubService.createPersonalRepoFromTemplate(githubAccessToken, templateOwner, templateRepoName, repoName);

//             return ResponseEntity
//                     .ok("Candidate repository created: https://github.com/" + githubUsername + "/" + repoName);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("Error starting assessment: " + e.getMessage());
//         }
//     }
// }
