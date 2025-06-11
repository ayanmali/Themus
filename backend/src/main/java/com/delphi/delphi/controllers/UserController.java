package com.delphi.delphi.controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.delphi.delphi.dtos.FetchUserDto;
import com.delphi.delphi.dtos.NewUserDto;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Create a new user
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserDto newUserDto) {
        try {
            User user = new User();
            user.setName(newUserDto.getName());
            user.setEmail(newUserDto.getEmail());
            user.setOrganizationName(newUserDto.getOrganizationName());
            user.setPassword(newUserDto.getPassword());
            
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FetchUserDto(createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
        }
    }
    
    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new FetchUserDto(user.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user: " + e.getMessage());
        }
    }
    
    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            Optional<User> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(new FetchUserDto(user.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user: " + e.getMessage());
        }
    }
    
    // Get all users with pagination
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> users = userService.getAllUsers(pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving users: " + e.getMessage());
        }
    }
    
    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody NewUserDto userUpdates) {
        try {
            User updateUser = new User();
            updateUser.setName(userUpdates.getName());
            updateUser.setEmail(userUpdates.getEmail());
            updateUser.setOrganizationName(userUpdates.getOrganizationName());
            updateUser.setPassword(userUpdates.getPassword());
            
            User updatedUser = userService.updateUser(id, updateUser);
            return ResponseEntity.ok(new FetchUserDto(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating user: " + e.getMessage());
        }
    }
    
    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting user: " + e.getMessage());
        }
    }
    
    // Search users by organization
    @GetMapping("/search/organization")
    public ResponseEntity<?> searchUsersByOrganization(
            @RequestParam String organizationName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsersByOrganization(organizationName, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching users: " + e.getMessage());
        }
    }
    
    // Search users by name
    @GetMapping("/search/name")
    public ResponseEntity<?> searchUsersByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsersByName(name, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching users: " + e.getMessage());
        }
    }
    
    // Get users created within date range
    @GetMapping("/created-between")
    public ResponseEntity<?> getUsersCreatedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersCreatedBetween(startDate, endDate, pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving users: " + e.getMessage());
        }
    }
    
    // Get users with active assessments
    @GetMapping("/with-active-assessments")
    public ResponseEntity<?> getUsersWithActiveAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersWithActiveAssessments(pageable);
            Page<FetchUserDto> userDtos = users.map(FetchUserDto::new);
            
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving users: " + e.getMessage());
        }
    }
    
    // Count users by organization
    @GetMapping("/count/organization")
    public ResponseEntity<?> countUsersByOrganization(@RequestParam String organizationName) {
        try {
            Long count = userService.countUsersByOrganization(organizationName);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error counting users: " + e.getMessage());
        }
    }
    
    // Change password
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        try {
            userService.changePassword(id, currentPassword, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error changing password: " + e.getMessage());
        }
    }
    
    // Reset password (admin function)
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        try {
            userService.resetPassword(id, newPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error resetting password: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error resetting password: " + e.getMessage());
        }
    }
    
    // Check if email exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        try {
            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
} 