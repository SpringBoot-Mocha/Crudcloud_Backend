package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.GitHubAuthService;
import com.crudzaso.CrudCloud.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of GitHub OAuth2 authentication service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubAuthServiceImpl implements GitHubAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    @Override
    @Transactional
    public AuthResponse authenticateWithGitHub(String accessToken) {
        try {
            log.debug("Authenticating user with GitHub access token");
            
            // Verify GitHub token and get user info
            GHUser ghUser = verifyGitHubToken(accessToken);
            
            // Get user email (primary email)
            final String email;
            if (ghUser.getEmail() == null || ghUser.getEmail().isEmpty()) {
                // If email is not public, try to get from emails list
                email = ghUser.getLogin() + "@github.local";
                log.warn("GitHub user {} has no public email, using: {}", ghUser.getLogin(), email);
            } else {
                email = ghUser.getEmail();
            }
            
            final String name = ghUser.getName() != null ? ghUser.getName() : ghUser.getLogin();
            
            log.debug("GitHub user verified: {} ({})", name, email);
            
            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGitHubUser(email, name));
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(user.getEmail());
            
            // Build response
            UserResponse userResponse = UserResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .isOrganization(user.getIsOrganization())
                    .createdAt(user.getCreatedAt())
                    .build();
            
            return AuthResponse.builder()
                    .token(jwtToken)
                    .user(userResponse)
                    .build();
            
        } catch (IOException e) {
            log.error("GitHub token verification failed", e);
            throw new AppException("Failed to verify GitHub token: " + e.getMessage(), 
                    "GITHUB_TOKEN_VERIFICATION_FAILED");
        } catch (Exception e) {
            log.error("GitHub authentication failed", e);
            throw new AppException("GitHub authentication failed: " + e.getMessage(), 
                    "GITHUB_AUTH_FAILED");
        }
    }

    /**
     * Verify GitHub access token and retrieve user information.
     */
    private GHUser verifyGitHubToken(String accessToken) throws IOException {
        GitHub github = new GitHubBuilder()
                .withOAuthToken(accessToken)
                .build();
        
        // Get authenticated user
        GHUser user = github.getMyself();
        
        if (user == null) {
            throw new IOException("Invalid GitHub token");
        }
        
        return user;
    }

    /**
     * Exchange authorization code for access token.
     */
    public String exchangeCodeForToken(String code) {
        try {
            log.debug("Exchanging GitHub authorization code for access token");
            
            // Create request body
            Map<String, String> requestBody = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "code", code
            );
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://github.com/login/oauth/access_token"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            // Send request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.error("GitHub token exchange failed with status: {}", response.statusCode());
                throw new AppException("Failed to exchange code for token", "GITHUB_TOKEN_EXCHANGE_FAILED");
            }
            
            // Parse response
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(response.body(), Map.class);
            
            if (responseData.containsKey("error")) {
                String error = (String) responseData.get("error");
                String errorDescription = (String) responseData.getOrDefault("error_description", "Unknown error");
                log.error("GitHub OAuth error: {} - {}", error, errorDescription);
                throw new AppException("GitHub OAuth error: " + errorDescription, "GITHUB_OAUTH_ERROR");
            }
            
            String accessToken = (String) responseData.get("access_token");
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("No access token received from GitHub");
                throw new AppException("No access token received from GitHub", "GITHUB_NO_ACCESS_TOKEN");
            }
            
            log.debug("Successfully exchanged code for access token");
            return accessToken;
            
        } catch (IOException e) {
            log.error("Failed to exchange code for token: {}", e.getMessage());
            throw new AppException("Failed to exchange code for token: " + e.getMessage(), "GITHUB_TOKEN_EXCHANGE_FAILED");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted: {}", e.getMessage());
            throw new AppException("Request interrupted: " + e.getMessage(), "GITHUB_REQUEST_INTERRUPTED");
        }
    }

    /**
     * Create a new user from GitHub authentication.
     */
    private User createGitHubUser(String email, String name) {
        log.debug("Creating new user from GitHub: {} ({})", name, email);
        
        User user = User.builder()
                .email(email)
                .name(name)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .isOrganization(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        return userRepository.save(user);
    }
}
