package com.crudzaso.CrudCloud.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessException
 * Coverage: Base exception with status codes
 */
class BusinessExceptionTest {

    @Test
    void constructor_WithMessageAndStatusCode_SetsValues() {
        // Given
        String message = "Business logic error";
        int statusCode = 400;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }

    @Test
    void constructor_With400StatusCode() {
        // Given
        String message = "Invalid request";
        int statusCode = 400;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void constructor_With401StatusCode() {
        // Given
        String message = "Unauthorized";
        int statusCode = 401;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void constructor_With403StatusCode() {
        // Given
        String message = "Forbidden";
        int statusCode = 403;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(403, exception.getStatusCode());
    }

    @Test
    void constructor_With404StatusCode() {
        // Given
        String message = "Not found";
        int statusCode = 404;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void constructor_With500StatusCode() {
        // Given
        String message = "Internal server error";
        int statusCode = 500;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void constructorWithCause_WithMessageStatusCodeAndCause_SetsAllValues() {
        // Given
        String message = "Business exception with cause";
        int statusCode = 400;
        RuntimeException cause = new RuntimeException("Root cause");

        // When
        BusinessException exception = new BusinessException(message, statusCode, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructorWithCause_PreservesCauseMessage() {
        // Given
        String causeMessage = "Database connection failed";
        RuntimeException cause = new RuntimeException(causeMessage);
        String message = "Business operation failed";
        int statusCode = 500;

        // When
        BusinessException exception = new BusinessException(message, statusCode, cause);

        // Then
        assertEquals(causeMessage, exception.getCause().getMessage());
    }

    @Test
    void constructorWithCause_PreservesCauseType() {
        // Given
        IllegalStateException cause = new IllegalStateException("Invalid state");
        String message = "State error";
        int statusCode = 400;

        // When
        BusinessException exception = new BusinessException(message, statusCode, cause);

        // Then
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    void isRuntimeException() {
        // Given
        BusinessException exception = new BusinessException("Test", 400);

        // When & Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exceptionCanBeThrown() {
        // Given
        String message = "Test error";
        int statusCode = 400;

        // When & Then
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message, statusCode);
        });
    }

    @Test
    void exceptionCanBeCaughtAsRuntimeException() {
        // Given
        BusinessException businessException = new BusinessException("Test", 400);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            throw businessException;
        });
    }

    @Test
    void multipleInstancesCanHaveDifferentStatusCodes() {
        // Given
        BusinessException exception1 = new BusinessException("Error 1", 400);
        BusinessException exception2 = new BusinessException("Error 2", 401);
        BusinessException exception3 = new BusinessException("Error 3", 403);

        // When & Then
        assertEquals(400, exception1.getStatusCode());
        assertEquals(401, exception2.getStatusCode());
        assertEquals(403, exception3.getStatusCode());
    }

    @Test
    void statusCodeIsAccessible() {
        // Given
        int expectedStatusCode = 422;
        BusinessException exception = new BusinessException("Unprocessable entity", expectedStatusCode);

        // When
        int actualStatusCode = exception.getStatusCode();

        // Then
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    void emptyMessageWithStatusCode() {
        // Given
        String emptyMessage = "";
        int statusCode = 500;

        // When
        BusinessException exception = new BusinessException(emptyMessage, statusCode);

        // Then
        assertEquals(emptyMessage, exception.getMessage());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void nullMessageWithStatusCode() {
        // Given
        String nullMessage = null;
        int statusCode = 500;

        // When
        BusinessException exception = new BusinessException(nullMessage, statusCode);

        // Then
        assertNull(exception.getMessage());
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void messageWithSpecialCharacters() {
        // Given
        String specialMessage = "Error: <>&\"'";
        int statusCode = 400;

        // When
        BusinessException exception = new BusinessException(specialMessage, statusCode);

        // Then
        assertEquals(specialMessage, exception.getMessage());
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void statusCodeZero() {
        // Given
        String message = "No status code";
        int statusCode = 0;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(0, exception.getStatusCode());
    }

    @Test
    void statusCodeNegative() {
        // Given
        String message = "Negative status";
        int statusCode = -1;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(-1, exception.getStatusCode());
    }

    @Test
    void statusCodeLarge() {
        // Given
        String message = "Large status code";
        int statusCode = 599;

        // When
        BusinessException exception = new BusinessException(message, statusCode);

        // Then
        assertEquals(599, exception.getStatusCode());
    }

    @Test
    void exceptionWithCauseAndNullMessage() {
        // Given
        RuntimeException cause = new RuntimeException("Cause");
        String nullMessage = null;
        int statusCode = 500;

        // When
        BusinessException exception = new BusinessException(nullMessage, statusCode, cause);

        // Then
        assertNull(exception.getMessage());
        assertEquals(500, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exceptionWithNestedCause() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root");
        RuntimeException parentCause = new RuntimeException("Parent", rootCause);
        String message = "Business error";
        int statusCode = 500;

        // When
        BusinessException exception = new BusinessException(message, statusCode, parentCause);

        // Then
        assertEquals(parentCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}
