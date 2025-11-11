package com.crudzaso.CrudCloud.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AppException
 * Coverage: Application exception with error codes
 */
class AppExceptionTest {

    @Test
    void constructor_WithMessageAndCode_SetsValues() {
        // Given
        String message = "Application error";
        String code = "APP_ERROR";

        // When
        AppException exception = new AppException(message, code);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
    }

    @Test
    void getCode_ReturnsCorrectCode() {
        // Given
        String expectedCode = "USER_NOT_FOUND";
        AppException exception = new AppException("User not found", expectedCode);

        // When
        String actualCode = exception.getCode();

        // Then
        assertEquals(expectedCode, actualCode);
    }

    @Test
    void constructor_WithMessageAndCode_DifferentCodes() {
        // Test various error codes
        AppException userNotFound = new AppException("User not found", "USER_NOT_FOUND");
        AppException invalidToken = new AppException("Invalid token", "INVALID_TOKEN");
        AppException forbidden = new AppException("Access denied", "FORBIDDEN");

        assertEquals("USER_NOT_FOUND", userNotFound.getCode());
        assertEquals("INVALID_TOKEN", invalidToken.getCode());
        assertEquals("FORBIDDEN", forbidden.getCode());
    }

    @Test
    void isRuntimeException() {
        // Given
        AppException exception = new AppException("Test", "TEST_CODE");

        // When & Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exceptionCanBeThrown() {
        // Given
        String message = "Test error";
        String code = "TEST_CODE";

        // When & Then
        assertThrows(AppException.class, () -> {
            throw new AppException(message, code);
        });
    }

    @Test
    void exceptionCanBeCaughtAsRuntimeException() {
        // Given
        AppException appException = new AppException("Test", "CODE");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            throw appException;
        });
    }

    @Test
    void messageAndCodeAreIndependent() {
        // Given
        String message = "Database error";
        String code = "DB_ERROR";

        // When
        AppException exception = new AppException(message, code);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
        assertNotEquals(message, code);
    }

    @Test
    void codeCanBeEmpty() {
        // Given
        String message = "Error with empty code";
        String emptyCode = "";

        // When
        AppException exception = new AppException(message, emptyCode);

        // Then
        assertEquals(emptyCode, exception.getCode());
    }

    @Test
    void codeCanBeNull() {
        // Given
        String message = "Error with null code";
        String nullCode = null;

        // When
        AppException exception = new AppException(message, nullCode);

        // Then
        assertNull(exception.getCode());
    }

    @Test
    void messageCanBeEmpty() {
        // Given
        String emptyMessage = "";
        String code = "ERROR_CODE";

        // When
        AppException exception = new AppException(emptyMessage, code);

        // Then
        assertEquals(emptyMessage, exception.getMessage());
        assertEquals(code, exception.getCode());
    }

    @Test
    void messageCanBeNull() {
        // Given
        String nullMessage = null;
        String code = "ERROR_CODE";

        // When
        AppException exception = new AppException(nullMessage, code);

        // Then
        assertNull(exception.getMessage());
        assertEquals(code, exception.getCode());
    }

    @Test
    void codeWithSpecialCharacters() {
        // Given
        String message = "Error";
        String codeWithSpecialChars = "ERROR_CODE_123-456_ABC";

        // When
        AppException exception = new AppException(message, codeWithSpecialChars);

        // Then
        assertEquals(codeWithSpecialChars, exception.getCode());
    }

    @Test
    void multipleInstancesWithSameCode() {
        // Given
        String code = "DUPLICATE_CODE";
        AppException exception1 = new AppException("First error", code);
        AppException exception2 = new AppException("Second error", code);

        // When & Then
        assertEquals(code, exception1.getCode());
        assertEquals(code, exception2.getCode());
        assertEquals(exception1.getCode(), exception2.getCode());
    }

    @Test
    void multipleInstancesWithDifferentCodes() {
        // Given
        AppException exception1 = new AppException("Error 1", "CODE_1");
        AppException exception2 = new AppException("Error 2", "CODE_2");
        AppException exception3 = new AppException("Error 3", "CODE_3");

        // When & Then
        assertEquals("CODE_1", exception1.getCode());
        assertEquals("CODE_2", exception2.getCode());
        assertEquals("CODE_3", exception3.getCode());
        assertNotEquals(exception1.getCode(), exception2.getCode());
    }

    @Test
    void codeIsAccessibleAfterConstruction() {
        // Given
        String expectedCode = "ACCESSIBLE_CODE";
        AppException exception = new AppException("Test message", expectedCode);

        // When
        String retrievedCode = exception.getCode();

        // Then
        assertEquals(expectedCode, retrievedCode);
    }

    @Test
    void messageIsAccessibleAfterConstruction() {
        // Given
        String expectedMessage = "Test message";
        AppException exception = new AppException(expectedMessage, "CODE");

        // When
        String retrievedMessage = exception.getMessage();

        // Then
        assertEquals(expectedMessage, retrievedMessage);
    }

    @Test
    void exceptionWithLongMessage() {
        // Given
        String longMessage = "This is a very long error message that contains detailed information about what went wrong in the application. " +
                           "It provides context and helps developers understand the issue quickly.";
        String code = "LONG_MESSAGE_ERROR";

        // When
        AppException exception = new AppException(longMessage, code);

        // Then
        assertEquals(longMessage, exception.getMessage());
        assertEquals(code, exception.getCode());
    }

    @Test
    void exceptionWithLongCode() {
        // Given
        String message = "Error";
        String longCode = "VERY_LONG_ERROR_CODE_WITH_MULTIPLE_COMPONENTS_AND_DETAILED_INFORMATION";

        // When
        AppException exception = new AppException(message, longCode);

        // Then
        assertEquals(longCode, exception.getCode());
    }

    @Test
    void codeWithUppercaseLetters() {
        // Given
        String message = "Error";
        String uppercaseCode = "UPPERCASE_CODE";

        // When
        AppException exception = new AppException(message, uppercaseCode);

        // Then
        assertEquals(uppercaseCode, exception.getCode());
    }

    @Test
    void codeWithNumbers() {
        // Given
        String message = "Error";
        String codeWithNumbers = "ERROR_CODE_123";

        // When
        AppException exception = new AppException(message, codeWithNumbers);

        // Then
        assertEquals(codeWithNumbers, exception.getCode());
    }

    @Test
    void codeWithUnderscores() {
        // Given
        String message = "Error";
        String codeWithUnderscores = "ERROR_CODE_NAME_HERE";

        // When
        AppException exception = new AppException(message, codeWithUnderscores);

        // Then
        assertEquals(codeWithUnderscores, exception.getCode());
    }

    @Test
    void messageWithNewlines() {
        // Given
        String messageWithNewlines = "Error line 1\nError line 2\nError line 3";
        String code = "MULTILINE_ERROR";

        // When
        AppException exception = new AppException(messageWithNewlines, code);

        // Then
        assertEquals(messageWithNewlines, exception.getMessage());
        assertTrue(exception.getMessage().contains("\n"));
    }

    @Test
    void messageWithSpecialCharacters() {
        // Given
        String messageWithSpecialChars = "Error: <>&\"'`~!@#$%^&*()";
        String code = "SPECIAL_CHAR_ERROR";

        // When
        AppException exception = new AppException(messageWithSpecialChars, code);

        // Then
        assertEquals(messageWithSpecialChars, exception.getMessage());
    }

    @Test
    void codeConsistencyAcrossMultipleCalls() {
        // Given
        String code = "CONSISTENT_CODE";
        AppException exception = new AppException("Message", code);

        // When
        String code1 = exception.getCode();
        String code2 = exception.getCode();
        String code3 = exception.getCode();

        // Then
        assertEquals(code1, code2);
        assertEquals(code2, code3);
        assertEquals(code, code1);
    }

    @Test
    void canBeCaughtAndCodeCanBeRetrieved() {
        // Given
        String expectedCode = "CAUGHT_CODE";

        // When & Then
        try {
            throw new AppException("Error message", expectedCode);
        } catch (AppException e) {
            assertEquals(expectedCode, e.getCode());
            assertEquals("Error message", e.getMessage());
        }
    }
}
