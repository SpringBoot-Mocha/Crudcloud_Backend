package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.OAuthUserInfo;
import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.UnauthorizedException;
import com.crudzaso.CrudCloud.mapper.UserMapper;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.AuthenticationService;
import com.crudzaso.CrudCloud.service.JwtService;
import com.crudzaso.CrudCloud.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of AuthenticationService
 * Handles user authentication, OAuth authentication, and JWT token generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login attempt with non-existent email: {}", request.getEmail());
                    return new UnauthorizedException("Invalid credentials");
                });

        // Validate password
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {} - invalid password", request.getEmail());
            throw new UnauthorizedException("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());
        log.info("Login successful for email: {}", request.getEmail());

        // Build response
        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse loginWithOAuth(OAuthUserInfo oauthUserInfo) {
        log.info("🔐 Attempting OAuth login with {} for email: {}", oauthUserInfo.getProvider(), oauthUserInfo.getEmail());

        // ✅ CRITICAL VALIDATION: Email must never be null (safety net)
        if (oauthUserInfo.getEmail() == null || oauthUserInfo.getEmail().isEmpty()) {
            String errorMsg = "Cannot authenticate: Email is missing from OAuth provider response. " +
                    "Please ensure your email is public and accessible in your " +
                    oauthUserInfo.getProvider() + " settings.";
            log.error("❌ CRITICAL: OAuth user info has null/empty email! Provider: {}, email: {}",
                    oauthUserInfo.getProvider(), oauthUserInfo.getEmail());
            throw new IllegalArgumentException(errorMsg);
        }

        // Try to find existing user by OAuth provider ID
        Optional<User> existingUserByOAuth = userRepository.findByOauthProviderId(oauthUserInfo.getProviderId());

        User user;
        if (existingUserByOAuth.isPresent()) {
            // User already exists with this OAuth provider
            user = existingUserByOAuth.get();
            log.info("✅ OAuth user found by provider ID");
        } else {
            // Try to find by email
            Optional<User> existingUserByEmail = userRepository.findByEmail(oauthUserInfo.getEmail());

            if (existingUserByEmail.isPresent()) {
                // User exists with this email, link their OAuth account
                user = existingUserByEmail.get();
                log.info("ℹ️ Linking OAuth account to existing email user");
                user.setOauthProvider(oauthUserInfo.getProvider());
                user.setOauthProviderId(oauthUserInfo.getProviderId());
                user = userRepository.save(user);
            } else {
                // Create new user from OAuth info
                log.info("📝 Creating new user from OAuth info");
                user = User.builder()
                        .email(oauthUserInfo.getEmail())
                        .firstName(oauthUserInfo.getFirstName())
                        .lastName(oauthUserInfo.getLastName())
                        .oauthProvider(oauthUserInfo.getProvider())
                        .oauthProviderId(oauthUserInfo.getProviderId())
                        .passwordHash(null) // OAuth users don't have passwords
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                user = userRepository.save(user);

                // Create free subscription for new OAuth user
                try {
                    userService.createFreeSubscriptionForUser(user.getId());
                    log.info("✅ Free subscription created for new OAuth user");
                } catch (Exception e) {
                    log.warn("⚠️ Failed to create subscription for OAuth user: {}", e.getMessage());
                    // Don't fail the login if subscription creation fails
                }
            }
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());
        log.info("✅ OAuth login successful for email: {}", user.getEmail());

        // Build response
        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }
}
