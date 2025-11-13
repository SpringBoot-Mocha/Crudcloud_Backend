package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.UpdateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.UserMapper;
import com.crudzaso.CrudCloud.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl using Mockito
 * Coverage: All methods in UserServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setName("Test User");
        createUserRequest.setIsOrganization(false);

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("Updated Name");

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .name("Test User")
                .isOrganization(false)
                .build();

        userResponse = new UserResponse();
        userResponse.setUserId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setName("Test User");
        userResponse.setIsOrganization(false);
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        UserResponse result = userService.createUser(createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsAppException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            userService.createUser(createUserRequest);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserByEmail_UserNotFound_ThrowsAppException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });

        assertTrue(exception.getMessage().contains("User not found with email"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void updateUser_Success() {
        // Given
        User updatedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .name("Updated Name")
                .isOrganization(false)
                .build();

        UserResponse updatedUserResponse = new UserResponse();
        updatedUserResponse.setUserId(1L);
        updatedUserResponse.setEmail("test@example.com");
        updatedUserResponse.setName("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(updatedUserResponse);

        // When
        UserResponse result = userService.updateUser(1L, updateUserRequest);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(999L, updateUserRequest);
        });

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }
}
