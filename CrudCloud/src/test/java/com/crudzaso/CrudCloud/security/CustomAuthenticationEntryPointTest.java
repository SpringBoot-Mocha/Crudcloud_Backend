package com.crudzaso.CrudCloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomAuthenticationEntryPoint
 * Coverage: 401 Unauthorized JSON response handling
 */
@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void commence_BadCredentials_Returns401WithJsonResponse() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");
        when(request.getServletPath()).thenReturn("/api/login");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Unauthorized\"}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return errorMap.containsKey("timestamp") &&
                   errorMap.containsKey("status") &&
                   errorMap.containsKey("error") &&
                   errorMap.containsKey("message") &&
                   errorMap.containsKey("path") &&
                   errorMap.get("status").equals(401) &&
                   errorMap.get("error").equals("Unauthorized") &&
                   errorMap.get("path").equals("/api/login") &&
                   errorMap.get("message").toString().contains("Invalid credentials");
        }));
    }

    @Test
    void commence_InsufficientAuthentication_Returns401() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new InsufficientAuthenticationException("Full authentication required");
        when(request.getServletPath()).thenReturn("/api/users");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Unauthorized\"}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return errorMap.get("message").toString().contains("Full authentication required");
        }));
    }

    @Test
    void commence_SetsCorrectHttpStatus() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setStatus(401);
    }

    @Test
    void commence_SetsCorrectContentType() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setContentType("application/json");
    }

    @Test
    void commence_IncludesTimestampInResponse() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api/test");
        long beforeTime = System.currentTimeMillis();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);
        long afterTime = System.currentTimeMillis();

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            Object timestamp = errorMap.get("timestamp");
            if (!(timestamp instanceof Long)) return false;
            long ts = (Long) timestamp;
            return ts >= beforeTime && ts <= afterTime;
        }));
    }

    @Test
    void commence_IncludesRequestPathInResponse() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        String expectedPath = "/api/protected/resource";
        when(request.getServletPath()).thenReturn(expectedPath);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return expectedPath.equals(errorMap.get("path"));
        }));
    }

    @Test
    void commence_IncludesErrorMessageWithAuthenticationFailure() throws IOException, ServletException {
        // Given
        String exceptionMessage = "Token has expired";
        AuthenticationException authException = new BadCredentialsException(exceptionMessage);
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            String message = errorMap.get("message").toString();
            return message.contains("Authentication failed") && message.contains(exceptionMessage);
        }));
    }

    @Test
    void commence_WritesJsonToResponseWriter() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        String expectedJson = "{\"status\":401,\"error\":\"Unauthorized\"}";
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

        // When
        authenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Then
        assertEquals(expectedJson, stringWriter.toString());
        verify(response).getWriter();
    }

    @Test
    void commence_WithEmptyPath_HandlesCorrectly() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return "".equals(errorMap.get("path"));
        }));
    }

    @Test
    void commence_WithRootPath_HandlesCorrectly() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return "/".equals(errorMap.get("path"));
        }));
    }

    @Test
    void commence_ResponseContainsAllRequiredFields() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Access denied");
        when(request.getServletPath()).thenReturn("/api/admin");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return errorMap.containsKey("timestamp") &&
                   errorMap.containsKey("status") &&
                   errorMap.containsKey("error") &&
                   errorMap.containsKey("message") &&
                   errorMap.containsKey("path") &&
                   errorMap.size() == 5;
        }));
    }

    @Test
    void commence_ErrorFieldHasCorrectValue() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        authenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return "Unauthorized".equals(errorMap.get("error"));
        }));
    }

    @Test
    void commence_MultipleCallsWithDifferentPaths_HandlesEachCorrectly() throws IOException, ServletException {
        // First call
        AuthenticationException authException1 = new BadCredentialsException("First error");
        when(request.getServletPath()).thenReturn("/api/path1");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        authenticationEntryPoint.commence(request, response, authException1);

        // Second call
        AuthenticationException authException2 = new BadCredentialsException("Second error");
        when(request.getServletPath()).thenReturn("/api/path2");

        authenticationEntryPoint.commence(request, response, authException2);

        // Then
        verify(response, times(2)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(2)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper, times(2)).writeValueAsString(any());
    }
}
