package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.CreateSubscriptionRequest;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.SubscriptionMapper;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionServiceImpl using Mockito
 * Coverage: Subscription creation and retrieval
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Plan plan;
    private Subscription subscription;
    private SubscriptionResponse subscriptionResponse;
    private CreateSubscriptionRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        plan = Plan.builder()
                .id(2L)
                .name("STANDARD")
                .pricePerMonth(new BigDecimal("9.99"))
                .maxInstances(5)
                .maxStorageGB(100L)
                .build();

        subscription = Subscription.builder()
                .id(10L)
                .user(user)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new CreateSubscriptionRequest();
        createRequest.setUserId(1L);
        createRequest.setPlanId(2L);

        subscriptionResponse = SubscriptionResponse.builder()
                .id(10L)
                .userId(1L)
                .planId(2L)
                .planName("STANDARD")
                .isActive(true)
                .build();
    }

    @Test
    void createSubscription_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        // When
        SubscriptionResponse result = subscriptionService.createSubscription(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getPlanId());
        assertEquals("STANDARD", result.getPlanName());
        assertTrue(result.getIsActive());

        verify(userRepository).findById(1L);
        verify(planRepository).findById(2L);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(subscriptionMapper).toResponse(subscription);
    }

    @Test
    void createSubscription_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CreateSubscriptionRequest invalidRequest = new CreateSubscriptionRequest();
        invalidRequest.setUserId(999L);
        invalidRequest.setPlanId(2L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            subscriptionService.createSubscription(invalidRequest);
        });

        verify(userRepository).findById(999L);
        verify(planRepository, never()).findById(anyLong());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_PlanNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(999L)).thenReturn(Optional.empty());

        CreateSubscriptionRequest invalidRequest = new CreateSubscriptionRequest();
        invalidRequest.setUserId(1L);
        invalidRequest.setPlanId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            subscriptionService.createSubscription(invalidRequest);
        });

        verify(userRepository).findById(1L);
        verify(planRepository).findById(999L);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getUserSubscription_Success() {
        // Given
        when(subscriptionRepository.findByUserIdAndIsActive(1L, true))
                .thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        // When
        SubscriptionResponse result = subscriptionService.getUserSubscription(1L);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("STANDARD", result.getPlanName());

        verify(subscriptionRepository).findByUserIdAndIsActive(1L, true);
        verify(subscriptionMapper).toResponse(subscription);
    }

    @Test
    void getUserSubscription_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(subscriptionRepository.findByUserIdAndIsActive(999L, true))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            subscriptionService.getUserSubscription(999L);
        });

        verify(subscriptionRepository).findByUserIdAndIsActive(999L, true);
    }

    @Test
    void getSubscriptionById_Success() {
        // Given
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        // When
        SubscriptionResponse result = subscriptionService.getSubscriptionById(10L);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getPlanId());

        verify(subscriptionRepository).findById(10L);
        verify(subscriptionMapper).toResponse(subscription);
    }

    @Test
    void getSubscriptionById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            subscriptionService.getSubscriptionById(999L);
        });

        verify(subscriptionRepository).findById(999L);
    }

    @Test
    void createSubscription_SetsCorrectAttributes() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription sub = invocation.getArgument(0);
            assertEquals(user, sub.getUser());
            assertEquals(plan, sub.getPlan());
            assertTrue(sub.getIsActive());
            assertNotNull(sub.getStartDate());
            return subscription;
        });
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        // When
        subscriptionService.createSubscription(createRequest);

        // Then
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(subscriptionMapper).toResponse(subscription);
    }
}
