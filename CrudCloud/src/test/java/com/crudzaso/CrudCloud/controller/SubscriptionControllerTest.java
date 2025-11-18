package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.CreateSubscriptionRequest;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.SubscriptionService;
import com.crudzaso.CrudCloud.service.UserService;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SubscriptionController.
 *
 * Tests subscription management endpoints including upgrade and plan validation.
 */
public class SubscriptionControllerTest extends BaseControllerTest {

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private UserService userService;

    @Test
    public void testUpgradeSubscriptionSuccess() throws Exception {
        // Arrange
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setUserId(1L);
        request.setPlanId(2L);

        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L)
                .userId(1L)
                .planId(2L)
                .planName("Pro")
                .isActive(true)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/subscriptions/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.planName").value("Pro"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    public void testUpgradeSubscriptionUserNotFound() throws Exception {
        // Arrange
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setUserId(999L);
        request.setPlanId(2L);

        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenThrow(new ResourceNotFoundException("User", 999L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/subscriptions/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpgradeSubscriptionPlanNotFound() throws Exception {
        // Arrange
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setUserId(1L);
        request.setPlanId(999L);

        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Plan", 999L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/subscriptions/upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testGetCurrentSubscriptionSuccess() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .email("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L)
                .userId(1L)
                .planId(1L)
                .planName("Free")
                .isActive(true)
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(LocalDateTime.now().plusMonths(11))
                .createdAt(LocalDateTime.now().minusMonths(1))
                .build();

        when(userService.getUserByEmail(anyString()))
                .thenReturn(userResponse);
        when(subscriptionService.getUserSubscription(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/subscriptions/current")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Free"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.planId").value(1L));
    }

    @Test
    public void testGetCurrentSubscriptionNotFound() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .email("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userService.getUserByEmail(anyString()))
                .thenReturn(userResponse);
        when(subscriptionService.getUserSubscription(1L))
                .thenThrow(new ResourceNotFoundException("Subscription", "userId", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/subscriptions/current")
                .param("userId", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testGetSubscriptionByIdSuccess() throws Exception {
        // Arrange
        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(5L)
                .userId(2L)
                .planId(3L)
                .planName("Professional")
                .isActive(true)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(12))
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionService.getSubscriptionById(5L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/subscriptions/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.planName").value("Professional"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    public void testGetSubscriptionByIdNotFound() throws Exception {
        // Arrange
        when(subscriptionService.getSubscriptionById(999L))
                .thenThrow(new ResourceNotFoundException("Subscription", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/subscriptions/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
