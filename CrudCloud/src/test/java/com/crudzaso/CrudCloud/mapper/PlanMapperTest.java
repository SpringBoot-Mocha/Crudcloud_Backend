package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.BaseIntegrationTest;
import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlanMapper using Spring Boot Test
 * Tests the actual MapStruct implementation with Spring context
 * Specifically tests the pricePerMonth to priceMonth field mapping
 * 
 * Extends BaseIntegrationTest for:
 * - Automatic Spring Boot context loading
 * - PostgreSQL database configuration (test profile)
 * - Automatic transaction rollback after each test (data cleanup)
 */
@ExtendWith(SpringExtension.class)
class PlanMapperTest extends BaseIntegrationTest {

    @Autowired
    private PlanMapper planMapper;

    private Plan plan;

    @BeforeEach
    void setUp() {
        // Setup test entity
        plan = Plan.builder()
                .id(1L)
                .name("Basic Plan")
                .description("Basic database hosting plan")
                .pricePerMonth(new BigDecimal("9.99"))
                .maxInstances(3)
                .maxStorageGB(10L)
                .build();
    }

    @Test
    void toResponse_ValidPlan_ReturnsCorrectResponse() {
        // When
        PlanResponse response = planMapper.toResponse(plan);

        // Then
        assertNotNull(response);
        assertEquals(plan.getId(), response.getId());
        assertEquals(plan.getName(), response.getName());
        assertEquals(plan.getDescription(), response.getDescription());
        assertEquals(plan.getPricePerMonth(), response.getPriceMonth()); // Tests the field mapping
        assertEquals(plan.getMaxInstances(), response.getMaxInstances());
        assertEquals(plan.getMaxStorageGB(), response.getMaxStorageGB());
    }

    @Test
    void toResponse_NullPlan_ReturnsNull() {
        // When
        PlanResponse response = planMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toResponse_PlanWithNullFields_ReturnsResponseWithNullFields() {
        // Given
        Plan planWithNullFields = Plan.builder()
                .id(2L)
                .name("Free Plan")
                .description(null)
                .pricePerMonth(null)
                .maxInstances(1)
                .maxStorageGB(1L)
                .build();

        // When
        PlanResponse response = planMapper.toResponse(planWithNullFields);

        // Then
        assertNotNull(response);
        assertEquals(planWithNullFields.getId(), response.getId());
        assertEquals(planWithNullFields.getName(), response.getName());
        assertNull(response.getDescription());
        assertNull(response.getPriceMonth()); // Tests null price mapping
        assertEquals(planWithNullFields.getMaxInstances(), response.getMaxInstances());
        assertEquals(planWithNullFields.getMaxStorageGB(), response.getMaxStorageGB());
    }

    @Test
    void toResponse_PlanWithZeroPrice_ReturnsResponseWithZeroPrice() {
        // Given
        Plan freePlan = Plan.builder()
                .id(3L)
                .name("Free Plan")
                .description("Free database hosting plan")
                .pricePerMonth(BigDecimal.ZERO)
                .maxInstances(1)
                .maxStorageGB(1L)
                .build();

        // When
        PlanResponse response = planMapper.toResponse(freePlan);

        // Then
        assertNotNull(response);
        assertEquals(freePlan.getId(), response.getId());
        assertEquals(freePlan.getName(), response.getName());
        assertEquals(BigDecimal.ZERO, response.getPriceMonth());
        assertEquals(freePlan.getMaxInstances(), response.getMaxInstances());
        assertEquals(freePlan.getMaxStorageGB(), response.getMaxStorageGB());
    }

    @Test
    void toResponse_PlanWithHighPrice_ReturnsResponseWithHighPrice() {
        // Given
        Plan enterprisePlan = Plan.builder()
                .id(4L)
                .name("Enterprise Plan")
                .description("Enterprise database hosting plan")
                .pricePerMonth(new BigDecimal("999.99"))
                .maxInstances(100)
                .maxStorageGB(1000L)
                .build();

        // When
        PlanResponse response = planMapper.toResponse(enterprisePlan);

        // Then
        assertNotNull(response);
        assertEquals(enterprisePlan.getId(), response.getId());
        assertEquals(enterprisePlan.getName(), response.getName());
        assertEquals(new BigDecimal("999.99"), response.getPriceMonth());
        assertEquals(enterprisePlan.getMaxInstances(), response.getMaxInstances());
        assertEquals(enterprisePlan.getMaxStorageGB(), response.getMaxStorageGB());
    }


    @Test
    void toResponseList_ValidPlans_ReturnsCorrectResponseList() {
        // Given
        Plan plan2 = Plan.builder()
                .id(6L)
                .name("Pro Plan")
                .description("Professional database hosting plan")
                .pricePerMonth(new BigDecimal("29.99"))
                .maxInstances(10)
                .maxStorageGB(50L)
                .build();

        Plan plan3 = Plan.builder()
                .id(7L)
                .name("Business Plan")
                .description("Business database hosting plan")
                .pricePerMonth(new BigDecimal("99.99"))
                .maxInstances(25)
                .maxStorageGB(200L)
                .build();

        List<Plan> plans = Arrays.asList(plan, plan2, plan3);

        // When
        List<PlanResponse> responses = planMapper.toResponseList(plans);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        // Verify first plan
        PlanResponse response1 = responses.get(0);
        assertEquals(plan.getId(), response1.getId());
        assertEquals(plan.getName(), response1.getName());
        assertEquals(plan.getPricePerMonth(), response1.getPriceMonth());

        // Verify second plan
        PlanResponse response2 = responses.get(1);
        assertEquals(plan2.getId(), response2.getId());
        assertEquals(plan2.getName(), response2.getName());
        assertEquals(plan2.getPricePerMonth(), response2.getPriceMonth());

        // Verify third plan
        PlanResponse response3 = responses.get(2);
        assertEquals(plan3.getId(), response3.getId());
        assertEquals(plan3.getName(), response3.getName());
        assertEquals(plan3.getPricePerMonth(), response3.getPriceMonth());
    }

    @Test
    void toResponseList_EmptyList_ReturnsEmptyList() {
        // Given
        List<Plan> emptyList = Arrays.asList();

        // When
        List<PlanResponse> responses = planMapper.toResponseList(emptyList);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void toResponseList_NullList_ReturnsNull() {
        // When
        List<PlanResponse> responses = planMapper.toResponseList(null);

        // Then
        assertNull(responses);
    }

}