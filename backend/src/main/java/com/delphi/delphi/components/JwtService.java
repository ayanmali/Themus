package com.delphi.delphi.components;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String jwtSecretKey;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;

    public JwtService(@Value("${jwt.secret}") String jwtSecretKey, 
                      @Value("${jwt.access.expiration}") Long accessTokenExpiration,
                      @Value("${jwt.refresh.expiration}") Long refreshTokenExpiration) {
        this.jwtSecretKey = jwtSecretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    // public String generateAccessToken(String id) {
    //     Map<String, Object> claims = new HashMap<>();
    //     return createToken(claims, id, accessTokenExpiration);
    // }

    // public String generateAccessToken(User user) {
    //     Map<String, Object> claims = new HashMap<>();
    //     return createToken(claims, user.getId().toString(), accessTokenExpiration);
    // }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", true);
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }
    
    // public String generateRefreshToken(String id) {
    //     Map<String, Object> claims = new HashMap<>();
    //     claims.put("refresh", true);
    //     return createToken(claims, id, refreshTokenExpiration);
    // }

    // public String generateRefreshToken(User user) {
    //     Map<String, Object> claims = new HashMap<>();
    //     claims.put("refresh", true);
    //     return createToken(claims, user.getEmail(), refreshTokenExpiration);
    // }
    
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes()))
            .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
