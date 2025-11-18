package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.OAuthUserInfo;
import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.GitHubOAuthRequest;
import com.crudzaso.CrudCloud.dto.request.GoogleOAuthRequest;
import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.AuthenticationService;
import com.crudzaso.CrudCloud.service.JwtService;
import com.crudzaso.CrudCloud.service.OAuthService;
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
    private final JwtService jwtService;
    private final UserService userService;
    private final OAuthService oauthService;

    /**
     * Register a new user account.
     *
     * @param request user registration details
     * @return authentication response with JWT token and user data
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with email and password. Returns JWT token for immediate login.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Register endpoint called for email: {}", request.getEmail());

        // Create the user
        UserResponse userResponse = userService.createUser(request);

        // Generate JWT token for automatic login after registration
        String token = jwtService.generateToken(userResponse.getEmail());

        // Return AuthResponse with token and user data
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();

        log.info("✅ User registered successfully with ID: {} and auto-login token generated", userResponse.getUserId());
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
     * Login/Register with Google OAuth.
     *
     * @param request Google OAuth token
     * @return authentication response with JWT token
     */
    @PostMapping("/google")
    @Operation(
            summary = "Google OAuth login/register",
            description = "Authenticates or registers user using Google OAuth token. " +
                    "Creates new user account if email doesn't exist."
    )
    public ResponseEntity<AuthResponse> googleOAuth(@Valid @RequestBody GoogleOAuthRequest request) {
        log.info("🔐 Google OAuth endpoint called");
        try {
            // Validate Google token and extract user info
            OAuthUserInfo googleUserInfo = oauthService.validateGoogleToken(request.getToken());
            log.info("✅ Google token validated for email: {}", googleUserInfo.getEmail());

            // Authenticate or create user
            AuthResponse response = authenticationService.loginWithOAuth(googleUserInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Google OAuth error: {}", e.getMessage());
            throw new IllegalArgumentException("Google OAuth authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Login/Register with GitHub OAuth.
     *
     * @param request GitHub OAuth token
     * @return authentication response with JWT token
     */
    @PostMapping("/github")
    @Operation(
            summary = "GitHub OAuth login/register",
            description = "Authenticates or registers user using GitHub OAuth token. " +
                    "Creates new user account if email doesn't exist."
    )
    public ResponseEntity<AuthResponse> githubOAuth(@Valid @RequestBody GitHubOAuthRequest request) {
        log.info("🔐 GitHub OAuth endpoint called");
        try {
            // Validate GitHub token and extract user info
            OAuthUserInfo githubUserInfo = oauthService.validateGitHubToken(request.getToken());
            log.info("✅ GitHub token validated for email: {}", githubUserInfo.getEmail());

            // Authenticate or create user
            AuthResponse response = authenticationService.loginWithOAuth(githubUserInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ GitHub OAuth error: {}", e.getMessage());
            throw new IllegalArgumentException("GitHub OAuth authentication failed: " + e.getMessage(), e);
        }
    }
}
