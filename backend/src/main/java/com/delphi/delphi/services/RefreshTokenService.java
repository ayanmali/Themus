package com.delphi.delphi.services;

import java.time.Instant;
import com.delphi.delphi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.JwtService;
import com.delphi.delphi.dtos.cache.RefreshTokenCacheDto;
import com.delphi.delphi.entities.RefreshToken;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.RefreshTokenRepository;

@Service
@Transactional
public class RefreshTokenService {

    private final UserRepository userRepository;

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Long refreshTokenExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, @Value("${jwt.refresh.expiration}") Long refreshTokenExpiration, JwtService jwtService, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @CachePut(value = "refreshTokens", key = "#user.id")
    @Transactional
    public RefreshTokenCacheDto createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtService.generateRefreshToken((UserDetails) user));
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpiration / 1000));
        return new RefreshTokenCacheDto(refreshTokenRepository.save(refreshToken));
    }

    @CachePut(value = "refreshTokens", key = "#refreshToken.user.id")
    @Transactional
    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    //@Cacheable(value = "refreshTokens", key = "verify + ':' + #token")
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    } 

    // @CacheEvict(value = "refreshTokens", key = "#token")
    // @Transactional
    // public void deleteRefreshToken(String token) {
    //     refreshTokenRepository.delete(
    //         findByToken(token).orElseThrow(() -> new RuntimeException("Refresh token not found"))
    //     );
    // }

    @CacheEvict(value = "refreshTokens", beforeInvocation = true, key = "#user.id")
    @Transactional
    public void deleteRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }

    @CacheEvict(value = "refreshTokens", beforeInvocation = true, key = "#refreshToken.user.id")
    @Transactional
    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    // private Optional<RefreshToken> findByToken(String token) {
    //     return refreshTokenRepository.findByToken(token);
    // }
    
    
}
