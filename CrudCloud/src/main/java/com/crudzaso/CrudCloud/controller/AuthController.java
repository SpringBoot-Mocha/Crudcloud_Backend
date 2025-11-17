package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.GitHubCallbackRequest;
import com.crudzaso.CrudCloud.dto.request.GitHubLoginRequest;
import com.crudzaso.CrudCloud.dto.request.GoogleLoginRequest;
import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.AuthenticationService;
import com.crudzaso.CrudCloud.service.GitHubAuthService;
import com.crudzaso.CrudCloud.service.GoogleAuthService;
import com.crudzaso.CrudCloud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints for user login and registration.
 *
 * Provides endpoints for user authentication and account creation.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final GoogleAuthService googleAuthService;
    private final GitHubAuthService gitHubAuthService;

    /**
     * Register a new user account.
     *
     * @param request user registration details
     * @return created user response with 201 status
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with email and password")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Register endpoint called for email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login with email and password.
     *
     * @param request login credentials
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login endpoint called for email: {}", request.getEmail());
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login with Google OAuth2.
     *
     * @param request Google ID token from client
     * @return authentication response with JWT token
     */
    @PostMapping("/google")
    @Operation(summary = "Google OAuth2 login", description = "Authenticates user with Google ID token and returns JWT token")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        log.info("Google login endpoint called");
        AuthResponse response = googleAuthService.authenticateWithGoogle(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Login with GitHub OAuth2.
     *
     * @param request GitHub access token from client
     * @return authentication response with JWT token
     */
    @PostMapping("/github")
    @Operation(summary = "GitHub OAuth2 login", description = "Authenticates user with GitHub access token and returns JWT token")
    public ResponseEntity<AuthResponse> loginWithGitHub(@Valid @RequestBody GitHubLoginRequest request) {
        log.info("GitHub login endpoint called");
        AuthResponse response = gitHubAuthService.authenticateWithGitHub(request.getAccessToken());
        return ResponseEntity.ok(response);
    }
    
    /**
     * GitHub OAuth2 callback - exchanges authorization code for access token and authenticates user.
     *
     * @param request GitHub authorization code from OAuth callback
     * @return authentication response with JWT token
     */
    @PostMapping("/github/callback")
    @Operation(summary = "GitHub OAuth2 callback", description = "Exchanges authorization code for access token and authenticates user")
    public ResponseEntity<AuthResponse> handleGitHubCallback(@Valid @RequestBody GitHubCallbackRequest request) {
        log.info("GitHub callback endpoint called");
        
        // Exchange code for access token
        String accessToken = gitHubAuthService.exchangeCodeForToken(request.getCode());
        
        // Authenticate with the access token
        AuthResponse response = gitHubAuthService.authenticateWithGitHub(accessToken);
        
        return ResponseEntity.ok(response);
    }
}
