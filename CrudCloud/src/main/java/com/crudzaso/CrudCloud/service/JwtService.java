package com.crudzaso.CrudCloud.service;

/**
 * Service interface for JWT token operations
 * Handles JWT token generation, validation, and extraction
 */
public interface JwtService {

    /**
     * Generate a JWT token for a user
     * @param username the username (email) for the token
     * @return the generated JWT token
     */
    String generateToken(String username);

    /**
     * Validate a JWT token
     * @param token the token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Extract username from a JWT token
     * @param token the JWT token
     * @return the username (email) from the token
     */
    String getUsernameFromToken(String token);
}
