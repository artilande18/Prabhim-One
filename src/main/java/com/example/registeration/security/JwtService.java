package com.example.registeration.security;

import com.example.registeration.entity.User;
import com.example.registeration.repository.SessionRepository;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final Key key;
    private final SessionRepository sessionRepository;

    private final long accessTokenValidity = 15 * 60 * 1000; // 15 minutes
    private final long refreshTokenValidity = 7 * 24 * 60 * 60 * 1000; // 7 days

    public JwtService(@Value("${jwt.secret}") String secret, SessionRepository sessionRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.sessionRepository = sessionRepository;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", "refresh")
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public String generateAccessTokenFromRefresh(String refreshToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(refreshToken).getBody();
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Optional: rotate refresh token
    public String rotateRefreshToken(String oldRefreshToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(oldRefreshToken).getBody();
        UUID userId = UUID.fromString(claims.getSubject());
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Optional: invalidate refresh token (if you store them in DB)
    public void invalidateRefreshToken(String refreshToken) {
        sessionRepository.deleteByRefreshToken(refreshToken);

    }

    public UUID extractUserId(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
        .parseClaimsJws(token).getBody();
    String subject = claims.getSubject();
    try {
        return UUID.fromString(subject);
    } catch (IllegalArgumentException e) {
        throw new RuntimeException("Token subject is not a valid UUID: " + subject);
    }
}
}
