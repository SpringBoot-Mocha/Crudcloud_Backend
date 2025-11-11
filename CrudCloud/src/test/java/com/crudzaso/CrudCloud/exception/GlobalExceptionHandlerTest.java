package com.crudzaso.CrudCloud.exception;

import com.crudzaso.CrudCloud.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 * Coverage: All exception handlers and error response generation
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getServletPath()).thenReturn("/api/test");
    }

    // ==================== BusinessException Handler Tests ====================

    @Test
    void handleBusinessException_ResourceNotFound_Returns404() {
        // Given
        String message = "User not found";
        BusinessException exception = new ResourceNotFoundException(message);
        when(request.getServletPath()).thenReturn("/api/users/999");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals(message, response.getBody().getMessage());
        assertEquals("/api/users/999", response.getBody().getPath());
    }

    @Test
    void handleBusinessException_InvalidCredentials_Returns401() {
        // Given
        String message = "Invalid username or password";
        BusinessException exception = new InvalidCredentialsException(message);
        when(request.getServletPath()).thenReturn("/api/login");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_IncludesTimestamp() {
        // Given
        BusinessException exception = new ResourceNotFoundException("Not found");
        long beforeTime = System.currentTimeMillis();
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(exception, request);
        long afterTime = System.currentTimeMillis();

        // Then
        assertNotNull(response.getBody());
        long timestamp = response.getBody().getTimestamp();
        assertTrue(timestamp >= beforeTime && timestamp <= afterTime);
    }

    @Test
    void handleBusinessException_IncludesPathFromRequest() {
        // Given
        BusinessException exception = new ResourceNotFoundException("Not found");
        String expectedPath = "/api/protected/resource";
        when(request.getServletPath()).thenReturn(expectedPath);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals(expectedPath, response.getBody().getPath());
    }

    @Test
    void handleBusinessException_WithDifferentStatusCodes() {
        // Test with custom BusinessException with different status codes
        BusinessException exception400 = new BusinessException("Bad request", 400);
        BusinessException exception403 = new BusinessException("Forbidden", 403);
        BusinessException exception500 = new BusinessException("Server error", 500);

        when(request.getServletPath()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response400 = globalExceptionHandler.handleBusinessException(exception400, request);
        ResponseEntity<ErrorResponse> response403 = globalExceptionHandler.handleBusinessException(exception403, request);
        ResponseEntity<ErrorResponse> response500 = globalExceptionHandler.handleBusinessException(exception500, request);

        assertEquals(400, response400.getBody().getStatus());
        assertEquals(403, response403.getBody().getStatus());
        assertEquals(500, response500.getBody().getStatus());
    }

    @Test
    void handleBusinessException_ErrorFieldUsesHttpStatusReason() {
        // Given
        BusinessException exception = new InvalidCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    @Test
    void handleBusinessException_AllFieldsPresent() {
        // Given
        BusinessException exception = new ResourceNotFoundException("Resource not found");
        when(request.getServletPath()).thenReturn("/api/resource");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getPath());
    }

    // ==================== MethodArgumentNotValid Handler Tests ====================

    @Test
    void handleMethodArgumentNotValid_SingleFieldError_Returns400() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("User", "email", "Email is required");
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(request.getServletPath()).thenReturn("/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValid_MultipleFieldErrors_IncludesAllErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("User", "email", "Email is required"));
        fieldErrors.add(new FieldError("User", "name", "Name must not be blank"));
        fieldErrors.add(new FieldError("User", "age", "Age must be positive"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(request.getServletPath()).thenReturn("/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Then
        assertNotNull(response.getBody());
        Map<String, String> details = response.getBody().getDetails();
        assertNotNull(details);
        assertEquals(3, details.size());
        assertEquals("Email is required", details.get("email"));
        assertEquals("Name must not be blank", details.get("name"));
        assertEquals("Age must be positive", details.get("age"));
    }

    @Test
    void handleMethodArgumentNotValid_IncludesTimestamp() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
        long beforeTime = System.currentTimeMillis();
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);
        long afterTime = System.currentTimeMillis();

        // Then
        assertNotNull(response.getBody());
        long timestamp = response.getBody().getTimestamp();
        assertTrue(timestamp >= beforeTime && timestamp <= afterTime);
    }

    @Test
    void handleMethodArgumentNotValid_IncludesRequestPath() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
        String expectedPath = "/api/create-user";
        when(request.getServletPath()).thenReturn(expectedPath);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals(expectedPath, response.getBody().getPath());
    }

    @Test
    void handleMethodArgumentNotValid_NoFieldErrors_EmptyDetails() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Then
        assertNotNull(response.getBody());
        Map<String, String> details = response.getBody().getDetails();
        assertNotNull(details);
        assertTrue(details.isEmpty());
    }

    @Test
    void handleMethodArgumentNotValid_AllFieldsPresent() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("User", "username", "Username is required"));
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(request.getServletPath()).thenReturn("/api/register");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Validation failed", errorResponse.getMessage());
        assertNotNull(errorResponse.getPath());
        assertNotNull(errorResponse.getDetails());
    }

    // ==================== Generic Exception Handler Tests ====================

    @Test
    void handleGenericException_UnexpectedException_Returns500() {
        // Given
        Exception exception = new RuntimeException("Something went wrong");
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_NullPointerException_Returns500() {
        // Given
        Exception exception = new NullPointerException("Null value encountered");
        when(request.getServletPath()).thenReturn("/api/data");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    void handleGenericException_DatabaseException_Returns500() {
        // Given
        Exception exception = new IllegalStateException("Database connection failed");
        when(request.getServletPath()).thenReturn("/api/database");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    void handleGenericException_IncludesTimestamp() {
        // Given
        Exception exception = new Exception("Test");
        long beforeTime = System.currentTimeMillis();
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);
        long afterTime = System.currentTimeMillis();

        // Then
        assertNotNull(response.getBody());
        long timestamp = response.getBody().getTimestamp();
        assertTrue(timestamp >= beforeTime && timestamp <= afterTime);
    }

    @Test
    void handleGenericException_IncludesRequestPath() {
        // Given
        Exception exception = new Exception("Test");
        String expectedPath = "/api/problematic/endpoint";
        when(request.getServletPath()).thenReturn(expectedPath);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals(expectedPath, response.getBody().getPath());
    }

    @Test
    void handleGenericException_AllFieldsPresent() {
        // Given
        Exception exception = new Exception("Unexpected error");
        when(request.getServletPath()).thenReturn("/api/endpoint");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getPath());
    }

    @Test
    void handleGenericException_DoesNotIncludeDetails() {
        // Given
        Exception exception = new Exception("Test");
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response.getBody());
        // Details should be null or empty for generic exceptions
        assertNull(response.getBody().getDetails());
    }

    // ==================== Handler Precedence Tests ====================

    @Test
    void businessExceptionHandlerTakesPrecedenceOverGenericHandler() {
        // Given
        BusinessException businessException = new ResourceNotFoundException("Not found");
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(businessException, request);

        // Then
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    void validationExceptionHandlerTakesPrecedenceOverGenericHandler() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("Request", "field", "Invalid"));
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(request.getServletPath()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Then
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
    }

    // ==================== Status Code Mapping Tests ====================

    @Test
    void httpStatusIsCorrectlyMappedFromStatusCode() {
        // Test that status codes map to correct HTTP statuses
        ResponseEntity<ErrorResponse> response404 = globalExceptionHandler.handleBusinessException(
            new ResourceNotFoundException("Not found"), request
        );
        assertEquals(HttpStatus.NOT_FOUND, response404.getStatusCode());

        ResponseEntity<ErrorResponse> response401 = globalExceptionHandler.handleBusinessException(
            new InvalidCredentialsException("Unauthorized"), request
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response401.getStatusCode());
    }
}
