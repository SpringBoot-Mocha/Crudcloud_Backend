package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.UpdateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.UserService;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserController.
 *
 * Tests user profile management endpoints: GET, PUT, DELETE.
 */
public class UserControllerTest extends BaseControllerTest {

    @MockBean
    private UserService userService;

    @Test
    public void testGetUserSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        UserResponse response = UserResponse.builder()
                .userId(userId)
                .email("test@example.com")
                .name("Test User")
                .isOrganization(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(userId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testGetUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User", userId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testUpdateUserSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        UserResponse response = UserResponse.builder()
                .userId(userId)
                .email("test@example.com")
                .name("Updated Name")
                .isOrganization(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    public void testUpdateUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User", userId));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUserSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        doThrow(new ResourceNotFoundException("User", userId))
                .when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
