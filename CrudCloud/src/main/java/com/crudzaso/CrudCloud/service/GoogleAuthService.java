package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.response.AuthResponse;

/**
 * Service interface for Google OAuth2 authentication.
 */
public interface GoogleAuthService {
    
    /**
     * Authenticate user with Google ID token.
     *
     * @param idToken Google ID token from client
     * @return authentication response with JWT token
     */
    AuthResponse authenticateWithGoogle(String idToken);
}
