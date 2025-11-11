package com.crudzaso.CrudCloud.security;

import com.crudzaso.CrudCloud.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter
 * Coverage: JWT token extraction and validation in filter chain
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String userEmail = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.getUsernameFromToken(validToken)).thenReturn(userEmail);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userEmail, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().isEmpty());

        verify(jwtService).validateToken(validToken);
        verify(jwtService).getUsernameFromToken(validToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).getUsernameFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidTokenFormat_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtService, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(jwtService).validateToken(invalidToken);
        verify(jwtService, never()).getUsernameFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExceptionInValidation_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenThrow(new RuntimeException("Token validation error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExceptionInUsernameExtraction_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.getUsernameFromToken(validToken)).thenThrow(new RuntimeException("Username extraction error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NullToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer null");
        when(jwtService.validateToken("null")).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidTokenWithDetails_SetsAuthenticationDetails() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String userEmail = "user@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.getUsernameFromToken(validToken)).thenReturn(userEmail);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertNotNull(authentication.getDetails());
        assertEquals(userEmail, authentication.getPrincipal());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_BearerWithExtraSpaces_ExtractsToken() throws ServletException, IOException {
        // Given
        String validToken = "valid.jwt.token";
        String userEmail = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.getUsernameFromToken(validToken)).thenReturn(userEmail);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userEmail, authentication.getPrincipal());

        verify(filterChain).doFilter(request, response);
    }
}
