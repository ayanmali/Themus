package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.UserRepository;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Create a new user
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    // Authenticate user
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return user;
    }
    
    // Get user by ID
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Get user by ID or throw exception
    @Transactional(readOnly = true)
    public User getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
    
    // Get user by email
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Get all users with pagination
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    // Update user
    public User updateUser(Long id, User userUpdates) {
        User existingUser = getUserByIdOrThrow(id);
        
        // Update fields if provided
        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }
        if (userUpdates.getEmail() != null && !userUpdates.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userUpdates.getEmail())) {
                throw new IllegalArgumentException("Email " + userUpdates.getEmail() + " is already in use");
            }
            existingUser.setEmail(userUpdates.getEmail());
        }
        if (userUpdates.getOrganizationName() != null) {
            existingUser.setOrganizationName(userUpdates.getOrganizationName());
        }
        if (userUpdates.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userUpdates.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }
    
    // Delete user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    // Check if email exists
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Search users by organization name
    @Transactional(readOnly = true)
    public Page<User> searchUsersByOrganization(String organizationName, Pageable pageable) {
        return userRepository.findByOrganizationNameContainingIgnoreCase(organizationName, pageable);
    }
    
    // Search users by name
    @Transactional(readOnly = true)
    public Page<User> searchUsersByName(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    // Get users created within date range
    @Transactional(readOnly = true)
    public Page<User> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return userRepository.findByCreatedDateBetween(startDate, endDate, pageable);
    }
    
    // Get users with active assessments
    @Transactional(readOnly = true)
    public Page<User> getUsersWithActiveAssessments(Pageable pageable) {
        return userRepository.findUsersWithActiveAssessments(pageable);
    }
    
    // Count users by organization
    @Transactional(readOnly = true)
    public Long countUsersByOrganization(String organizationName) {
        return userRepository.countByOrganizationName(organizationName);
    }
    
    // Get user with assessments
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAssessments(Long userId) {
        return userRepository.findByIdWithAssessments(userId);
    }
    
    // Change password
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserByIdOrThrow(userId);
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    // Reset password (admin function)
    public void resetPassword(Long userId, String newPassword) {
        User user = getUserByIdOrThrow(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    // Get user by GitHub username
    @Transactional(readOnly = true)
    public Optional<User> getUserByGithubUsername(String githubUsername) {
        return userRepository.findByGithubUsername(githubUsername);
    }
    
    // Get user by GitHub access token
    @Transactional(readOnly = true)
    public Optional<User> getUserByGithubAccessToken(String githubAccessToken) {
        return userRepository.findByGithubAccessToken(githubAccessToken);
    }
    
    // Check if GitHub username exists
    @Transactional(readOnly = true)
    public boolean githubUsernameExists(String githubUsername) {
        return userRepository.existsByGithubUsername(githubUsername);
    }
    
    // Check if GitHub access token exists
    @Transactional(readOnly = true)
    public boolean githubAccessTokenExists(String githubAccessToken) {
        return userRepository.existsByGithubAccessToken(githubAccessToken);
    }
    
    // Update user's GitHub credentials
    public User updateGithubCredentials(Long userId, String githubAccessToken, String githubUsername) {
        User user = getUserByIdOrThrow(userId);
        
        // Check if GitHub username is already taken by another user
        if (githubUsername != null && !githubUsername.equals(user.getGithubUsername())) {
            if (userRepository.existsByGithubUsername(githubUsername)) {
                throw new IllegalArgumentException("GitHub username " + githubUsername + " is already in use");
            }
        }
        
        // Check if GitHub access token is already taken by another user
        if (githubAccessToken != null && !githubAccessToken.equals(user.getGithubAccessToken())) {
            if (userRepository.existsByGithubAccessToken(githubAccessToken)) {
                throw new IllegalArgumentException("GitHub access token is already in use");
            }
        }
        
        user.setGithubAccessToken(githubAccessToken);
        user.setGithubUsername(githubUsername);
        
        return userRepository.save(user);
    }
    
    // Remove user's GitHub credentials
    public User removeGithubCredentials(Long userId) {
        User user = getUserByIdOrThrow(userId);
        user.setGithubAccessToken(null);
        user.setGithubUsername(null);
        return userRepository.save(user);
    }
    
    // Find or create user by GitHub credentials
    public User findOrCreateUserByGithub(String githubUsername, String githubAccessToken, String name, String email, String organizationName) {
        // First try to find by GitHub username
        Optional<User> existingUser = getUserByGithubUsername(githubUsername);
        if (existingUser.isPresent()) {
            // Update access token if different
            User user = existingUser.get();
            if (!githubAccessToken.equals(user.getGithubAccessToken())) {
                user.setGithubAccessToken(githubAccessToken);
                return userRepository.save(user);
            }
            return user;
        }
        
        // Check if user exists by email
        existingUser = getUserByEmail(email);
        if (existingUser.isPresent()) {
            // Link GitHub credentials to existing user
            return updateGithubCredentials(existingUser.get().getId(), githubAccessToken, githubUsername);
        }
        
        // Create new user with GitHub credentials
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setOrganizationName(organizationName);
        newUser.setGithubUsername(githubUsername);
        newUser.setGithubAccessToken(githubAccessToken);
        
        // Set a default password (should be changed later)
        newUser.setPassword(passwordEncoder.encode("temp-github-password-" + System.currentTimeMillis()));
        
        return userRepository.save(newUser);
    }
}
