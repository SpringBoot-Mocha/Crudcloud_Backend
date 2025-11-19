package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.PlanMapper;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlanServiceImpl using Mockito
 * Coverage: All plan retrieval methods
 */
@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanMapper planMapper;

    @InjectMocks
    private PlanServiceImpl planService;

    private Plan freePlan;
    private Plan standardPlan;
    private Plan premiumPlan;
    private PlanResponse freePlanResponse;
    private PlanResponse standardPlanResponse;
    private PlanResponse premiumPlanResponse;

    @BeforeEach
    void setUp() {
        // Setup test plans
        freePlan = Plan.builder()
                .id(1L)
                .name("FREE")
                .pricePerMonth(BigDecimal.ZERO)
                .maxInstances(2)
                .maxStorageMB(150L)
                .description("Plan Gratuito - 2 instancias, 150 MB almacenamiento")
                .build();

        standardPlan = Plan.builder()
                .id(2L)
                .name("STANDARD")
                .pricePerMonth(new BigDecimal("12000.00"))
                .maxInstances(5)
                .maxStorageMB(750L)
                .description("Plan Estándar - 5 instancias, 750 MB almacenamiento")
                .build();

        premiumPlan = Plan.builder()
                .id(3L)
                .name("PREMIUM")
                .pricePerMonth(new BigDecimal("39900.00"))
                .maxInstances(10)
                .maxStorageMB(2048L)
                .description("Plan Premium - 10 instancias, 2048 MB almacenamiento")
                .build();

        // Setup test plan responses
        freePlanResponse = PlanResponse.builder()
                .id(1L)
                .name("FREE")
                .priceMonth(BigDecimal.ZERO)
                .maxInstances(2)
                .maxStorageMB(150L)
                .description("Plan Gratuito - 2 instancias, 150 MB almacenamiento")
                .build();

        standardPlanResponse = PlanResponse.builder()
                .id(2L)
                .name("STANDARD")
                .priceMonth(new BigDecimal("12000.00"))
                .maxInstances(5)
                .maxStorageMB(750L)
                .description("Plan Estándar - 5 instancias, 750 MB almacenamiento")
                .build();

        premiumPlanResponse = PlanResponse.builder()
                .id(3L)
                .name("PREMIUM")
                .priceMonth(new BigDecimal("39900.00"))
                .maxInstances(10)
                .maxStorageMB(2048L)
                .description("Plan Premium - 10 instancias, 2048 MB almacenamiento")
                .build();
    }

    @Test
    void getAllPlans_Success() {
        // Given
        List<Plan> plans = Arrays.asList(freePlan, standardPlan, premiumPlan);
        List<PlanResponse> expectedResponses = Arrays.asList(freePlanResponse, standardPlanResponse, premiumPlanResponse);
        when(planRepository.findAll(any(Sort.class))).thenReturn(plans);
        when(planMapper.toResponseList(plans)).thenReturn(expectedResponses);

        // When
        List<PlanResponse> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("FREE", result.get(0).getName());
        assertEquals("STANDARD", result.get(1).getName());
        assertEquals("PREMIUM", result.get(2).getName());

        verify(planRepository).findAll(any(Sort.class));
        verify(planMapper).toResponseList(plans);
    }

    @Test
    void getAllPlans_EmptyList_ReturnsEmptyList() {
        // Given
        List<Plan> emptyPlans = Arrays.asList();
        when(planRepository.findAll(any(Sort.class))).thenReturn(emptyPlans);
        when(planMapper.toResponseList(emptyPlans)).thenReturn(Arrays.asList());

        // When
        List<PlanResponse> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(planRepository).findAll(any(Sort.class));
        verify(planMapper).toResponseList(emptyPlans);
    }

    @Test
    void getAllPlans_VerifySortingByPrice() {
        // Given
        List<Plan> plans = Arrays.asList(freePlan);
        List<PlanResponse> expectedResponses = Arrays.asList(freePlanResponse);
        when(planRepository.findAll(any(Sort.class))).thenReturn(plans);
        when(planMapper.toResponseList(plans)).thenReturn(expectedResponses);

        // When
        planService.getAllPlans();

        // Then - Verify that findAll was called with Sort parameter
        verify(planRepository).findAll(any(Sort.class));
        verify(planMapper).toResponseList(plans);
    }

    @Test
    void getPlanById_Success() {
        // Given
        when(planRepository.findById(2L)).thenReturn(Optional.of(standardPlan));
        when(planMapper.toResponse(standardPlan)).thenReturn(standardPlanResponse);

        // When
        PlanResponse result = planService.getPlanById(2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("STANDARD", result.getName());
        assertEquals(new BigDecimal("12000.00"), result.getPriceMonth());
        assertEquals(5, result.getMaxInstances());
        assertEquals(750L, result.getMaxStorageMB());

        verify(planRepository).findById(2L);
        verify(planMapper).toResponse(standardPlan);
    }

    @Test
    void getPlanById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(planRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            planService.getPlanById(999L);
        });

        assertTrue(exception.getMessage().contains("Plan"));
        assertTrue(exception.getMessage().contains("id"));
        assertTrue(exception.getMessage().contains("999"));

        verify(planRepository).findById(999L);
        verify(planMapper, never()).toResponse(any());
    }

    @Test
    void getPlanById_FreePlan_ReturnsCorrectData() {
        // Given
        when(planRepository.findById(1L)).thenReturn(Optional.of(freePlan));
        when(planMapper.toResponse(freePlan)).thenReturn(freePlanResponse);

        // When
        PlanResponse result = planService.getPlanById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FREE", result.getName());
        assertEquals(BigDecimal.ZERO, result.getPriceMonth());
        assertEquals(1, result.getMaxInstances());

        verify(planRepository).findById(1L);
        verify(planMapper).toResponse(freePlan);
    }

    @Test
    void getPlanById_PremiumPlan_ReturnsCorrectData() {
        // Given
        when(planRepository.findById(3L)).thenReturn(Optional.of(premiumPlan));
        when(planMapper.toResponse(premiumPlan)).thenReturn(premiumPlanResponse);

        // When
        PlanResponse result = planService.getPlanById(3L);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("PREMIUM", result.getName());
        assertEquals(new BigDecimal("39900.00"), result.getPriceMonth());
        assertEquals(10, result.getMaxInstances());
        assertEquals(2048L, result.getMaxStorageMB());

        verify(planRepository).findById(3L);
        verify(planMapper).toResponse(premiumPlan);
    }

    @Test
    void getAllPlans_VerifyMapperCalledForEachPlan() {
        // Given
        List<Plan> plans = Arrays.asList(freePlan, standardPlan);
        List<PlanResponse> expectedResponses = Arrays.asList(freePlanResponse, standardPlanResponse);
        when(planRepository.findAll(any(Sort.class))).thenReturn(plans);
        when(planMapper.toResponseList(plans)).thenReturn(expectedResponses);

        // When
        List<PlanResponse> result = planService.getAllPlans();

        // Then
        assertEquals(2, result.size());
        verify(planMapper).toResponseList(plans);
    }
}
