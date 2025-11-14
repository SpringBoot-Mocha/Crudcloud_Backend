package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.BaseIntegrationTest;
import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SubscriptionMapper using Spring Boot Test
 * Tests the actual MapStruct implementation with Spring context
 * Specifically tests the relationship mappings:
 * - user.id → userId
 * - plan.id → planId
 * - plan.name → planName
 * 
 * Extends BaseIntegrationTest for:
 * - Automatic Spring Boot context loading
 * - PostgreSQL database configuration (test profile)
 * - Automatic transaction rollback after each test (data cleanup)
 */
@ExtendWith(SpringExtension.class)
class SubscriptionMapperTest extends BaseIntegrationTest {

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    private Subscription subscription;
    private User user;
    private Plan plan;

    @BeforeEach
    void setUp() {
        // Setup test entities
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .isOrganization(false)
                .build();

        plan = Plan.builder()
                .id(1L)
                .name("Basic Plan")
                .description("Basic database hosting plan")
                .pricePerMonth(new BigDecimal("9.99"))
                .maxInstances(3)
                .maxStorageGB(10L)
                .build();

        LocalDateTime now = LocalDateTime.now();
        subscription = Subscription.builder()
                .id(1L)
                .user(user)
                .plan(plan)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void toResponse_ValidSubscription_ReturnsCorrectResponse() {
        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);

        // Then
        assertNotNull(response);
        assertEquals(subscription.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId()); // Tests user.id → userId mapping
        assertEquals(plan.getId(), response.getPlanId()); // Tests plan.id → planId mapping
        assertEquals(plan.getName(), response.getPlanName()); // Tests plan.name → planName mapping
        assertEquals(subscription.getStartDate(), response.getStartDate());
        assertEquals(subscription.getEndDate(), response.getEndDate());
        assertEquals(subscription.getIsActive(), response.getIsActive());
        assertEquals(subscription.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void toResponse_NullSubscription_ReturnsNull() {
        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toResponse_SubscriptionWithNullUser_ReturnsResponseWithNullUserId() {
        // Given
        Subscription subscriptionWithNullUser = Subscription.builder()
                .id(2L)
                .user(null)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(subscriptionWithNullUser);

        // Then
        assertNotNull(response);
        assertEquals(subscriptionWithNullUser.getId(), response.getId());
        assertNull(response.getUserId()); // userId should be null when user is null
        assertEquals(plan.getId(), response.getPlanId());
        assertEquals(plan.getName(), response.getPlanName());
    }

    @Test
    void toResponse_SubscriptionWithNullPlan_ReturnsResponseWithNullPlanFields() {
        // Given
        Subscription subscriptionWithNullPlan = Subscription.builder()
                .id(3L)
                .user(user)
                .plan(null)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(subscriptionWithNullPlan);

        // Then
        assertNotNull(response);
        assertEquals(subscriptionWithNullPlan.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId());
        assertNull(response.getPlanId()); // planId should be null when plan is null
        assertNull(response.getPlanName()); // planName should be null when plan is null
    }

    @Test
    void toResponse_SubscriptionWithNullFields_ReturnsResponseWithNullFields() {
        // Given
        Subscription subscriptionWithNullFields = Subscription.builder()
                .id(4L)
                .user(user)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(subscriptionWithNullFields);

        // Then
        assertNotNull(response);
        assertEquals(subscriptionWithNullFields.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(plan.getId(), response.getPlanId());
        assertEquals(plan.getName(), response.getPlanName());
        assertNull(response.getEndDate()); // endDate should be null
    }

    @Test
    void toResponse_SubscriptionWithInactiveStatus_ReturnsResponseWithInactiveStatus() {
        // Given
        Subscription inactiveSubscription = Subscription.builder()
                .id(5L)
                .user(user)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(inactiveSubscription);

        // Then
        assertNotNull(response);
        assertEquals(inactiveSubscription.getId(), response.getId());
        assertFalse(response.getIsActive());
    }

    @Test
    void toResponse_SubscriptionWithNoEndDate_ReturnsResponseWithNullEndDate() {
        // Given
        Subscription subscriptionWithoutEndDate = Subscription.builder()
                .id(6L)
                .user(user)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(subscriptionWithoutEndDate);

        // Then
        assertNotNull(response);
        assertEquals(subscriptionWithoutEndDate.getId(), response.getId());
        assertNull(response.getEndDate());
        assertTrue(response.getIsActive());
    }

    @Test
    void toResponseList_ValidSubscriptions_ReturnsCorrectResponseList() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .name("Another User")
                .isOrganization(false)
                .build();

        Plan plan2 = Plan.builder()
                .id(2L)
                .name("Pro Plan")
                .description("Professional database hosting plan")
                .pricePerMonth(new BigDecimal("29.99"))
                .maxInstances(10)
                .maxStorageGB(50L)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Subscription subscription2 = Subscription.builder()
                .id(2L)
                .user(user2)
                .plan(plan2)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<Subscription> subscriptions = Arrays.asList(subscription, subscription2);

        // When
        List<SubscriptionResponse> responses = subscriptionMapper.toResponseList(subscriptions);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());

        // Verify first subscription
        SubscriptionResponse response1 = responses.get(0);
        assertEquals(subscription.getId(), response1.getId());
        assertEquals(user.getId(), response1.getUserId());
        assertEquals(plan.getId(), response1.getPlanId());
        assertEquals(plan.getName(), response1.getPlanName());

        // Verify second subscription
        SubscriptionResponse response2 = responses.get(1);
        assertEquals(subscription2.getId(), response2.getId());
        assertEquals(user2.getId(), response2.getUserId());
        assertEquals(plan2.getId(), response2.getPlanId());
        assertEquals(plan2.getName(), response2.getPlanName());
    }

    @Test
    void toResponseList_EmptyList_ReturnsEmptyList() {
        // Given
        List<Subscription> emptyList = Arrays.asList();

        // When
        List<SubscriptionResponse> responses = subscriptionMapper.toResponseList(emptyList);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void toResponseList_NullList_ReturnsNull() {
        // When
        List<SubscriptionResponse> responses = subscriptionMapper.toResponseList(null);

        // Then
        assertNull(responses);
    }

    @Test
    void toResponseList_MixedActiveInactiveSubscriptions_ReturnsCorrectResponses() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Subscription activeSubscription = Subscription.builder()
                .id(7L)
                .user(user)
                .plan(plan)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Subscription inactiveSubscription = Subscription.builder()
                .id(8L)
                .user(user)
                .plan(plan)
                .startDate(now.minusMonths(2))
                .endDate(now.minusMonths(1))
                .isActive(false)
                .createdAt(now.minusMonths(2))
                .updatedAt(now.minusMonths(1))
                .build();

        List<Subscription> mixedSubscriptions = Arrays.asList(activeSubscription, inactiveSubscription);

        // When
        List<SubscriptionResponse> responses = subscriptionMapper.toResponseList(mixedSubscriptions);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());

        // Verify active subscription
        SubscriptionResponse activeResponse = responses.get(0);
        assertTrue(activeResponse.getIsActive());

        // Verify inactive subscription
        SubscriptionResponse inactiveResponse = responses.get(1);
        assertFalse(inactiveResponse.getIsActive());
    }

    @Test
    void toResponse_SubscriptionWithDifferentUserAndPlan_ReturnsCorrectMappings() {
        // Given
        User differentUser = User.builder()
                .id(999L)
                .email("different@example.com")
                .name("Different User")
                .isOrganization(true)
                .build();

        Plan differentPlan = Plan.builder()
                .id(999L)
                .name("Enterprise Plan")
                .description("Enterprise database hosting plan")
                .pricePerMonth(new BigDecimal("199.99"))
                .maxInstances(50)
                .maxStorageGB(500L)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Subscription subscriptionWithDifferentEntities = Subscription.builder()
                .id(9L)
                .user(differentUser)
                .plan(differentPlan)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        SubscriptionResponse response = subscriptionMapper.toResponse(subscriptionWithDifferentEntities);

        // Then
        assertNotNull(response);
        assertEquals(subscriptionWithDifferentEntities.getId(), response.getId());
        assertEquals(differentUser.getId(), response.getUserId());
        assertEquals(differentPlan.getId(), response.getPlanId());
        assertEquals(differentPlan.getName(), response.getPlanName());
    }
}