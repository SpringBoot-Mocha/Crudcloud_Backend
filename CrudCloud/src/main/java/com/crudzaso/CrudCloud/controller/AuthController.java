package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.AuthenticationService;
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
}
