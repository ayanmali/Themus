package com.delphi.delphi.dtos.cache;

import java.time.Instant;

import com.delphi.delphi.entities.RefreshToken;

public class RefreshTokenCacheDto {
    private Long id;
    private String token;
    private Long userId;
    private Instant expiryDate;
    private boolean used;

    public RefreshTokenCacheDto() {
    }

    public RefreshTokenCacheDto(RefreshToken refreshToken) {
        this.id = refreshToken.getId();
        this.token = refreshToken.getToken();
        this.userId = refreshToken.getUser().getId();
        this.expiryDate = refreshToken.getExpiryDate();
        this.used = refreshToken.isUsed();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    
    
}
