package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Implementation of JwtService
 * Handles JWT token generation, validation, and extraction
 * Compatible with jjwt 0.12.3
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")  // Default 24 hours in milliseconds
    private long jwtExpirationMs;

    /**
     * Get the signing key from the JWT secret
     * @return SecretKey for HMAC-SHA512 signing
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateToken(String username) {
        log.debug("Generating JWT token for username: {}", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();

        log.debug("JWT token generated successfully for username: {}", username);
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .verifyingKey(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .verifyingKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from JWT token: {}", e.getMessage());
            return null;
        }
    }
}
