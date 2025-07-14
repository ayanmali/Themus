package com.delphi.delphi.services;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.delphi.delphi.entities.GithubAppInstallation;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.GithubAppInstallationRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class GithubAppService {
    
    private static final Logger log = LoggerFactory.getLogger(GithubAppService.class);
    
    private final String appId;
    private final String privateKeyPem;
    private final GithubAppInstallationRepository installationRepository;
    private final EncryptionService encryptionService;
    private final RestTemplate restTemplate;
    private final PrivateKey privateKey;
    
    public GithubAppService(
            @Value("${github.app.id}") String appId,
            @Value("${github.app.private-key}") String privateKeyPem,
            GithubAppInstallationRepository installationRepository,
            EncryptionService encryptionService,
            RestTemplate restTemplate) {
        this.appId = appId;
        this.privateKeyPem = privateKeyPem;
        this.installationRepository = installationRepository;
        this.encryptionService = encryptionService;
        this.restTemplate = restTemplate;
        this.privateKey = parsePrivateKey(privateKeyPem);
    }
    
    /**
     * Parse the PEM private key string into a PrivateKey object
     */
    private PrivateKey parsePrivateKey(String privateKeyPem) {
        try {
            // Remove PEM headers and whitespace
            String privateKeyBase64 = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            // Decode the base64 string
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            
            // Create the private key
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to parse GitHub App private key", e);
        }
    }
    
    /**
     * Generate a JWT token for GitHub App authentication
     */
    public String generateAppJWT() {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(600); // 10 minutes (GitHub's max)
        
        return Jwts.builder()
            .setIssuer(appId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .compact();
    }
    
    /**
     * Get installation access token for a specific installation
     */
    public String getInstallationAccessToken(Long installationId) {
        // Check if we have a cached valid token
        Optional<GithubAppInstallation> installationOpt = installationRepository.findByInstallationId(installationId);
        if (installationOpt.isPresent()) {
            GithubAppInstallation installation = installationOpt.get();
            if (!installation.isTokenExpired() && installation.getInstallationToken() != null) {
                try {
                    return encryptionService.decrypt(installation.getInstallationToken());
                } catch (Exception e) {
                    log.warn("Failed to decrypt cached installation token, refreshing", e);
                }
            }
        }
        
        // Generate new installation token
        return refreshInstallationToken(installationId);
    }
    
    /**
     * Refresh installation access token from GitHub
     */
    public String refreshInstallationToken(Long installationId) {
        try {
            String appJWT = generateAppJWT();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(appJWT);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            String url = String.format("https://api.github.com/app/installations/%d/access_tokens", installationId);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get installation access token: " + response.getStatusCode());
            }
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response when getting installation access token");
            }
            
            String accessToken = (String) responseBody.get("token");
            String expiresAtStr = (String) responseBody.get("expires_at");
            
            // Update the installation token in database
            Optional<GithubAppInstallation> installationOpt = installationRepository.findByInstallationId(installationId);
            if (installationOpt.isPresent()) {
                GithubAppInstallation installation = installationOpt.get();
                installation.setInstallationToken(encryptionService.encrypt(accessToken));
                installation.setTokenExpiresAt(LocalDateTime.parse(expiresAtStr.replace("Z", "")));
                installationRepository.save(installation);
            }
            
            log.info("Successfully refreshed installation token for installation {}", installationId);
            return accessToken;
            
        } catch (Exception e) {
            log.error("Failed to refresh installation token for installation " + installationId, e);
            throw new RuntimeException("Failed to refresh installation token", e);
        }
    }
    
    /**
     * Get user's GitHub App installations
     */
    public List<GithubAppInstallation> getUserInstallations(User user) {
        return installationRepository.findActiveInstallationsByUser(user);
    }
    
    /**
     * Create or update GitHub App installation
     */
    @Transactional
    public GithubAppInstallation createOrUpdateInstallation(
            Long installationId, String accountLogin, Long accountId, 
            String accountType, String repositorySelection, String permissions, User user) {
        
        Optional<GithubAppInstallation> existingOpt = installationRepository.findByInstallationId(installationId);
        
        GithubAppInstallation installation;
        if (existingOpt.isPresent()) {
            installation = existingOpt.get();
            installation.setAccountLogin(accountLogin);
            installation.setAccountId(accountId);
            installation.setAccountType(accountType);
            installation.setRepositorySelection(repositorySelection);
            installation.setPermissions(permissions);
            installation.setUser(user);
            installation.setSuspendedAt(null); // Clear suspension if it was suspended
        } else {
            installation = new GithubAppInstallation(
                installationId, accountLogin, accountId, accountType, 
                repositorySelection, permissions, user
            );
        }
        
        return installationRepository.save(installation);
    }
    
    /**
     * Remove GitHub App installation
     */
    @Transactional
    public void removeInstallation(Long installationId) {
        installationRepository.deleteByInstallationId(installationId);
    }
    
    /**
     * Suspend GitHub App installation
     */
    @Transactional
    public void suspendInstallation(Long installationId) {
        Optional<GithubAppInstallation> installationOpt = installationRepository.findByInstallationId(installationId);
        if (installationOpt.isPresent()) {
            GithubAppInstallation installation = installationOpt.get();
            installation.setSuspendedAt(LocalDateTime.now());
            installationRepository.save(installation);
        }
    }
    
    /**
     * Get GitHub App info and installations accessible by the app
     */
    public Map<String, Object> getAppInfo() {
        try {
            String appJWT = generateAppJWT();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(appJWT);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.github.com/app", HttpMethod.GET, entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get app info", e);
            throw new RuntimeException("Failed to get GitHub App info", e);
        }
    }
    
    /**
     * List installations for the GitHub App
     */
    public List<Map<String, Object>> getAppInstallations() {
        try {
            String appJWT = generateAppJWT();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(appJWT);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/app/installations", HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get app installations", e);
            throw new RuntimeException("Failed to get GitHub App installations", e);
        }
    }
    
    /**
     * Check if GitHub App configuration is valid
     */
    public boolean isConfigurationValid() {
        try {
            generateAppJWT();
            return true;
        } catch (Exception e) {
            log.error("GitHub App configuration is invalid", e);
            return false;
        }
    }
} 