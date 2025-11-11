package com.crudzaso.CrudCloud.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvalidCredentialsException
 * Coverage: Invalid credentials error handling
 */
class InvalidCredentialsExceptionTest {

    @Test
    void constructor_WithMessage_SetsMessageAndStatusCode() {
        // Given
        String message = "Invalid username or password";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.getStatusCode());
    }

    @Test
    void constructor_WithMessage_StatusCodeIs401() {
        // Given
        String message = "Authentication failed";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        // Then
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void extendsBusinessException_ReturnsCorrectStatusCode() {
        // Given
        String message = "Wrong credentials";
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        // When & Then
        assertTrue(exception instanceof BusinessException);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.getStatusCode());
    }

    @Test
    void constructor_WithEmptyMessage_StillSetsStatusCode() {
        // Given
        String emptyMessage = "";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(emptyMessage);

        // Then
        assertEquals(emptyMessage, exception.getMessage());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void constructor_WithNullMessage_StillSetsStatusCode() {
        // Given
        String nullMessage = null;

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(nullMessage);

        // Then
        assertNull(exception.getMessage());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void statusCodeAlwaysUnauthorized_RegardlessOfMessage() {
        // Test with various messages
        String[] messages = {
            "Invalid credentials",
            "User not found",
            "Locked account",
            "Disabled account",
            "Wrong password",
            "Session expired",
            "Token invalid"
        };

        for (String message : messages) {
            InvalidCredentialsException exception = new InvalidCredentialsException(message);
            assertEquals(401, exception.getStatusCode(), "Status code should be 401 for: " + message);
        }
    }

    @Test
    void isRuntimeException() {
        // Given
        InvalidCredentialsException exception = new InvalidCredentialsException("Test");

        // When & Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exceptionCanBeCaught() {
        // Given
        String message = "Invalid credentials";

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException(message);
        });
    }

    @Test
    void exceptionCanBeCaughtAsBusinessException() {
        // Given
        String message = "Invalid credentials";

        // When & Then
        assertThrows(BusinessException.class, () -> {
            throw new InvalidCredentialsException(message);
        });
    }

    @Test
    void exceptionCanBeCaughtAsRuntimeException() {
        // Given
        String message = "Invalid credentials";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            throw new InvalidCredentialsException(message);
        });
    }

    @Test
    void messagePreservedInException() {
        // Given
        String detailedMessage = "Invalid credentials provided for user: admin@example.com";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(detailedMessage);

        // Then
        assertEquals(detailedMessage, exception.getMessage());
    }

    @Test
    void multipleInstancesHaveSameStatusCode() {
        // Given
        InvalidCredentialsException exception1 = new InvalidCredentialsException("Error 1");
        InvalidCredentialsException exception2 = new InvalidCredentialsException("Error 2");
        InvalidCredentialsException exception3 = new InvalidCredentialsException("Error 3");

        // When & Then
        assertEquals(exception1.getStatusCode(), exception2.getStatusCode());
        assertEquals(exception2.getStatusCode(), exception3.getStatusCode());
        assertEquals(401, exception1.getStatusCode());
    }

    @Test
    void exceptionWithSpecialCharactersInMessage() {
        // Given
        String messageWithSpecialChars = "Invalid credentials: <script>alert('xss')</script>";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(messageWithSpecialChars);

        // Then
        assertEquals(messageWithSpecialChars, exception.getMessage());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void exceptionWithMultilineMessage() {
        // Given
        String multilineMessage = "Invalid credentials\nPlease check username and password\nTry again";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(multilineMessage);

        // Then
        assertEquals(multilineMessage, exception.getMessage());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void statusCodeMatchesHttpStatusUnauthorized() {
        // Given
        InvalidCredentialsException exception = new InvalidCredentialsException("Test");

        // When & Then
        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception.getStatusCode());
    }

    @Test
    void thrownExceptionCanBeRethrownWithoutLossingStatusCode() {
        // Given
        InvalidCredentialsException originalException = new InvalidCredentialsException("Original");

        // When & Then
        try {
            throw originalException;
        } catch (InvalidCredentialsException e) {
            assertEquals(401, e.getStatusCode());
            assertEquals("Original", e.getMessage());
        }
    }
}
