package com.crudzaso.CrudCloud.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtServiceImpl
 * Coverage: Token generation, validation, and username extraction
 */
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "mySecretKeyForJWTTokenGenerationThatNeedsToBeAtLeast256BitsLongForHS512Algorithm");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L); // 24 hours
    }

    @Test
    void generateToken_Success() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void generateToken_DifferentUsers_GenerateDifferentTokens() {
        // Given
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        // When
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String username = "test@example.com";
        String token = jwtService.generateToken(username);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        // Given
        String malformedToken = "this-is-not-a-jwt";

        // When
        boolean isValid = jwtService.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtService.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        // When
        boolean isValid = jwtService.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        // Given
        String expectedUsername = "test@example.com";
        String token = jwtService.generateToken(expectedUsername);

        // When
        String actualUsername = jwtService.getUsernameFromToken(token);

        // Then
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void getUsernameFromToken_InvalidToken_ReturnsNull() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        String username = jwtService.getUsernameFromToken(invalidToken);

        // Then
        assertNull(username);
    }

    @Test
    void getUsernameFromToken_MalformedToken_ReturnsNull() {
        // Given
        String malformedToken = "not-a-valid-jwt";

        // When
        String username = jwtService.getUsernameFromToken(malformedToken);

        // Then
        assertNull(username);
    }

    @Test
    void getUsernameFromToken_EmptyToken_ReturnsNull() {
        // Given
        String emptyToken = "";

        // When
        String username = jwtService.getUsernameFromToken(emptyToken);

        // Then
        assertNull(username);
    }

    @Test
    void getUsernameFromToken_NullToken_ReturnsNull() {
        // When
        String username = jwtService.getUsernameFromToken(null);

        // Then
        assertNull(username);
    }

    @Test
    void tokenLifecycle_GenerateValidateExtract() {
        // Given
        String expectedUsername = "user@example.com";

        // When - Generate token
        String token = jwtService.generateToken(expectedUsername);

        // Then - Validate token
        assertTrue(jwtService.validateToken(token));

        // And - Extract username
        String actualUsername = jwtService.getUsernameFromToken(token);
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void generateToken_ConsistentFormat() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtService.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // JWT tokens start with "eyJ" when base64 encoded
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }
}
