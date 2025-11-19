package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.dto.OAuthUserInfo;
import com.crudzaso.CrudCloud.domain.enums.OAuthProvider;
import com.crudzaso.CrudCloud.service.OAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth Service implementation for validating Google and GitHub tokens.
 * Validates Google JWT tokens and makes calls to GitHub API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthServiceImpl implements OAuthService {

    private final JwtDecoder jwtDecoder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${oauth.github.client-id}")
    private String githubClientId;

    @Value("${oauth.github.client-secret}")
    private String githubClientSecret;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    /**
     * Validates a Google authorization code or JWT token and extracts user information.
     * If a code is provided, exchanges it for an ID token.
     * If a JWT token is provided, validates it directly.
     * Google sends a JWT with claims: sub (ID), email, given_name, family_name
     *
     * @param googleTokenOrCode Google authorization code or JWT token
     * @return OAuthUserInfo with user information
     */
    @Override
    public OAuthUserInfo validateGoogleToken(String googleTokenOrCode) {
        try {
            log.info("Validating Google token/code");

            String idToken = googleTokenOrCode;

            // Check if it's an authorization code (not a JWT token)
            // Authorization codes are alphanumeric strings, not base64-encoded with dots
            // JWT tokens have 3 parts separated by dots (header.payload.signature)
            if (!googleTokenOrCode.contains(".")) {
                log.info("Detected Google authorization code, exchanging for ID token");
                idToken = exchangeGoogleCodeForToken(googleTokenOrCode);
            }

            // Decode and validate the Google ID JWT
            Jwt jwt = jwtDecoder.decode(idToken);

            // Verify that the issuer is Google
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (!issuer.contains("google") && !issuer.contains("accounts.google")) {
                throw new IllegalArgumentException("Invalid Google token issuer");
            }

            // Verify that the audience is our app's client ID
            Object aud = jwt.getClaim("aud");
            String audience = aud != null ? aud.toString() : "";
            if (!audience.equals(googleClientId) && !audience.contains(googleClientId)) {
                log.warn("Audience mismatch. Expected: {}, Got: {}", googleClientId, audience);
                // Don't throw error, sometimes Google includes client ID in audience differently
            }

            // Extract user information from JWT claims
            String providerId = jwt.getSubject(); // 'sub' claim is the Google User ID
            String email = jwt.getClaimAsString("email");
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");

            log.info("Google token validated successfully for user: {}", email);

            return OAuthUserInfo.builder()
                    .providerId(providerId)
                    .email(email)
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .provider(OAuthProvider.GOOGLE)
                    .build();

        } catch (Exception e) {
            log.error("Error validating Google token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid or expired Google token", e);
        }
    }

    /**
     * Validates a GitHub token by making a call to GitHub API.
     * The token can be a personal access token or an authorization code.
     *
     * @param githubToken GitHub token or code
     * @return OAuthUserInfo with user information
     */
    @Override
    public OAuthUserInfo validateGitHubToken(String githubToken) {
        try {
            log.info("Validating GitHub token");

            // If it's an authorization code (not a token), exchange it for a token
            String accessToken = githubToken;
            if (!githubToken.startsWith("gho_") && !githubToken.startsWith("ghp_") && !githubToken.startsWith("ghu_")) {
                accessToken = exchangeGitHubCodeForToken(githubToken);
            }

            // Make request to GitHub API to get user information
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Calling GitHub /user endpoint with access token");
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Failed to fetch GitHub user info");
            }

            // Parse the JSON response
            JsonNode userNode = objectMapper.readTree(response.getBody());

            String providerId = userNode.get("id").asText();
            String login = userNode.get("login").asText();
            String email = userNode.get("email") != null ? userNode.get("email").asText() : null;

            log.info("GitHub /user endpoint returned - id: {}, login: {}, email: {}", providerId, login, email);

            // If email is null (user has private email in GitHub), fetch it from /user/emails endpoint
            if (email == null || email.isEmpty()) {
                log.info("📧 Email is null/empty from /user endpoint, attempting to fetch from /user/emails endpoint for user: {}", login);
                email = fetchGitHubPrimaryEmail(accessToken);
                log.info("📧 Result from /user/emails endpoint: email = {}", email != null ? email : "NULL");
            }

            // If still no email, reject the login - do NOT generate synthetic email
            // Synthetic emails like 'login@github.com' are incorrect when user registered GitHub with Google/other providers
            if (email == null || email.isEmpty()) {
                String errorMsg = "Cannot authenticate: GitHub email is private and not accessible. " +
                        "Please make your email public in GitHub Settings (Settings > Email > Public) " +
                        "or contact support for assistance.";
                log.error("❌ GitHub user {} has no accessible email. Rejecting login with message: {}", login, errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            log.info("✅ GitHub email validation successful for user {}: email = {}", login, email);

            String name = userNode.get("name") != null ? userNode.get("name").asText() : userNode.get("login").asText();

            // Ensure name is not null or empty
            if (name == null || name.trim().isEmpty()) {
                name = login;
                log.warn("GitHub name was empty, using login instead: {}", login);
            }

            // Split name into firstName and lastName
            // Divide the name roughly in half
            String[] nameParts = name.trim().split("\\s+");
            String firstName;
            String lastName;

            if (nameParts.length == 1) {
                // Single name: everything is firstName
                firstName = nameParts[0];
                lastName = "";
            } else if (nameParts.length == 2) {
                // Two names: first is firstName, second is lastName
                firstName = nameParts[0];
                lastName = nameParts[1];
            } else {
                // 3+ names: divide roughly in half
                // For 3 names: midpoint = 2, so firstName gets first 2, lastName gets last 1
                // For 4 names: midpoint = 2, so firstName gets first 2, lastName gets last 2
                int midpoint = (nameParts.length) / 2;
                if (nameParts.length % 2 == 1) {
                    // Odd number: give more to firstName
                    midpoint = (nameParts.length + 1) / 2;
                }
                firstName = String.join(" ", Arrays.copyOf(nameParts, midpoint));
                lastName = String.join(" ", Arrays.copyOfRange(nameParts, midpoint, nameParts.length));
            }

            // Ensure firstName is never empty
            if (firstName == null || firstName.trim().isEmpty()) {
                firstName = login;
                log.warn("GitHub firstName was empty, using login instead: {}", login);
            }

            log.info("GitHub token validated successfully for user: {}", email);

            return OAuthUserInfo.builder()
                    .providerId(providerId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .provider(OAuthProvider.GITHUB)
                    .build();

        } catch (Exception e) {
            log.error("Error validating GitHub token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid or expired GitHub token", e);
        }
    }

    /**
     * Fetches the primary verified email from GitHub's /user/emails endpoint.
     * This is used when the /user endpoint returns null for email (user has private email setting).
     *
     * @param accessToken GitHub access token
     * @return Primary email of the user or null if unable to obtain
     */
    private String fetchGitHubPrimaryEmail(String accessToken) {
        try {
            log.info("Fetching primary email from GitHub /user/emails endpoint");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("GitHub /user/emails response status: {}", response.getStatusCode());
            log.info("GitHub /user/emails response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Failed to fetch GitHub emails, status: {}", response.getStatusCode());
                return null;
            }

            // Parse the emails array
            JsonNode emailsNode = objectMapper.readTree(response.getBody());

            log.info("Parsed emails node, isArray: {}, size: {}", emailsNode.isArray(), emailsNode.isArray() ? emailsNode.size() : "N/A");

            if (emailsNode.isArray()) {
                // Search for primary and verified email
                for (JsonNode emailNode : emailsNode) {
                    boolean isPrimary = emailNode.get("primary") != null && emailNode.get("primary").asBoolean();
                    boolean isVerified = emailNode.get("verified") != null && emailNode.get("verified").asBoolean();
                    String emailValue = emailNode.get("email").asText();

                    log.debug("Email from /user/emails: {}, primary: {}, verified: {}", emailValue, isPrimary, isVerified);

                    if (isPrimary && isVerified) {
                        log.info("Found primary verified email from GitHub: {}", emailValue);
                        return emailValue;
                    }
                }

                // If no primary verified email, use first verified email
                for (JsonNode emailNode : emailsNode) {
                    boolean isVerified = emailNode.get("verified") != null && emailNode.get("verified").asBoolean();
                    if (isVerified) {
                        String emailValue = emailNode.get("email").asText();
                        log.info("Found verified email from GitHub: {}", emailValue);
                        return emailValue;
                    }
                }

                // If no verified email, use first email (even if unverified)
                if (emailsNode.size() > 0) {
                    String emailValue = emailsNode.get(0).get("email").asText();
                    log.warn("Using unverified email from GitHub: {}", emailValue);
                    return emailValue;
                }
            }

            log.warn("No emails found in GitHub /user/emails response");
            return null;

        } catch (Exception e) {
            log.error("Error fetching GitHub primary email: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Exchanges a Google authorization code for an ID token.
     * Only used if the frontend sends a code instead of a token.
     *
     * @param code Google authorization code
     * @return Google ID token (JWT)
     */
    private String exchangeGoogleCodeForToken(String code) {
        try {
            log.info("Exchanging Google code for ID token");
            log.info("Google Client ID: {}", googleClientId);
            log.info("Google Client Secret configured: {}", googleClientSecret != null && !googleClientSecret.isEmpty());
            log.info("Authorization code: {}", code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null");

            // Prepare parameters for code exchange
            Map<String, String> params = new HashMap<>();
            params.put("client_id", googleClientId);
            params.put("client_secret", googleClientSecret);
            params.put("code", code);
            params.put("grant_type", "authorization_code");
            params.put("redirect_uri", "http://localhost:3000/auth/google/callback");

            log.info("Request parameters: client_id={}, client_secret={}, code={}, grant_type={}, redirect_uri={}",
                    googleClientId,
                    googleClientSecret != null ? "***" : "null",
                    code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null",
                    "authorization_code",
                    "http://localhost:3000/auth/google/callback");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            // Convert map to form-encoded string
            StringBuilder formBody = new StringBuilder();
            params.forEach((key, value) -> {
                if (formBody.length() > 0) formBody.append("&");
                formBody.append(key).append("=").append(value);
            });

            HttpEntity<String> entity = new HttpEntity<>(formBody.toString(), headers);

            log.info("Sending POST request to https://oauth2.googleapis.com/token");
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://oauth2.googleapis.com/token",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Google OAuth token endpoint response status: {}", response.getStatusCode());
            log.info("Google OAuth token endpoint response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to exchange Google code for token. Status: {}", response.getStatusCode());
                log.error("Response body: {}", response.getBody());
                throw new IllegalArgumentException("Failed to exchange Google code for token. Status: " + response.getStatusCode());
            }

            // Parse the JSON response
            JsonNode responseNode = objectMapper.readTree(response.getBody());

            if (responseNode.has("error")) {
                String error = responseNode.get("error").asText();
                String errorDescription = responseNode.has("error_description") ? responseNode.get("error_description").asText() : "";
                String errorUri = responseNode.has("error_uri") ? responseNode.get("error_uri").asText() : "";
                log.error("Google OAuth error: {} - {} (URI: {})", error, errorDescription, errorUri);
                throw new IllegalArgumentException("Google OAuth error: " + error + " - " + errorDescription);
            }

            String idToken = responseNode.get("id_token").asText();
            log.info("Google code exchanged successfully for ID token");

            return idToken;

        } catch (Exception e) {
            log.error("Error exchanging Google code for token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to exchange Google code for token", e);
        }
    }

    /**
     * Exchanges a GitHub authorization code for an access token.
     * Only used if the frontend sends a code instead of a token.
     *
     * @param code GitHub authorization code
     * @return GitHub access token
     */
    private String exchangeGitHubCodeForToken(String code) {
        try {
            log.info("Exchanging GitHub code for access token");
            log.info("GitHub Client ID: {}", githubClientId);
            log.info("GitHub Client Secret configured: {}", githubClientSecret != null && !githubClientSecret.isEmpty());
            log.info("Authorization code: {}", code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null");

            // Prepare parameters for code exchange
            Map<String, String> params = new HashMap<>();
            params.put("client_id", githubClientId);
            params.put("client_secret", githubClientSecret);
            params.put("code", code);
            params.put("redirect_uri", "http://localhost:3000/auth/github/callback");

            log.info("Request parameters: client_id={}, client_secret={}, code={}, redirect_uri={}",
                    githubClientId,
                    githubClientSecret != null ? "***" : "null",
                    code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null",
                    "http://localhost:3000/auth/github/callback");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            // Convert map to form-encoded string
            StringBuilder formBody = new StringBuilder();
            params.forEach((key, value) -> {
                if (formBody.length() > 0) formBody.append("&");
                formBody.append(key).append("=").append(value);
            });

            HttpEntity<String> entity = new HttpEntity<>(formBody.toString(), headers);

            log.info("Sending POST request to https://github.com/login/oauth/access_token");
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://github.com/login/oauth/access_token",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("GitHub OAuth token endpoint response status: {}", response.getStatusCode());
            log.info("GitHub OAuth token endpoint response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to exchange GitHub code for token. Status: {}", response.getStatusCode());
                log.error("Response body: {}", response.getBody());
                throw new IllegalArgumentException("Failed to exchange GitHub code for token. Status: " + response.getStatusCode());
            }

            // Parse the JSON response
            JsonNode responseNode = objectMapper.readTree(response.getBody());

            if (responseNode.has("error")) {
                String error = responseNode.get("error").asText();
                String errorDescription = responseNode.has("error_description") ? responseNode.get("error_description").asText() : "";
                String errorUri = responseNode.has("error_uri") ? responseNode.get("error_uri").asText() : "";
                log.error("GitHub API error: {} - {} (URI: {})", error, errorDescription, errorUri);
                throw new IllegalArgumentException("GitHub OAuth error: " + error + " - " + errorDescription);
            }

            String accessToken = responseNode.get("access_token").asText();
            log.info("GitHub code exchanged successfully for access token");

            return accessToken;

        } catch (Exception e) {
            log.error("Error exchanging GitHub code for token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to exchange GitHub code for token", e);
        }
    }
}
