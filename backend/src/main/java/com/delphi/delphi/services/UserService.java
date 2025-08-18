package com.delphi.delphi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.dtos.cache.UserCacheDto;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.UserRepository;
import com.delphi.delphi.utils.git.GithubAccountType;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final EncryptionService encryptionService;
    
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;

    private final RedisService redisService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EncryptionService encryptionService, RedisService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
        this.redisService = redisService;
    }
    
    // Create a new user
    @CachePut(value = "users", key = "#user.id")
    public UserCacheDto createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return new UserCacheDto(userRepository.save(user));
    }

    // Authenticate user
    // public User authenticate(String email, String password) {
    //     User user = userRepository.findByEmail(email)
    //         .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
    //     if (!passwordEncoder.matches(password, user.getPassword())) {
    //         throw new IllegalArgumentException("Invalid password");
    //     }

    //     return user;
    // }
    
    // Get user by ID or throw exception
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserCacheDto getUserByIdOrThrow(Long id) {
        return new UserCacheDto(userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id)));
    }
    
    // Get user by email
    @Caching(cacheable = {
        @Cacheable(value = "users", key = "#email")
    }, put = {
        @CachePut(value = "users", key = "#result.id")
    })
    //@CacheEvict(value = "users", key = "#email", beforeInvocation = true)
    @Transactional(readOnly = true)
    public UserCacheDto getUserByEmail(String email) {
        return new UserCacheDto(userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email)));
    }
    
    // Clear user cache for specific email
    // @CacheEvict(value = "users", key = "#email")
    // public void clearUserCache(String email) {
    //     // This method only exists to trigger cache eviction
    //     log.info("Clearing cache for user email: {}", email);
    // }
    
    // Get all users with pagination
    // @Cacheable(value = "users", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<UserCacheDto> getAllUsers(Pageable pageable) {
    //     return userRepository.findAll(pageable).getContent().stream().map(UserCacheDto::new).collect(Collectors.toList());
    // }

    // Get users with multiple filters
    // @Cacheable(value = "users", key = "#name + ':' + #organizationName + ':' + #createdAfter + ':' + #createdBefore + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<UserCacheDto> getUsersWithFilters(String name, String organizationName, LocalDateTime createdAfter, 
    //                                      LocalDateTime createdBefore, Pageable pageable) {
    //     return userRepository.findWithFilters(name, organizationName, createdAfter, createdBefore, pageable).getContent().stream().map(UserCacheDto::new).collect(Collectors.toList());
    // }

    // Update user
    @Caching(put = {
        @CachePut(value = "users", key = "#id"),
        @CachePut(value = "users", key = "#result.email")
    })
    public UserCacheDto updateUser(Long id, User userUpdates) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
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
        
        return new UserCacheDto(userRepository.save(existingUser));
    }
    
    // Delete user
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"),
        @CacheEvict(value = "users", key = "#result.email")
    })
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // checks if user's github credentials exist (not necessarily valid; validateGithubCredentials() should be called to check if they are valid)
    @Cacheable(value = "users", key = "'connectedGithub:' + #user.id")
    @Transactional(readOnly = true)
    public boolean connectedGithub(User user) {
        log.info("UserService - checking if user is connected to github: {}", user);
        return user.getGithubUsername() != null && user.getGithubAccessToken() != null;
    }

    // checks if user's github credentials exist (not necessarily valid; validateGithubCredentials() should be called to check if they are valid)
    @Cacheable(value = "users", key = "'connectedGithub:' + #user.id")
    @Transactional(readOnly = true)
    public boolean connectedGithub(UserCacheDto user) {
        log.info("UserService - checking if user is connected to github: {}", user);
        return user.getGithubUsername() != null && user.getGithubAccessToken() != null;
    }
    
    // Check if email exists
    @Cacheable(value = "users", key = "'emailExists:' + #email")
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Search users by organization name
    // @Cacheable(value = "users", key = "#organizationName + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<UserCacheDto> searchUsersByOrganization(String organizationName, Pageable pageable) {
    //     return userRepository.findByOrganizationNameContainingIgnoreCase(organizationName, pageable).getContent().stream().map(UserCacheDto::new).collect(Collectors.toList());
    // }
    
    // Search users by name
    // @Cacheable(value = "users", key = "#name + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<UserCacheDto> searchUsersByName(String name, Pageable pageable) {
    //     return userRepository.findByNameContainingIgnoreCase(name, pageable).getContent().stream().map(UserCacheDto::new).collect(Collectors.toList());
    // }
    
    // Get users created within date range
    // @Cacheable(value = "users", key = "createdBetween + ':' + #startDate + ':' + #endDate + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<UserCacheDto> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    //     return userRepository.findByCreatedDateBetween(startDate, endDate, pageable).getContent().stream().map(UserCacheDto::new).collect(Collectors.toList());
    // }
    
    // Get users with active assessments
    // @Cacheable(value = "users", key = "activeAssessments + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    // @Transactional(readOnly = true)
    // public List<UserCacheDto> getUsersWithActiveAssessments(Pageable pageable) {
    //     return userRepository.findUsersWithActiveAssessments(pageable).getContent().stream().map(UserCacheDto::new).collect(Collectors.toList());
    // }
    
    // Count users by organization
    // @Cacheable(value = "users", key = "#organizationName")
    // @Transactional(readOnly = true)
    // public Long countUsersByOrganization(String organizationName) {
    //     return userRepository.countByOrganizationName(organizationName);
    // }
    
    // Get user with assessments
    // @Cacheable(value = "users", key = "withAssessments + ':' + #userId")
    // @Transactional(readOnly = true)
    // public UserCacheDto getUserWithAssessments(Long userId) {
    //     return new UserCacheDto(userRepository.findByIdWithAssessments(userId)
    //         .orElseThrow(() -> new IllegalArgumentException("User not found with assessments with id: " + userId)));
    // }
    
    // Change password
    @Caching(evict = {
        @CacheEvict(value = "users", beforeInvocation = true, key = "#userId"),
        @CacheEvict(value = "users", key = "#result.email")
    })
    @Transactional
    public UserCacheDto changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return new UserCacheDto(userRepository.save(user));
    }
    
    // Reset password (admin function)
    @Caching(evict = {
        @CacheEvict(value = "users", beforeInvocation = true, key = "#userId"),
        @CacheEvict(value = "users", key = "#result.email")
    })
    @Transactional
    public UserCacheDto resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        return new UserCacheDto(userRepository.save(user));
    }
    
    // Get user by GitHub username
    // @Cacheable(value = "users", key = "gh_username + ':' + #githubUsername")
    // @Transactional(readOnly = true)
    // public UserCacheDto getUserByGithubUsername(String githubUsername) {
    //     return new UserCacheDto(userRepository.findByGithubUsername(githubUsername)
    //         .orElseThrow(() -> new IllegalArgumentException("User not found with github username: " + githubUsername)));
    // }
    
    // Get user by GitHub access token
    // @Cacheable(value = "users", key = "gh_access_token + ':' + #githubAccessToken")
    // @Transactional(readOnly = true)
    // public UserCacheDto getUserByGithubAccessToken(String githubAccessToken) {
    //     return new UserCacheDto(userRepository.findByGithubAccessToken(githubAccessToken)
    //         .orElseThrow(() -> new IllegalArgumentException("User not found with github access token: " + githubAccessToken)));
    // }
    
    // Check if GitHub username exists
    // @Cacheable(value = "users", key = "gh_username_exists + ':' + #githubUsername")
    // @Transactional(readOnly = true)
    // public boolean githubUsernameExists(String githubUsername) {
    //     return userRepository.existsByGithubUsername(githubUsername);
    // }
    
    // // Check if GitHub access token exists
    // @Cacheable(value = "users", key = "gh_access_token_exists + ':' + #githubAccessToken")
    // @Transactional(readOnly = true)
    // public boolean githubAccessTokenExists(String githubAccessToken) {
    //     return userRepository.existsByGithubAccessToken(githubAccessToken);
    // }
    
    // Update user's GitHub credentials
    @Caching(put = {
        @CachePut(value = "users", key = "#userId"),
        @CachePut(value = "users", key = "#result.email")
    })
    @Transactional
    public UserCacheDto updateGithubCredentials(Long userId, String githubAccessToken, String githubUsername, GithubAccountType githubAccountType) throws Exception {        
        // // Check if GitHub username is already taken by another user
        // if (githubUsername != null && !githubUsername.equals(user.getGithubUsername()) && userRepository.existsByGithubUsername(githubUsername)) {
        //     throw new IllegalArgumentException("GitHub username " + githubUsername + " is already in use");
        // }
        
        // // Check if GitHub access token is already taken by another user
        // if (githubAccessToken != null && !githubAccessToken.equals(user.getGithubAccessToken())) {
        //     if (userRepository.existsByGithubAccessToken(githubAccessToken)) {
        //         throw new IllegalArgumentException("GitHub access token is already in use");
        //     }
        // }
        
        // store the encrypted access token in the DB
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        log.info("Encrypting github access token for user: {}", user.getEmail());
        String encryptedAccessToken = encryptionService.encrypt(githubAccessToken);
        user.setGithubAccessToken(encryptedAccessToken);
        user.setGithubUsername(githubUsername);
        user.setGithubAccountType(githubAccountType);
        log.info("UserService - saving user: {}", user);
        log.info("UserService - user ID: {}", user.getId());

        redisService.set("cache:users:gh_username_exists:" + githubUsername, true);
        redisService.set("cache:users:gh_access_token_exists:" + githubAccessToken, true);
        redisService.set("cache:users:connectedGithub:" + userId, true);

        return new UserCacheDto(userRepository.save(user));
    }

    @Caching(evict = {
        @CacheEvict(value = "users", beforeInvocation = true, key = "#user.id"),
        @CacheEvict(value = "users", beforeInvocation = true, key = "#user.email")
    })
    @Transactional
    public User removeGithubCredentials(User user) throws Exception {
        user.setGithubAccessToken(null);
        user.setGithubUsername(null);
        user.setGithubAccountType(null);

        // evict github caches
        evictGithubCaches(user.getId());
        return userRepository.save(user);
    }

    // Update user's GitHub access token
    // @CachePut(value = "users", key = "#result.id")
    // @Transactional
    // public User updateGithubAccessToken(User user, String githubAccessToken) throws Exception {
    //     // store the encrypted access token in the DB
    //     String encryptedAccessToken = encryptionService.encrypt(githubAccessToken);
    //     user.setGithubAccessToken(encryptedAccessToken);
        
    //     return userRepository.save(user);
    // }
    
    // Remove user's GitHub credentials
    @CacheEvict(value = "users", beforeInvocation = true, key = "#userId")
    @Transactional
    public User removeGithubCredentials(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setGithubAccessToken(null);
        user.setGithubUsername(null);
        evictGithubCaches(userId);
        return userRepository.save(user);
    }
    
    // Find or create user by GitHub credentials
    // @CacheEvict(value = "users", key = "#githubUsername + ':' + #email")
    // public User findOrCreateUserByGithub(String githubUsername, String githubAccessToken, String name, String email, String organizationName) {
    //     // First try to find by GitHub username
    //     Optional<User> existingUser = getUserByGithubUsername(githubUsername);
    //     if (existingUser.isPresent()) {
    //         // Update access token if different
    //         User user = existingUser.get();
    //         if (!githubAccessToken.equals(user.getGithubAccessToken())) {
    //             user.setGithubAccessToken(githubAccessToken);
    //             return userRepository.save(user);
    //         }
    //         return user;
    //     }
        
    //     // Check if user exists by email
    //     existingUser = getUserByEmail(email);
    //     if (existingUser.isPresent()) {
    //         // Link GitHub credentials to existing user
    //         return updateGithubCredentials(existingUser.get().getId(), githubAccessToken, githubUsername);
    //     }
        
    //     // Create new user with GitHub credentials
    //     User newUser = new User();
    //     newUser.setName(name);
    //     newUser.setEmail(email);
    //     newUser.setOrganizationName(organizationName);
    //     newUser.setGithubUsername(githubUsername);
    //     newUser.setGithubAccessToken(githubAccessToken);
        
    //     // Set a default password (should be changed later)
    //     newUser.setPassword(passwordEncoder.encode("temp-github-password-" + System.currentTimeMillis()));
        
    //     return userRepository.save(newUser);
    // }

    // Method to get decrypted token for GitHub operations
    public String getDecryptedGithubToken(Long userId) {
        try {
            // check redis first
            String encryptedGithubToken = getEncryptedGithubToken(userId);
            if (encryptedGithubToken != null) {
                return encryptionService.decrypt(encryptedGithubToken);
            }

            // check db if not in redis
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            if (user.getGithubAccessToken() != null) {
                return encryptionService.decrypt(user.getGithubAccessToken());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting GitHub access token", e);
        }
    }

    private String getEncryptedGithubToken(Long userId) {
        Object encryptedGithubToken = redisService.get("cache:users:encrypted_github_access_token:" + userId);
        if (encryptedGithubToken == null) {
            return null;
        }
        return encryptedGithubToken.toString();
    }

    private void evictGithubCaches(Long userId) {   
        redisService.evictCache("cache:users:connectedGithub:" + userId + ":*");
        redisService.evictCache("cache:users:encrypted_github_access_token:" + userId);
    }
}
