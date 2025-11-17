package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.response.AuthResponse;

/**
 * Service interface for GitHub OAuth2 authentication.
 */
public interface GitHubAuthService {
    
    /**
     * Authenticate user with GitHub access token.
     *
     * @param accessToken GitHub access token from client
     * @return authentication response with JWT token
     */
    AuthResponse authenticateWithGitHub(String accessToken);
    
    /**
     * Exchange GitHub authorization code for access token.
     *
     * @param code GitHub authorization code from OAuth callback
     * @return GitHub access token
     */
    String exchangeCodeForToken(String code);
}
