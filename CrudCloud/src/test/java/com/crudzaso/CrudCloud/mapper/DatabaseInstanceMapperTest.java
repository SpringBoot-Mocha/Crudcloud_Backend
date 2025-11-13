package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseInstanceMapper using Spring Boot Test
 * Tests the actual MapStruct implementation with Spring context
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class DatabaseInstanceMapperTest {

    @Autowired
    private DatabaseInstanceMapper databaseInstanceMapper;

    private DatabaseInstance databaseInstance;
    private User user;
    private Subscription subscription;
    private DatabaseEngine databaseEngine;

    @BeforeEach
    void setUp() {
        // Setup test entities
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        subscription = Subscription.builder()
                .id(2L)
                .isActive(true)
                .build();

        databaseEngine = DatabaseEngine.builder()
                .id(3L)
                .name("PostgreSQL")
                .version("15.0")
                .build();

        databaseInstance = DatabaseInstance.builder()
                .id(10L)
                .containerName("test-database-instance")
                .host("localhost")
                .port(5432)
                .status(com.crudzaso.CrudCloud.domain.enums.InstanceStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .subscription(subscription)
                .databaseEngine(databaseEngine)
                .build();
    }

    @Test
    void toResponse_ValidDatabaseInstance_ReturnsCorrectResponse() {
        // When
        DatabaseInstanceResponse response = databaseInstanceMapper.toResponse(databaseInstance);

        // Then
        assertNotNull(response);
        assertEquals(databaseInstance.getId(), response.getId());
        assertEquals(databaseInstance.getContainerName(), response.getContainerName());
        assertEquals(databaseInstance.getHost(), response.getHost());
        assertEquals(databaseInstance.getPort(), response.getPort());
        assertEquals(databaseInstance.getStatus(), response.getStatus());
        assertEquals(databaseInstance.getCreatedAt(), response.getCreatedAt());
        assertEquals(databaseInstance.getUpdatedAt(), response.getUpdatedAt());

        // Verify relationship mappings
        assertEquals(user.getId(), response.getUserId());
        assertEquals(subscription.getId(), response.getSubscriptionId());
        assertEquals(databaseEngine.getId(), response.getDatabaseEngine());
    }

    @Test
    void toResponse_NullDatabaseInstance_ReturnsNull() {
        // When
        DatabaseInstanceResponse response = databaseInstanceMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toResponse_DatabaseInstanceWithNullRelationships_ReturnsResponseWithNullIds() {
        // Given
        DatabaseInstance instanceWithoutRelationships = DatabaseInstance.builder()
                .id(20L)
                .containerName("database-without-relationships")
                .host("localhost")
                .port(5432)
                .status(com.crudzaso.CrudCloud.domain.enums.InstanceStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(null)
                .subscription(null)
                .databaseEngine(null)
                .build();

        // When
        DatabaseInstanceResponse response = databaseInstanceMapper.toResponse(instanceWithoutRelationships);

        // Then
        assertNotNull(response);
        assertEquals(instanceWithoutRelationships.getId(), response.getId());
        assertEquals(instanceWithoutRelationships.getContainerName(), response.getContainerName());
        assertNull(response.getUserId());
        assertNull(response.getSubscriptionId());
        assertNull(response.getDatabaseEngine());
    }

    @Test
    void toResponseList_ValidDatabaseInstances_ReturnsCorrectResponseList() {
        // Given
        DatabaseInstance instance2 = DatabaseInstance.builder()
                .id(11L)
                .containerName("test-database-instance-2")
                .host("localhost")
                .port(5433)
                .status(com.crudzaso.CrudCloud.domain.enums.InstanceStatus.RUNNING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .subscription(subscription)
                .databaseEngine(databaseEngine)
                .build();

        List<DatabaseInstance> databaseInstances = Arrays.asList(databaseInstance, instance2);

        // When
        List<DatabaseInstanceResponse> responses = databaseInstanceMapper.toResponseList(databaseInstances);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());

        // Verify first instance
        DatabaseInstanceResponse response1 = responses.get(0);
        assertEquals(databaseInstance.getId(), response1.getId());
        assertEquals(databaseInstance.getContainerName(), response1.getContainerName());
        assertEquals(user.getId(), response1.getUserId());

        // Verify second instance
        DatabaseInstanceResponse response2 = responses.get(1);
        assertEquals(instance2.getId(), response2.getId());
        assertEquals(instance2.getContainerName(), response2.getContainerName());
        assertEquals(user.getId(), response2.getUserId());
    }

    @Test
    void toResponseList_EmptyList_ReturnsEmptyList() {
        // Given
        List<DatabaseInstance> emptyList = Arrays.asList();

        // When
        List<DatabaseInstanceResponse> responses = databaseInstanceMapper.toResponseList(emptyList);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void toResponseList_NullList_ReturnsNull() {
        // When
        List<DatabaseInstanceResponse> responses = databaseInstanceMapper.toResponseList(null);

        // Then
        assertNull(responses);
    }
}