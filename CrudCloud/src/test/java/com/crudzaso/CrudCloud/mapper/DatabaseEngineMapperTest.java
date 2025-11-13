package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseEngineMapper using Spring Boot Test
 * Tests the actual MapStruct implementation with Spring context
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class DatabaseEngineMapperTest {

    @Autowired
    private DatabaseEngineMapper databaseEngineMapper;

    private DatabaseEngine databaseEngine;

    @BeforeEach
    void setUp() {
        // Setup test entity
        databaseEngine = DatabaseEngine.builder()
                .id(1L)
                .name("PostgreSQL")
                .version("15.0")
                .description("Advanced open-source relational database")
                .build();
    }

    @Test
    void toResponse_ValidDatabaseEngine_ReturnsCorrectResponse() {
        // When
        DatabaseEngineResponse response = databaseEngineMapper.toResponse(databaseEngine);

        // Then
        assertNotNull(response);
        assertEquals(databaseEngine.getId(), response.getId());
        assertEquals(databaseEngine.getName(), response.getName());
        assertEquals(databaseEngine.getVersion(), response.getVersion());
        assertEquals(databaseEngine.getDescription(), response.getDescription());
    }

    @Test
    void toResponse_NullDatabaseEngine_ReturnsNull() {
        // When
        DatabaseEngineResponse response = databaseEngineMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toResponse_DatabaseEngineWithNullFields_ReturnsResponseWithNullFields() {
        // Given
        DatabaseEngine engineWithNullFields = DatabaseEngine.builder()
                .id(2L)
                .name("MySQL")
                .version(null)
                .description(null)
                .build();

        // When
        DatabaseEngineResponse response = databaseEngineMapper.toResponse(engineWithNullFields);

        // Then
        assertNotNull(response);
        assertEquals(engineWithNullFields.getId(), response.getId());
        assertEquals(engineWithNullFields.getName(), response.getName());
        assertNull(response.getVersion());
        assertNull(response.getDescription());
    }

    @Test
    void toResponse_DatabaseEngineWithEmptyFields_ReturnsResponseWithEmptyFields() {
        // Given
        DatabaseEngine engineWithEmptyFields = DatabaseEngine.builder()
                .id(3L)
                .name("")
                .version("")
                .description("")
                .build();

        // When
        DatabaseEngineResponse response = databaseEngineMapper.toResponse(engineWithEmptyFields);

        // Then
        assertNotNull(response);
        assertEquals(engineWithEmptyFields.getId(), response.getId());
        assertEquals(engineWithEmptyFields.getName(), response.getName());
        assertEquals(engineWithEmptyFields.getVersion(), response.getVersion());
        assertEquals(engineWithEmptyFields.getDescription(), response.getDescription());
    }

    @Test
    void toResponseList_ValidDatabaseEngines_ReturnsCorrectResponseList() {
        // Given
        DatabaseEngine engine2 = DatabaseEngine.builder()
                .id(4L)
                .name("MySQL")
                .version("8.0")
                .description("Popular open-source relational database")
                .build();

        DatabaseEngine engine3 = DatabaseEngine.builder()
                .id(5L)
                .name("MongoDB")
                .version("7.0")
                .description("Document-oriented NoSQL database")
                .build();

        List<DatabaseEngine> databaseEngines = Arrays.asList(databaseEngine, engine2, engine3);

        // When
        List<DatabaseEngineResponse> responses = databaseEngineMapper.toResponseList(databaseEngines);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        // Verify first engine
        DatabaseEngineResponse response1 = responses.get(0);
        assertEquals(databaseEngine.getId(), response1.getId());
        assertEquals(databaseEngine.getName(), response1.getName());
        assertEquals(databaseEngine.getVersion(), response1.getVersion());

        // Verify second engine
        DatabaseEngineResponse response2 = responses.get(1);
        assertEquals(engine2.getId(), response2.getId());
        assertEquals(engine2.getName(), response2.getName());
        assertEquals(engine2.getVersion(), response2.getVersion());

        // Verify third engine
        DatabaseEngineResponse response3 = responses.get(2);
        assertEquals(engine3.getId(), response3.getId());
        assertEquals(engine3.getName(), response3.getName());
        assertEquals(engine3.getVersion(), response3.getVersion());
    }

    @Test
    void toResponseList_EmptyList_ReturnsEmptyList() {
        // Given
        List<DatabaseEngine> emptyList = Arrays.asList();

        // When
        List<DatabaseEngineResponse> responses = databaseEngineMapper.toResponseList(emptyList);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void toResponseList_NullList_ReturnsNull() {
        // When
        List<DatabaseEngineResponse> responses = databaseEngineMapper.toResponseList(null);

        // Then
        assertNull(responses);
    }

}