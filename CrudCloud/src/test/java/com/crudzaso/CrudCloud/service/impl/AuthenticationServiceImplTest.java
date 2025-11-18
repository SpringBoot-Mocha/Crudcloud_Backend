package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.LoginRequest;
import com.crudzaso.CrudCloud.dto.response.AuthResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.UnauthorizedException;
import com.crudzaso.CrudCloud.mapper.UserMapper;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationServiceImpl using Mockito
 * Coverage: Login flow with JWT generation
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private LoginRequest loginRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Test")
                .lastName("User")
                .build();

        userResponse = new UserResponse();
        userResponse.setUserId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("Test");
        userResponse.setLastName("User");
    }

    @Test
    void login_Success() {
        // Given
        String expectedToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.token";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn(expectedToken);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        AuthResponse result = authenticationService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertNotNull(result.getUser());
        assertEquals("test@example.com", result.getUser().getEmail());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtService).generateToken("test@example.com");
        verify(userMapper).toResponse(user);
    }

    @Test
    void login_UserNotFound_ThrowsUnauthorizedException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("nonexistent@example.com");
        invalidRequest.setPassword("password123");

        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authenticationService.login(invalidRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("wrongPassword");

        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authenticationService.login(invalidRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongPassword", "$2a$10$hashedPassword");
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_CorrectCredentials_GeneratesToken() {
        // Given
        String expectedToken = "valid.jwt.token";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn(expectedToken);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        AuthResponse result = authenticationService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());

        verify(jwtService).generateToken("test@example.com");
    }

    @Test
    void login_VerifyUserResponseMapping() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString())).thenReturn("token");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        AuthResponse result = authenticationService.login(loginRequest);

        // Then
        assertNotNull(result.getUser());
        assertEquals(1L, result.getUser().getUserId());
        assertEquals("test@example.com", result.getUser().getEmail());
        assertEquals("Test", result.getUser().getFirstName());
        assertEquals("User", result.getUser().getLastName());

        verify(userMapper).toResponse(user);
    }
}
