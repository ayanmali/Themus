package com.delphi.delphi.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Create a new user
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
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
}
