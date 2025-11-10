package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.exception.UnauthorizedException;

/**
 * Service interface for authentication operations
 * Handles user login and JWT token generation
 */
public interface AuthenticationService {

    /**
     * Authenticate a user and generate JWT token
     * @param request the login request with email and password
     * @return the authentication response with token and user info
     * @throws UnauthorizedException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);
}
