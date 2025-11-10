package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import com.crudzaso.CrudCloud.service.DatabaseEngineService;
import com.crudzaso.CrudCloud.service.PlanService;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for public controllers.
 *
 * Tests endpoints that do not require authentication: DatabaseEngineController, PlanController.
 * Extends BasePublicControllerTest to avoid automatic authentication.
 */
public class PublicControllersTest extends BasePublicControllerTest {

    @MockBean
    private DatabaseEngineService databaseEngineService;

    @MockBean
    private PlanService planService;

    // DatabaseEngineController Tests

    @Test
    public void testGetAllEngines() throws Exception {
        // Arrange
        List<DatabaseEngineResponse> engines = Arrays.asList(
                DatabaseEngineResponse.builder()
                        .id(1L)
                        .name("PostgreSQL")
                        .version("15.0")
                        .defaultPort(5432)
                        .dockerImage("postgres:15")
                        .description("Open source object-relational database")
                        .build(),
                DatabaseEngineResponse.builder()
                        .id(2L)
                        .name("MySQL")
                        .version("8.0")
                        .defaultPort(3306)
                        .dockerImage("mysql:8.0")
                        .description("Open source relational database")
                        .build()
        );

        when(databaseEngineService.getAllEngines()).thenReturn(engines);

        // Act & Assert
        mockMvc.perform(get("/api/v1/engines")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("PostgreSQL"))
                .andExpect(jsonPath("$[1].name").value("MySQL"));
    }

    @Test
    public void testGetEngineById() throws Exception {
        // Arrange
        DatabaseEngineResponse engine = DatabaseEngineResponse.builder()
                .id(1L)
                .name("PostgreSQL")
                .version("15.0")
                .defaultPort(5432)
                .dockerImage("postgres:15")
                .description("Open source object-relational database")
                .build();

        when(databaseEngineService.getEngineById(1L)).thenReturn(engine);

        // Act & Assert
        mockMvc.perform(get("/api/v1/engines/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("PostgreSQL"))
                .andExpect(jsonPath("$.defaultPort").value(5432));
    }

    @Test
    public void testGetEngineByIdNotFound() throws Exception {
        // Arrange
        when(databaseEngineService.getEngineById(999L))
                .thenThrow(new ResourceNotFoundException("DatabaseEngine", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/engines/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // PlanController Tests

    @Test
    public void testGetAllPlans() throws Exception {
        // Arrange
        List<PlanResponse> plans = Arrays.asList(
                PlanResponse.builder()
                        .id(1L)
                        .name("Free")
                        .maxInstances(1)
                        .maxStorageGB(10L)
                        .priceMonth(BigDecimal.ZERO)
                        .description("Free tier")
                        .build(),
                PlanResponse.builder()
                        .id(2L)
                        .name("Pro")
                        .maxInstances(5)
                        .maxStorageGB(100L)
                        .priceMonth(new BigDecimal("29.99"))
                        .description("Professional tier")
                        .build()
        );

        when(planService.getAllPlans()).thenReturn(plans);

        // Act & Assert
        mockMvc.perform(get("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Free"))
                .andExpect(jsonPath("$[1].name").value("Pro"));
    }

    @Test
    public void testGetPlanById() throws Exception {
        // Arrange
        PlanResponse plan = PlanResponse.builder()
                .id(2L)
                .name("Pro")
                .maxInstances(5)
                .maxStorageGB(100L)
                .priceMonth(new BigDecimal("29.99"))
                .description("Professional tier")
                .build();

        when(planService.getPlanById(2L)).thenReturn(plan);

        // Act & Assert
        mockMvc.perform(get("/api/v1/plans/{id}", 2L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pro"))
                .andExpect(jsonPath("$.maxInstances").value(5));
    }

    @Test
    public void testGetPlanByIdNotFound() throws Exception {
        // Arrange
        when(planService.getPlanById(999L))
                .thenThrow(new ResourceNotFoundException("Plan", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/plans/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
