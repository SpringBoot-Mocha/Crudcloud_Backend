package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.GoogleAuthService;
import com.crudzaso.CrudCloud.service.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

/**
 * Implementation of GoogleAuthService for Google OAuth2 authentication.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse authenticateWithGoogle(String idToken) {
        try {
            // Verify the Google ID token
            GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
            
            if (payload == null) {
                throw new AppException("Invalid Google ID token", "INVALID_GOOGLE_TOKEN");
            }

            // Extract user information from token
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            boolean emailVerified = payload.getEmailVerified();

            if (!emailVerified) {
                throw new AppException("Google email not verified", "EMAIL_NOT_VERIFIED");
            }

            log.info("Google authentication for email: {}", email);

            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email, name));

            // Generate JWT token
            String token = jwtService.generateToken(user.getEmail());

            // Build UserResponse
            UserResponse userResponse = UserResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .isOrganization(user.getIsOrganization())
                    .createdAt(user.getCreatedAt())
                    .build();

            log.info("Successfully authenticated user with Google: {}", email);

            return AuthResponse.builder()
                    .token(token)
                    .user(userResponse)
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            log.error("Error verifying Google token", e);
            throw new AppException("Failed to verify Google token", "GOOGLE_TOKEN_VERIFICATION_FAILED");
        }
    }

    /**
     * Verify Google ID token using Google API client.
     *
     * @param idTokenString Google ID token
     * @return GoogleIdToken.Payload with user information
     * @throws GeneralSecurityException if verification fails
     * @throws IOException if network error occurs
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) 
            throws GeneralSecurityException, IOException {
        
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        
        if (idToken != null) {
            return idToken.getPayload();
        }
        
        return null;
    }

    /**
     * Create a new user account from Google authentication.
     *
     * @param email user email from Google
     * @param name user name from Google
     * @return created User entity
     */
    private User createGoogleUser(String email, String name) {
        log.info("Creating new user from Google authentication: {}", email);

        // Generate a random password for Google users (they won't use it)
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        // Parse name into first and last name
        String firstName = "";
        String lastName = "";
        if (name != null && !name.isEmpty()) {
            String[] nameParts = name.trim().split("\\s+", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        } else {
            // Use email username as fallback
            firstName = email.split("@")[0];
            lastName = "";
        }

        User user = User.builder()
                .email(email)
                .name(name != null ? name : email.split("@")[0])
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(encodedPassword)
                .isOrganization(false)
                .build();

        return userRepository.save(user);
    }
}
