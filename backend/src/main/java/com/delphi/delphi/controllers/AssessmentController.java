package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.dtos.FetchAssessmentDto;
import com.delphi.delphi.dtos.NewAssessmentDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.AssessmentService;
import com.delphi.delphi.services.UserService;
import com.delphi.delphi.services.agent.GithubClient;
import com.delphi.delphi.utils.AssessmentStatus;
import com.delphi.delphi.utils.AssessmentType;
import com.delphi.delphi.utils.DelphiGithubConstants;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserService userService;
    private final GithubClient githubClient;

    public AssessmentController(AssessmentService assessmentService, UserService userService, GithubClient githubClient) {
        this.assessmentService = assessmentService;
        this.userService = userService;
        this.githubClient = githubClient;
    }
    
    // Create a new assessment
    @PostMapping
    public ResponseEntity<?> createAssessment(
            @Valid @RequestBody NewAssessmentDto newAssessmentDto,
            @RequestParam Long userId) {
        try {
            Assessment assessment = new Assessment();
            assessment.setName(newAssessmentDto.getName());
            assessment.setDescription(newAssessmentDto.getDescription());
            assessment.setRoleName(newAssessmentDto.getRoleName());
            assessment.setAssessmentType(newAssessmentDto.getAssessmentType());
            assessment.setStartDate(newAssessmentDto.getStartDate());
            assessment.setEndDate(newAssessmentDto.getEndDate());
            assessment.setDuration(newAssessmentDto.getDuration());
            assessment.setSkills(newAssessmentDto.getSkills());
            assessment.setLanguageOptions(newAssessmentDto.getLanguageOptions());
            assessment.setGithubRepoName(assessment.getName().replace(' ', '-'));
            
            // Set user relationship
            User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            assessment.setUser(user);

            // create github repo and add Delphi as a contributor
            githubClient.createRepo(user.getGithubAccessToken(), assessment.getName().replace(' ', '-'));
            githubClient.addContributor(user.getGithubAccessToken(), DelphiGithubConstants.DELPHI_GITHUB_NAME, assessment.getName(), user.getGithubUsername());
            
            Assessment createdAssessment = assessmentService.createAssessment(assessment);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchAssessmentDto(createdAssessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
        }
    }
    
    // Get assessment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getAssessmentById(@PathVariable Long id) {
        try {
            Optional<Assessment> assessment = assessmentService.getAssessmentById(id);
            if (assessment.isPresent()) {
                return ResponseEntity.ok(new FetchAssessmentDto(assessment.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessment: " + e.getMessage());
        }
    }
    
    // Get all assessments with pagination
    @GetMapping
    public ResponseEntity<?> getAllAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Assessment> assessments = assessmentService.getAllAssessments(pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Update assessment
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssessment(@PathVariable Long id, @Valid @RequestBody NewAssessmentDto assessmentUpdates) {
        try {
            Assessment updateAssessment = new Assessment();
            updateAssessment.setName(assessmentUpdates.getName());
            updateAssessment.setDescription(assessmentUpdates.getDescription());
            updateAssessment.setRoleName(assessmentUpdates.getRoleName());
            updateAssessment.setAssessmentType(assessmentUpdates.getAssessmentType());
            updateAssessment.setStartDate(assessmentUpdates.getStartDate());
            updateAssessment.setEndDate(assessmentUpdates.getEndDate());
            updateAssessment.setDuration(assessmentUpdates.getDuration());
            updateAssessment.setSkills(assessmentUpdates.getSkills());
            updateAssessment.setLanguageOptions(assessmentUpdates.getLanguageOptions());
            
            Assessment updatedAssessment = assessmentService.updateAssessment(id, updateAssessment);
            return ResponseEntity.ok(new FetchAssessmentDto(updatedAssessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating assessment: " + e.getMessage());
        }
    }
    
    // Delete assessment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssessment(@PathVariable Long id) {
        try {
            assessmentService.deleteAssessment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting assessment: " + e.getMessage());
        }
    }
    
    // Get assessments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAssessmentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsByUserId(userId, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Get assessments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAssessmentsByStatus(
            @PathVariable AssessmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsByStatus(status, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Get assessments by type
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getAssessmentsByType(
            @PathVariable AssessmentType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsByType(type, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Search assessments by name
    @GetMapping("/search/name")
    public ResponseEntity<?> searchAssessmentsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.searchAssessmentsByName(name, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching assessments: " + e.getMessage());
        }
    }
    
    // Search assessments by role name
    @GetMapping("/search/role")
    public ResponseEntity<?> searchAssessmentsByRoleName(
            @RequestParam String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.searchAssessmentsByRoleName(roleName, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching assessments: " + e.getMessage());
        }
    }
    
    // Get assessments within date range
    @GetMapping("/date-range")
    public ResponseEntity<?> getAssessmentsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsInDateRange(startDate, endDate, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Get active assessments
    @GetMapping("/active")
    public ResponseEntity<?> getActiveAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getActiveAssessmentsInDateRange(LocalDateTime.now(), pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving active assessments: " + e.getMessage());
        }
    }
    
    // Get assessments by duration range
    @GetMapping("/duration-range")
    public ResponseEntity<?> getAssessmentsByDurationRange(
            @RequestParam Integer minDuration,
            @RequestParam Integer maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsByDurationRange(minDuration, maxDuration, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Get assessments by skill
    @GetMapping("/skill/{skill}")
    public ResponseEntity<?> getAssessmentsBySkill(
            @PathVariable String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsBySkill(skill, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Get assessments by language option
    @GetMapping("/language/{language}")
    public ResponseEntity<?> getAssessmentsByLanguageOption(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Assessment> assessments = assessmentService.getAssessmentsByLanguageOption(language, pageable);
            Page<FetchAssessmentDto> assessmentDtos = assessments.map(FetchAssessmentDto::new);
            
            return ResponseEntity.ok(assessmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving assessments: " + e.getMessage());
        }
    }
    
    // Count assessments by user and status
    @GetMapping("/count/user/{userId}/status/{status}")
    public ResponseEntity<?> countAssessmentsByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable AssessmentStatus status) {
        try {
            Long count = assessmentService.countAssessmentsByUserAndStatus(userId, status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error counting assessments: " + e.getMessage());
        }
    }
    
    // Activate assessment
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateAssessment(@PathVariable Long id) {
        try {
            Assessment assessment = assessmentService.activateAssessment(id);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error activating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error activating assessment: " + e.getMessage());
        }
    }
    
    // Deactivate assessment
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateAssessment(@PathVariable Long id) {
        try {
            Assessment assessment = assessmentService.deactivateAssessment(id);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error deactivating assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deactivating assessment: " + e.getMessage());
        }
    }
    
    // Publish assessment
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishAssessment(@PathVariable Long id) {
        try {
            Assessment assessment = assessmentService.publishAssessment(id);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error publishing assessment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error publishing assessment: " + e.getMessage());
        }
    }
    
    // Update skills
    @PutMapping("/{id}/skills")
    public ResponseEntity<?> updateSkills(
            @PathVariable Long id,
            @RequestBody List<String> skills) {
        try {
            Assessment assessment = assessmentService.updateSkills(id, skills);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating skills: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating skills: " + e.getMessage());
        }
    }
    
    // Add skill
    @PostMapping("/{id}/skills")
    public ResponseEntity<?> addSkill(
            @PathVariable Long id,
            @RequestParam String skill) {
        try {
            Assessment assessment = assessmentService.addSkill(id, skill);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error adding skill: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding skill: " + e.getMessage());
        }
    }
    
    // Remove skill
    @DeleteMapping("/{id}/skills/{skill}")
    public ResponseEntity<?> removeSkill(
            @PathVariable Long id,
            @PathVariable String skill) {
        try {
            Assessment assessment = assessmentService.removeSkill(id, skill);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error removing skill: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error removing skill: " + e.getMessage());
        }
    }
    
    // Update language options
    @PutMapping("/{id}/language-options")
    public ResponseEntity<?> updateLanguageOptions(
            @PathVariable Long id,
            @RequestBody List<String> languageOptions) {
        try {
            Assessment assessment = assessmentService.updateLanguageOptions(id, languageOptions);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating language options: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating language options: " + e.getMessage());
        }
    }
    
    // Update metadata
    @PutMapping("/{id}/metadata/new")
    public ResponseEntity<?> updateMetadata(
            @PathVariable Long id,
            @RequestBody Map<String, String> metadata) {
        try {
            Assessment assessment = assessmentService.updateMetadata(id, metadata);
            return ResponseEntity.ok(new FetchAssessmentDto(assessment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating metadata: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating metadata: " + e.getMessage());
        }
    }

    // @PutMapping("/{id}/metadata")
    // public ResponseEntity<?> updateGithubRepo(
    //         @PathVariable Long id,
    //         @RequestBody String key, @RequestBody String value) {
    //     try {
    //         Assessment assessment = assessmentService.updateMetadata(id, Map.of(key, value));
    //         return ResponseEntity.ok(new FetchAssessmentDto(assessment));
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body("Error updating metadata: " + e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body("Error updating metadata: " + e.getMessage());
    //     }
    // }
} 