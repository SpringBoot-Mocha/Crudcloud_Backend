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
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomAccessDeniedHandler
 * Coverage: 403 Forbidden JSON response handling
 */
@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomAccessDeniedHandler accessDeniedHandler;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void handle_AccessDenied_Returns403WithJsonResponse() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("User lacks required permissions");
        when(request.getServletPath()).thenReturn("/api/admin");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Forbidden\"}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return errorMap.containsKey("timestamp") &&
                   errorMap.containsKey("status") &&
                   errorMap.containsKey("error") &&
                   errorMap.containsKey("message") &&
                   errorMap.containsKey("path") &&
                   errorMap.get("status").equals(403) &&
                   errorMap.get("error").equals("Forbidden") &&
                   errorMap.get("path").equals("/api/admin");
        }));
    }

    @Test
    void handle_SetsCorrectHttpStatus() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setStatus(403);
    }

    @Test
    void handle_SetsCorrectContentType() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setContentType("application/json");
    }

    @Test
    void handle_IncludesTimestampInResponse() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/test");
        long beforeTime = System.currentTimeMillis();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);
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
    void handle_IncludesRequestPathInResponse() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        String expectedPath = "/api/protected/resource";
        when(request.getServletPath()).thenReturn(expectedPath);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return expectedPath.equals(errorMap.get("path"));
        }));
    }

    @Test
    void handle_ErrorMessageIndicatesInsufficientPermissions() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("User is not an admin");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            String message = errorMap.get("message").toString();
            return message.contains("Access denied") && message.contains("insufficient permissions");
        }));
    }

    @Test
    void handle_WritesJsonToResponseWriter() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        String expectedJson = "{\"status\":403,\"error\":\"Forbidden\"}";
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);
        writer.flush();

        // Then
        assertEquals(expectedJson, stringWriter.toString());
        verify(response).getWriter();
    }

    @Test
    void handle_WithEmptyPath_HandlesCorrectly() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return "".equals(errorMap.get("path"));
        }));
    }

    @Test
    void handle_WithRootPath_HandlesCorrectly() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return "/".equals(errorMap.get("path"));
        }));
    }

    @Test
    void handle_WithComplexPath_HandlesCorrectly() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        String complexPath = "/api/v1/admin/users/123/roles";
        when(request.getServletPath()).thenReturn(complexPath);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return complexPath.equals(errorMap.get("path"));
        }));
    }

    @Test
    void handle_ResponseContainsAllRequiredFields() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/admin");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

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
    void handle_ErrorFieldHasCorrectValue() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return "Forbidden".equals(errorMap.get("error"));
        }));
    }

    @Test
    void handle_StatusFieldHasCorrectValue() throws IOException, ServletException {
        // Given
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Then
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            return Integer.valueOf(403).equals(errorMap.get("status"));
        }));
    }

    @Test
    void handle_MultipleCallsWithDifferentPaths_HandlesEachCorrectly() throws IOException, ServletException {
        // First call
        AccessDeniedException exception1 = new AccessDeniedException("First denied");
        when(request.getServletPath()).thenReturn("/api/admin");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        accessDeniedHandler.handle(request, response, exception1);

        // Second call
        AccessDeniedException exception2 = new AccessDeniedException("Second denied");
        when(request.getServletPath()).thenReturn("/api/users/delete");

        accessDeniedHandler.handle(request, response, exception2);

        // Then
        verify(response, times(2)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(2)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper, times(2)).writeValueAsString(any());
    }

    @Test
    void handle_VariousExceptionMessages_AllHandledCorrectly() throws IOException, ServletException {
        // Given various exception messages
        String[] exceptionMessages = {
            "Access denied",
            "Insufficient privileges",
            "Role required: ADMIN",
            "Permission required: DELETE_USER"
        };

        int callCount = 0;
        for (String message : exceptionMessages) {
            callCount++;
            AccessDeniedException exception = new AccessDeniedException(message);
            when(request.getServletPath()).thenReturn("/api/test");
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // When
            accessDeniedHandler.handle(request, response, exception);
        }

        // Then - verify response was set correctly for all calls
        verify(response, times(callCount)).setStatus(403);
        verify(response, times(callCount)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void handle_MessageAlwaysIndicatesInsufficientPermissions() throws IOException, ServletException {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Some specific reason");
        when(request.getServletPath()).thenReturn("/api/resource");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, exception);

        // Then - verify that the message always contains the standard suffix
        verify(objectMapper).writeValueAsString(argThat(map -> {
            if (!(map instanceof java.util.Map)) return false;
            java.util.Map<?, ?> errorMap = (java.util.Map<?, ?>) map;
            String message = errorMap.get("message").toString();
            return message.equals("Access denied: insufficient permissions");
        }));
    }

    @Test
    void handle_CalledWithNullPath_StillWorks() throws IOException, ServletException {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, exception);

        // Then
        verify(response).setStatus(403);
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    void handle_ObjectMapperIsCalledWithCorrectData() throws IOException, ServletException {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        when(request.getServletPath()).thenReturn("/api/test");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        accessDeniedHandler.handle(request, response, exception);

        // Then
        verify(objectMapper, times(1)).writeValueAsString(any(java.util.Map.class));
    }
}
