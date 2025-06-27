package com.delphi.delphi.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.delphi.delphi.entities.RefreshToken;
import com.delphi.delphi.entities.User;
import com.delphi.delphi.repositories.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Long refreshTokenExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, @Value("${jwt.refresh.expiration}") Long refreshTokenExpiration, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.jwtService = jwtService;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtService.generateRefreshToken(user));
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpiration));
        return refreshTokenRepository.save(refreshToken);
    }

    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    } 

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.delete(
            findByToken(token).orElseThrow(() -> new RuntimeException("Refresh token not found"))
        );
    }

    private Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    
}
