package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.DatabaseInstanceService;
import com.crudzaso.CrudCloud.service.UserService;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for DatabaseInstanceController.
 *
 * Tests database instance CRUD operations and ownership validation.
 */
public class DatabaseInstanceControllerTest extends BaseControllerTest {

    @MockBean
    private DatabaseInstanceService databaseInstanceService;

    @MockBean
    private UserService userService;

    @Test
    public void testCreateInstanceSuccess() throws Exception {
        // Arrange
        CreateInstanceRequest request = new CreateInstanceRequest();
        request.setUserId(1L);
        request.setSubscriptionId(1L);
        request.setDatabaseEngineId(1L);
        request.setInstanceName("My Production DB");

        DatabaseInstanceResponse response = DatabaseInstanceResponse.builder()
                .id(1L)
                .userId(1L)
                .subscriptionId(1L)
                .databaseEngine("PostgreSQL")
                .containerName("prod-db-001")
                .host("db.example.com")
                .port(5432)
                .status(InstanceStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .email("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userService.getUserByEmail(anyString()))
                .thenReturn(userResponse);
        when(databaseInstanceService.createInstance(any(CreateInstanceRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.containerName").value("prod-db-001"))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    public void testCreateInstanceInvalidUser() throws Exception {
        // Arrange
        CreateInstanceRequest request = new CreateInstanceRequest();
        request.setUserId(999L);
        request.setSubscriptionId(1L);
        request.setDatabaseEngineId(1L);
        request.setInstanceName("Invalid User DB");

        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .email("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userService.getUserByEmail(anyString()))
                .thenReturn(userResponse);
        when(databaseInstanceService.createInstance(any(CreateInstanceRequest.class)))
                .thenThrow(new ResourceNotFoundException("User", 999L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testGetInstanceSuccess() throws Exception {
        // Arrange
        DatabaseInstanceResponse response = DatabaseInstanceResponse.builder()
                .id(1L)
                .userId(1L)
                .subscriptionId(1L)
                .databaseEngine("PostgreSQL")
                .containerName("prod-db-001")
                .host("db.example.com")
                .port(5432)
                .status(InstanceStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .build();

        when(databaseInstanceService.getInstance(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/instances/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerName").value("prod-db-001"))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.port").value(5432));
    }

    @Test
    public void testGetInstanceNotFound() throws Exception {
        // Arrange
        when(databaseInstanceService.getInstance(999L))
                .thenThrow(new ResourceNotFoundException("DatabaseInstance", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/instances/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testGetUserInstancesSuccess() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .email("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        List<DatabaseInstanceResponse> instances = Arrays.asList(
                DatabaseInstanceResponse.builder()
                        .id(1L)
                        .userId(1L)
                        .subscriptionId(1L)
                        .databaseEngine("PostgreSQL")
                        .containerName("prod-db-001")
                        .status(InstanceStatus.RUNNING)
                        .port(5432)
                        .host("db1.example.com")
                        .createdAt(LocalDateTime.now())
                        .build(),
                DatabaseInstanceResponse.builder()
                        .id(2L)
                        .userId(1L)
                        .subscriptionId(1L)
                        .databaseEngine("MySQL")
                        .containerName("staging-db-001")
                        .status(InstanceStatus.RUNNING)
                        .port(3306)
                        .host("db2.example.com")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(userService.getUserByEmail(anyString()))
                .thenReturn(userResponse);
        when(databaseInstanceService.getUserInstances(1L)).thenReturn(instances);

        // Act & Assert
        mockMvc.perform(get("/api/v1/instances")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].containerName").value("prod-db-001"))
                .andExpect(jsonPath("$[1].containerName").value("staging-db-001"));
    }

    @Test
    public void testGetUserInstancesEmpty() throws Exception {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .userId(1L)
                .email("testuser")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userService.getUserByEmail(anyString()))
                .thenReturn(userResponse);
        when(databaseInstanceService.getUserInstances(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/instances")
                .param("userId", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testDeleteInstanceSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/instances/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteInstanceNotFound() throws Exception {
        // Arrange
        when(databaseInstanceService.getInstance(999L))
                .thenThrow(new ResourceNotFoundException("DatabaseInstance", 999L));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/instances/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
