package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.OAuthUserInfo;
import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.exception.UnauthorizedException;

/**
 * Service interface for authentication operations
 * Handles user login, OAuth authentication, and JWT token generation
 */
public interface AuthenticationService {

    /**
     * Authenticate a user and generate JWT token
     * @param request the login request with email and password
     * @return the authentication response with token and user info
     * @throws UnauthorizedException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);

    /**
     * Authenticate a user using OAuth provider and generate JWT token
     * Creates a new user if they don't exist yet
     *
     * @param oauthUserInfo user information from OAuth provider
     * @return the authentication response with token and user info
     */
    AuthResponse loginWithOAuth(OAuthUserInfo oauthUserInfo);
}
