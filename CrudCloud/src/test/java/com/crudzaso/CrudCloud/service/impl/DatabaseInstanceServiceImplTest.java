package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import com.crudzaso.CrudCloud.repository.DatabaseInstanceRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatabaseInstanceServiceImpl using Mockito
 * Coverage: All database instance management methods
 */
@ExtendWith(MockitoExtension.class)
class DatabaseInstanceServiceImplTest {

    @Mock
    private DatabaseInstanceRepository databaseInstanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private DatabaseEngineRepository databaseEngineRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DatabaseInstanceServiceImpl databaseInstanceService;

    private User user;
    private Subscription subscription;
    private DatabaseEngine postgresEngine;
    private DatabaseEngine mysqlEngine;
    private DatabaseInstance postgresInstance;
    private DatabaseInstance mysqlInstance;
    private DatabaseInstanceResponse postgresInstanceResponse;
    private DatabaseInstanceResponse mysqlInstanceResponse;
    private CreateInstanceRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        subscription = Subscription.builder()
                .id(10L)
                .user(user)
                .isActive(true)
                .build();

        postgresEngine = DatabaseEngine.builder()
                .id(1L)
                .name("PostgreSQL")
                .version("14")
                .defaultPort(5432)
                .dockerImage("postgres:14")
                .description("PostgreSQL database")
                .build();

        mysqlEngine = DatabaseEngine.builder()
                .id(2L)
                .name("MySQL")
                .version("8.0")
                .defaultPort(3306)
                .dockerImage("mysql:8.0")
                .description("MySQL database")
                .build();

        postgresInstance = DatabaseInstance.builder()
                .id(100L)
                .user(user)
                .subscription(subscription)
                .databaseEngine(postgresEngine)
                .containerName("db-1-abc12345")
                .host("localhost")
                .port(5432)
                .status(InstanceStatus.CREATING)
                .build();

        mysqlInstance = DatabaseInstance.builder()
                .id(101L)
                .user(user)
                .subscription(subscription)
                .databaseEngine(mysqlEngine)
                .containerName("db-1-def67890")
                .host("localhost")
                .port(3306)
                .status(InstanceStatus.RUNNING)
                .build();

        postgresInstanceResponse = new DatabaseInstanceResponse();
        postgresInstanceResponse.setId(100L);
        postgresInstanceResponse.setContainerName("db-1-abc12345");
        postgresInstanceResponse.setStatus(InstanceStatus.CREATING);

        mysqlInstanceResponse = new DatabaseInstanceResponse();
        mysqlInstanceResponse.setId(101L);
        mysqlInstanceResponse.setContainerName("db-1-def67890");
        mysqlInstanceResponse.setStatus(InstanceStatus.RUNNING);

        createRequest = new CreateInstanceRequest();
        createRequest.setUserId(1L);
        createRequest.setSubscriptionId(10L);
        createRequest.setDatabaseEngineId(1L);
    }

    @Test
    void createInstance_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenReturn(postgresInstance);
        when(modelMapper.map(postgresInstance, DatabaseInstanceResponse.class)).thenReturn(postgresInstanceResponse);

        // When
        DatabaseInstanceResponse result = databaseInstanceService.createInstance(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("db-1-abc12345", result.getContainerName());
        assertEquals(InstanceStatus.CREATING, result.getStatus());

        verify(userRepository).findById(1L);
        verify(subscriptionRepository).findById(10L);
        verify(databaseEngineRepository).findById(1L);
        verify(databaseInstanceRepository).save(any(DatabaseInstance.class));
        verify(modelMapper).map(postgresInstance, DatabaseInstanceResponse.class);
    }

    @Test
    void createInstance_WithCustomInstanceName_Success() {
        // Given
        createRequest.setInstanceName("my-custom-db");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenAnswer(invocation -> {
            DatabaseInstance instance = invocation.getArgument(0);
            assertEquals("my-custom-db", instance.getContainerName());
            return postgresInstance;
        });
        when(modelMapper.map(any(DatabaseInstance.class), eq(DatabaseInstanceResponse.class))).thenReturn(postgresInstanceResponse);

        // When
        DatabaseInstanceResponse result = databaseInstanceService.createInstance(createRequest);

        // Then
        assertNotNull(result);
        verify(databaseInstanceRepository).save(argThat(instance ->
            "my-custom-db".equals(instance.getContainerName())
        ));
    }

    @Test
    void createInstance_UserNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CreateInstanceRequest invalidRequest = new CreateInstanceRequest();
        invalidRequest.setUserId(999L);
        invalidRequest.setSubscriptionId(10L);
        invalidRequest.setDatabaseEngineId(1L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            databaseInstanceService.createInstance(invalidRequest);
        });

        verify(userRepository).findById(999L);
        verify(subscriptionRepository, never()).findById(anyLong());
        verify(databaseEngineRepository, never()).findById(anyLong());
        verify(databaseInstanceRepository, never()).save(any());
    }

    @Test
    void createInstance_SubscriptionNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        CreateInstanceRequest invalidRequest = new CreateInstanceRequest();
        invalidRequest.setUserId(1L);
        invalidRequest.setSubscriptionId(999L);
        invalidRequest.setDatabaseEngineId(1L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            databaseInstanceService.createInstance(invalidRequest);
        });

        verify(userRepository).findById(1L);
        verify(subscriptionRepository).findById(999L);
        verify(databaseEngineRepository, never()).findById(anyLong());
        verify(databaseInstanceRepository, never()).save(any());
    }

    @Test
    void createInstance_DatabaseEngineNotFound_ThrowsResourceNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(999L)).thenReturn(Optional.empty());

        CreateInstanceRequest invalidRequest = new CreateInstanceRequest();
        invalidRequest.setUserId(1L);
        invalidRequest.setSubscriptionId(10L);
        invalidRequest.setDatabaseEngineId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            databaseInstanceService.createInstance(invalidRequest);
        });

        verify(userRepository).findById(1L);
        verify(subscriptionRepository).findById(10L);
        verify(databaseEngineRepository).findById(999L);
        verify(databaseInstanceRepository, never()).save(any());
    }

    @Test
    void createInstance_SetsCorrectDefaultValues() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenAnswer(invocation -> {
            DatabaseInstance instance = invocation.getArgument(0);
            assertEquals(user, instance.getUser());
            assertEquals(subscription, instance.getSubscription());
            assertEquals(postgresEngine, instance.getDatabaseEngine());
            assertEquals("localhost", instance.getHost());
            assertEquals(5432, instance.getPort());
            assertEquals(InstanceStatus.CREATING, instance.getStatus());
            return postgresInstance;
        });
        when(modelMapper.map(any(DatabaseInstance.class), eq(DatabaseInstanceResponse.class))).thenReturn(postgresInstanceResponse);

        // When
        databaseInstanceService.createInstance(createRequest);

        // Then
        verify(databaseInstanceRepository).save(any(DatabaseInstance.class));
    }

    @Test
    void getInstance_Success() {
        // Given
        when(databaseInstanceRepository.findById(100L)).thenReturn(Optional.of(postgresInstance));
        when(modelMapper.map(postgresInstance, DatabaseInstanceResponse.class)).thenReturn(postgresInstanceResponse);

        // When
        DatabaseInstanceResponse result = databaseInstanceService.getInstance(100L);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(InstanceStatus.CREATING, result.getStatus());

        verify(databaseInstanceRepository).findById(100L);
        verify(modelMapper).map(postgresInstance, DatabaseInstanceResponse.class);
    }

    @Test
    void getInstance_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(databaseInstanceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            databaseInstanceService.getInstance(999L);
        });

        verify(databaseInstanceRepository).findById(999L);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getUserInstances_Success() {
        // Given
        List<DatabaseInstance> instances = Arrays.asList(postgresInstance, mysqlInstance);
        when(databaseInstanceRepository.findByUserId(1L)).thenReturn(instances);
        when(modelMapper.map(postgresInstance, DatabaseInstanceResponse.class)).thenReturn(postgresInstanceResponse);
        when(modelMapper.map(mysqlInstance, DatabaseInstanceResponse.class)).thenReturn(mysqlInstanceResponse);

        // When
        List<DatabaseInstanceResponse> result = databaseInstanceService.getUserInstances(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(101L, result.get(1).getId());

        verify(databaseInstanceRepository).findByUserId(1L);
        verify(modelMapper, times(2)).map(any(DatabaseInstance.class), eq(DatabaseInstanceResponse.class));
    }

    @Test
    void getUserInstances_EmptyList_ReturnsEmptyList() {
        // Given
        when(databaseInstanceRepository.findByUserId(999L)).thenReturn(Arrays.asList());

        // When
        List<DatabaseInstanceResponse> result = databaseInstanceService.getUserInstances(999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(databaseInstanceRepository).findByUserId(999L);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void updateInstanceStatus_Success() {
        // Given
        DatabaseInstance updatedInstance = DatabaseInstance.builder()
                .id(100L)
                .user(user)
                .subscription(subscription)
                .databaseEngine(postgresEngine)
                .containerName("db-1-abc12345")
                .host("localhost")
                .port(5432)
                .status(InstanceStatus.RUNNING)
                .build();

        DatabaseInstanceResponse updatedResponse = new DatabaseInstanceResponse();
        updatedResponse.setId(100L);
        updatedResponse.setStatus(InstanceStatus.RUNNING);

        when(databaseInstanceRepository.findById(100L)).thenReturn(Optional.of(postgresInstance));
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenReturn(updatedInstance);
        when(modelMapper.map(updatedInstance, DatabaseInstanceResponse.class)).thenReturn(updatedResponse);

        // When
        DatabaseInstanceResponse result = databaseInstanceService.updateInstanceStatus(100L, InstanceStatus.RUNNING);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(InstanceStatus.RUNNING, result.getStatus());

        verify(databaseInstanceRepository).findById(100L);
        verify(databaseInstanceRepository).save(argThat(instance ->
            instance.getStatus() == InstanceStatus.RUNNING
        ));
        verify(modelMapper).map(updatedInstance, DatabaseInstanceResponse.class);
    }

    @Test
    void updateInstanceStatus_ToSuspended_Success() {
        // Given
        when(databaseInstanceRepository.findById(100L)).thenReturn(Optional.of(postgresInstance));
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenAnswer(invocation -> {
            DatabaseInstance instance = invocation.getArgument(0);
            assertEquals(InstanceStatus.SUSPENDED, instance.getStatus());
            return instance;
        });
        when(modelMapper.map(any(DatabaseInstance.class), eq(DatabaseInstanceResponse.class))).thenReturn(postgresInstanceResponse);

        // When
        databaseInstanceService.updateInstanceStatus(100L, InstanceStatus.SUSPENDED);

        // Then
        verify(databaseInstanceRepository).save(argThat(instance ->
            instance.getStatus() == InstanceStatus.SUSPENDED
        ));
    }

    @Test
    void updateInstanceStatus_ToDeleted_Success() {
        // Given
        when(databaseInstanceRepository.findById(100L)).thenReturn(Optional.of(postgresInstance));
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenAnswer(invocation -> {
            DatabaseInstance instance = invocation.getArgument(0);
            assertEquals(InstanceStatus.DELETED, instance.getStatus());
            return instance;
        });
        when(modelMapper.map(any(DatabaseInstance.class), eq(DatabaseInstanceResponse.class))).thenReturn(postgresInstanceResponse);

        // When
        databaseInstanceService.updateInstanceStatus(100L, InstanceStatus.DELETED);

        // Then
        verify(databaseInstanceRepository).save(argThat(instance ->
            instance.getStatus() == InstanceStatus.DELETED
        ));
    }

    @Test
    void updateInstanceStatus_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(databaseInstanceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            databaseInstanceService.updateInstanceStatus(999L, InstanceStatus.RUNNING);
        });

        verify(databaseInstanceRepository).findById(999L);
        verify(databaseInstanceRepository, never()).save(any());
    }
}
