package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.CredentialRepository;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import com.crudzaso.CrudCloud.repository.DatabaseInstanceRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.CredentialEncryptionService;
import com.crudzaso.CrudCloud.service.DatabaseProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.crudzaso.CrudCloud.mapper.DatabaseInstanceMapper;

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
    private CredentialRepository credentialRepository;

    @Mock
    private DatabaseInstanceMapper databaseInstanceMapper;

    @Mock
    private DatabaseProvisioningService provisioningService;

    @Mock
    private CredentialEncryptionService encryptionService;

    @InjectMocks
    private DatabaseInstanceServiceImpl databaseInstanceService;

    private User user;
    private Plan plan;
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

        plan = Plan.builder()
                .id(1L)
                .name("Basic Plan")
                .maxInstances(5)
                .build();

        subscription = Subscription.builder()
                .id(10L)
                .user(user)
                .plan(plan)
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
    void createInstance_Success() throws DatabaseProvisioningService.ProvisioningException {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(encryptionService.generateSecurePassword()).thenReturn("securePassword123");
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenReturn(postgresInstance);
        when(databaseInstanceMapper.toResponse(postgresInstance)).thenReturn(postgresInstanceResponse);
        when(credentialRepository.save(any())).thenReturn(null); // Mock credential save

        // Mock provisioning service to return a valid result
        DatabaseProvisioningService.ProvisioningResult provisioningResult =
            new DatabaseProvisioningService.ProvisioningResult("localhost", 5432, "testuser", "securePassword123", "db-1-abc12345");
        when(provisioningService.provisionDatabase(any(DatabaseInstance.class), anyString(), anyString(), anyString()))
            .thenReturn(provisioningResult);

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
        verify(databaseInstanceRepository, times(2)).save(any(DatabaseInstance.class));
        verify(databaseInstanceMapper).toResponse(postgresInstance);
        verify(credentialRepository).save(any()); // Verify credential was saved
    }

    @Test
    void createInstance_WithCustomInstanceName_Success() throws DatabaseProvisioningService.ProvisioningException {
        // Given
        createRequest.setInstanceName("my-custom-db");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(encryptionService.generateSecurePassword()).thenReturn("securePassword123");
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenAnswer(invocation -> {
            DatabaseInstance instance = invocation.getArgument(0);
            // Check container name on first save (before provisioning)
            if (instance.getStatus() == InstanceStatus.CREATING) {
                assertEquals("my-custom-db", instance.getContainerName());
            }
            return postgresInstance;
        });
        when(databaseInstanceMapper.toResponse(any(DatabaseInstance.class))).thenReturn(postgresInstanceResponse);
        when(credentialRepository.save(any())).thenReturn(null); // Mock credential save

        // Mock provisioning service
        DatabaseProvisioningService.ProvisioningResult provisioningResult =
            new DatabaseProvisioningService.ProvisioningResult("localhost", 5432, "testuser", "securePassword123", "my-custom-db");
        when(provisioningService.provisionDatabase(any(DatabaseInstance.class), anyString(), anyString(), anyString()))
            .thenReturn(provisioningResult);

        // When
        DatabaseInstanceResponse result = databaseInstanceService.createInstance(createRequest);

        // Then
        assertNotNull(result);
        verify(databaseInstanceRepository).save(argThat(instance ->
            "my-custom-db".equals(instance.getContainerName())
        ));
        verify(credentialRepository).save(any()); // Verify credential was saved
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
    void createInstance_SetsCorrectDefaultValues() throws DatabaseProvisioningService.ProvisioningException {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(encryptionService.generateSecurePassword()).thenReturn("securePassword123");
        when(databaseInstanceRepository.save(any(DatabaseInstance.class))).thenAnswer(invocation -> {
            DatabaseInstance instance = invocation.getArgument(0);
            assertEquals(user, instance.getUser());
            assertEquals(subscription, instance.getSubscription());
            assertEquals(postgresEngine, instance.getDatabaseEngine());
            // Check values on first save (before provisioning)
            if (instance.getStatus() == InstanceStatus.CREATING) {
                assertEquals("localhost", instance.getHost());
                assertEquals(5432, instance.getPort());
                assertEquals(InstanceStatus.CREATING, instance.getStatus());
            }
            return postgresInstance;
        });
        when(databaseInstanceMapper.toResponse(any(DatabaseInstance.class))).thenReturn(postgresInstanceResponse);
        when(credentialRepository.save(any())).thenReturn(null); // Mock credential save

        // Mock provisioning service
        DatabaseProvisioningService.ProvisioningResult provisioningResult =
            new DatabaseProvisioningService.ProvisioningResult("localhost", 5432, "testuser", "securePassword123", "db-1-abc12345");
        when(provisioningService.provisionDatabase(any(DatabaseInstance.class), anyString(), anyString(), anyString()))
            .thenReturn(provisioningResult);

        // When
        databaseInstanceService.createInstance(createRequest);

        // Then
        verify(databaseInstanceRepository, times(2)).save(any(DatabaseInstance.class));
        verify(credentialRepository).save(any()); // Verify credential was saved
    }

    @Test
    void getInstance_Success() {
        // Given
        when(databaseInstanceRepository.findById(100L)).thenReturn(Optional.of(postgresInstance));
        when(databaseInstanceMapper.toResponse(postgresInstance)).thenReturn(postgresInstanceResponse);

        // When
        DatabaseInstanceResponse result = databaseInstanceService.getInstance(100L);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(InstanceStatus.CREATING, result.getStatus());

        verify(databaseInstanceRepository).findById(100L);
        verify(databaseInstanceMapper).toResponse(postgresInstance);
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
        verify(databaseInstanceMapper, never()).toResponse(any());
    }

    @Test
    void getUserInstances_Success() {
        // Given
        List<DatabaseInstance> instances = Arrays.asList(postgresInstance, mysqlInstance);
        List<DatabaseInstanceResponse> expectedResponses = Arrays.asList(postgresInstanceResponse, mysqlInstanceResponse);
        when(databaseInstanceRepository.findByUserId(1L)).thenReturn(instances);
        when(databaseInstanceMapper.toResponseList(instances)).thenReturn(expectedResponses);

        // When
        List<DatabaseInstanceResponse> result = databaseInstanceService.getUserInstances(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(101L, result.get(1).getId());

        verify(databaseInstanceRepository).findByUserId(1L);
        verify(databaseInstanceMapper).toResponseList(instances);
    }

    @Test
    void getUserInstances_EmptyList_ReturnsEmptyList() {
        // Given
        List<DatabaseInstance> emptyList = Arrays.asList();
        when(databaseInstanceRepository.findByUserId(999L)).thenReturn(emptyList);
        when(databaseInstanceMapper.toResponseList(emptyList)).thenReturn(Arrays.asList());

        // When
        List<DatabaseInstanceResponse> result = databaseInstanceService.getUserInstances(999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(databaseInstanceRepository).findByUserId(999L);
        verify(databaseInstanceMapper).toResponseList(emptyList);
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
        when(databaseInstanceMapper.toResponse(updatedInstance)).thenReturn(updatedResponse);

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
        verify(databaseInstanceMapper).toResponse(updatedInstance);
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
        when(databaseInstanceMapper.toResponse(any(DatabaseInstance.class))).thenReturn(postgresInstanceResponse);

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
        when(databaseInstanceMapper.toResponse(any(DatabaseInstance.class))).thenReturn(postgresInstanceResponse);

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
