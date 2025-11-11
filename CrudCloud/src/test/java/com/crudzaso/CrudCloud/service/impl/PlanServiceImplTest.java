package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
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
    private ModelMapper modelMapper;

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
                .maxInstances(1)
                .maxStorageGB(1L)
                .description("Free plan for testing")
                .build();

        standardPlan = Plan.builder()
                .id(2L)
                .name("STANDARD")
                .pricePerMonth(new BigDecimal("9.99"))
                .maxInstances(5)
                .maxStorageGB(100L)
                .description("Standard plan for small teams")
                .build();

        premiumPlan = Plan.builder()
                .id(3L)
                .name("PREMIUM")
                .pricePerMonth(new BigDecimal("29.99"))
                .maxInstances(20)
                .maxStorageGB(500L)
                .description("Premium plan for enterprises")
                .build();

        // Setup test plan responses
        freePlanResponse = PlanResponse.builder()
                .id(1L)
                .name("FREE")
                .priceMonth(BigDecimal.ZERO)
                .maxInstances(1)
                .maxStorageGB(1L)
                .description("Free plan for testing")
                .build();

        standardPlanResponse = PlanResponse.builder()
                .id(2L)
                .name("STANDARD")
                .priceMonth(new BigDecimal("9.99"))
                .maxInstances(5)
                .maxStorageGB(100L)
                .description("Standard plan for small teams")
                .build();

        premiumPlanResponse = PlanResponse.builder()
                .id(3L)
                .name("PREMIUM")
                .priceMonth(new BigDecimal("29.99"))
                .maxInstances(20)
                .maxStorageGB(500L)
                .description("Premium plan for enterprises")
                .build();
    }

    @Test
    void getAllPlans_Success() {
        // Given
        List<Plan> plans = Arrays.asList(freePlan, standardPlan, premiumPlan);
        when(planRepository.findAll(any(Sort.class))).thenReturn(plans);
        when(modelMapper.map(freePlan, PlanResponse.class)).thenReturn(freePlanResponse);
        when(modelMapper.map(standardPlan, PlanResponse.class)).thenReturn(standardPlanResponse);
        when(modelMapper.map(premiumPlan, PlanResponse.class)).thenReturn(premiumPlanResponse);

        // When
        List<PlanResponse> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("FREE", result.get(0).getName());
        assertEquals("STANDARD", result.get(1).getName());
        assertEquals("PREMIUM", result.get(2).getName());

        verify(planRepository).findAll(any(Sort.class));
        verify(modelMapper, times(3)).map(any(Plan.class), eq(PlanResponse.class));
    }

    @Test
    void getAllPlans_EmptyList_ReturnsEmptyList() {
        // Given
        when(planRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList());

        // When
        List<PlanResponse> result = planService.getAllPlans();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(planRepository).findAll(any(Sort.class));
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getAllPlans_VerifySortingByPrice() {
        // Given
        List<Plan> plans = Arrays.asList(freePlan);
        when(planRepository.findAll(any(Sort.class))).thenReturn(plans);
        when(modelMapper.map(any(Plan.class), eq(PlanResponse.class))).thenReturn(freePlanResponse);

        // When
        planService.getAllPlans();

        // Then - Verify that findAll was called with Sort parameter
        verify(planRepository).findAll(argThat((Sort sort) -> {
            return sort != null &&
                   sort.getOrderFor("priceMonth") != null &&
                   sort.getOrderFor("priceMonth").getDirection() == Sort.Direction.ASC;
        }));
    }

    @Test
    void getPlanById_Success() {
        // Given
        when(planRepository.findById(2L)).thenReturn(Optional.of(standardPlan));
        when(modelMapper.map(standardPlan, PlanResponse.class)).thenReturn(standardPlanResponse);

        // When
        PlanResponse result = planService.getPlanById(2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("STANDARD", result.getName());
        assertEquals(new BigDecimal("9.99"), result.getPriceMonth());
        assertEquals(5, result.getMaxInstances());
        assertEquals(100L, result.getMaxStorageGB());

        verify(planRepository).findById(2L);
        verify(modelMapper).map(standardPlan, PlanResponse.class);
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
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getPlanById_FreePlan_ReturnsCorrectData() {
        // Given
        when(planRepository.findById(1L)).thenReturn(Optional.of(freePlan));
        when(modelMapper.map(freePlan, PlanResponse.class)).thenReturn(freePlanResponse);

        // When
        PlanResponse result = planService.getPlanById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FREE", result.getName());
        assertEquals(BigDecimal.ZERO, result.getPriceMonth());
        assertEquals(1, result.getMaxInstances());

        verify(planRepository).findById(1L);
    }

    @Test
    void getPlanById_PremiumPlan_ReturnsCorrectData() {
        // Given
        when(planRepository.findById(3L)).thenReturn(Optional.of(premiumPlan));
        when(modelMapper.map(premiumPlan, PlanResponse.class)).thenReturn(premiumPlanResponse);

        // When
        PlanResponse result = planService.getPlanById(3L);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("PREMIUM", result.getName());
        assertEquals(new BigDecimal("29.99"), result.getPriceMonth());
        assertEquals(20, result.getMaxInstances());
        assertEquals(500L, result.getMaxStorageGB());

        verify(planRepository).findById(3L);
    }

    @Test
    void getAllPlans_VerifyMapperCalledForEachPlan() {
        // Given
        List<Plan> plans = Arrays.asList(freePlan, standardPlan);
        when(planRepository.findAll(any(Sort.class))).thenReturn(plans);
        when(modelMapper.map(freePlan, PlanResponse.class)).thenReturn(freePlanResponse);
        when(modelMapper.map(standardPlan, PlanResponse.class)).thenReturn(standardPlanResponse);

        // When
        List<PlanResponse> result = planService.getAllPlans();

        // Then
        assertEquals(2, result.size());
        verify(modelMapper).map(freePlan, PlanResponse.class);
        verify(modelMapper).map(standardPlan, PlanResponse.class);
    }
}
