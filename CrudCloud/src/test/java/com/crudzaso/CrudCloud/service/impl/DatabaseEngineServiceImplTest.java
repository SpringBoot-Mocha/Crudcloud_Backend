package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.crudzaso.CrudCloud.mapper.DatabaseEngineMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatabaseEngineServiceImpl using Mockito
 * Coverage: All database engine retrieval methods
 */
@ExtendWith(MockitoExtension.class)
class DatabaseEngineServiceImplTest {

    @Mock
    private DatabaseEngineRepository databaseEngineRepository;

    @Mock
    private DatabaseEngineMapper databaseEngineMapper;

    @InjectMocks
    private DatabaseEngineServiceImpl databaseEngineService;

    private DatabaseEngine postgresEngine;
    private DatabaseEngine mysqlEngine;
    private DatabaseEngine mongoEngine;
    private DatabaseEngineResponse postgresResponse;
    private DatabaseEngineResponse mysqlResponse;
    private DatabaseEngineResponse mongoResponse;

    @BeforeEach
    void setUp() {
        postgresEngine = DatabaseEngine.builder()
                .id(1L)
                .name("PostgreSQL")
                .version("14")
                .defaultPort(5432)
                .dockerImage("postgres:14")
                .description("PostgreSQL relational database")
                .build();

        mysqlEngine = DatabaseEngine.builder()
                .id(2L)
                .name("MySQL")
                .version("8.0")
                .defaultPort(3306)
                .dockerImage("mysql:8.0")
                .description("MySQL relational database")
                .build();

        mongoEngine = DatabaseEngine.builder()
                .id(3L)
                .name("MongoDB")
                .version("6.0")
                .defaultPort(27017)
                .dockerImage("mongo:6.0")
                .description("MongoDB NoSQL database")
                .build();

        postgresResponse = new DatabaseEngineResponse();
        postgresResponse.setId(1L);
        postgresResponse.setName("PostgreSQL");
        postgresResponse.setVersion("14");
        postgresResponse.setDefaultPort(5432);

        mysqlResponse = new DatabaseEngineResponse();
        mysqlResponse.setId(2L);
        mysqlResponse.setName("MySQL");
        mysqlResponse.setVersion("8.0");
        mysqlResponse.setDefaultPort(3306);

        mongoResponse = new DatabaseEngineResponse();
        mongoResponse.setId(3L);
        mongoResponse.setName("MongoDB");
        mongoResponse.setVersion("6.0");
        mongoResponse.setDefaultPort(27017);
    }

    @Test
    void getAllEngines_Success() {
        // Given
        List<DatabaseEngine> engines = Arrays.asList(postgresEngine, mysqlEngine, mongoEngine);
        List<DatabaseEngineResponse> expectedResponses = Arrays.asList(postgresResponse, mysqlResponse, mongoResponse);
        when(databaseEngineRepository.findAll()).thenReturn(engines);
        when(databaseEngineMapper.toResponseList(engines)).thenReturn(expectedResponses);

        // When
        List<DatabaseEngineResponse> result = databaseEngineService.getAllEngines();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("PostgreSQL", result.get(0).getName());
        assertEquals("MySQL", result.get(1).getName());
        assertEquals("MongoDB", result.get(2).getName());

        verify(databaseEngineRepository).findAll();
        verify(databaseEngineMapper).toResponseList(engines);
    }

    @Test
    void getAllEngines_EmptyList_ReturnsEmptyList() {
        // Given
        List<DatabaseEngine> emptyList = Arrays.asList();
        when(databaseEngineRepository.findAll()).thenReturn(emptyList);
        when(databaseEngineMapper.toResponseList(emptyList)).thenReturn(Arrays.asList());

        // When
        List<DatabaseEngineResponse> result = databaseEngineService.getAllEngines();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(databaseEngineRepository).findAll();
        verify(databaseEngineMapper).toResponseList(emptyList);
    }

    @Test
    void getAllEngines_SingleEngine_ReturnsOne() {
        // Given
        List<DatabaseEngine> singleEngineList = Arrays.asList(postgresEngine);
        List<DatabaseEngineResponse> expectedResponses = Arrays.asList(postgresResponse);
        when(databaseEngineRepository.findAll()).thenReturn(singleEngineList);
        when(databaseEngineMapper.toResponseList(singleEngineList)).thenReturn(expectedResponses);

        // When
        List<DatabaseEngineResponse> result = databaseEngineService.getAllEngines();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PostgreSQL", result.get(0).getName());
        assertEquals(5432, result.get(0).getDefaultPort());

        verify(databaseEngineRepository).findAll();
        verify(databaseEngineMapper).toResponseList(Arrays.asList(postgresEngine));
    }

    @Test
    void getEngineById_Success() {
        // Given
        when(databaseEngineRepository.findById(1L)).thenReturn(Optional.of(postgresEngine));
        when(databaseEngineMapper.toResponse(postgresEngine)).thenReturn(postgresResponse);

        // When
        DatabaseEngineResponse result = databaseEngineService.getEngineById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PostgreSQL", result.getName());
        assertEquals("14", result.getVersion());
        assertEquals(5432, result.getDefaultPort());

        verify(databaseEngineRepository).findById(1L);
        verify(databaseEngineMapper).toResponse(postgresEngine);
    }

    @Test
    void getEngineById_MySQLEngine_Success() {
        // Given
        when(databaseEngineRepository.findById(2L)).thenReturn(Optional.of(mysqlEngine));
        when(databaseEngineMapper.toResponse(mysqlEngine)).thenReturn(mysqlResponse);

        // When
        DatabaseEngineResponse result = databaseEngineService.getEngineById(2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("MySQL", result.getName());
        assertEquals("8.0", result.getVersion());
        assertEquals(3306, result.getDefaultPort());

        verify(databaseEngineRepository).findById(2L);
    }

    @Test
    void getEngineById_MongoDBEngine_Success() {
        // Given
        when(databaseEngineRepository.findById(3L)).thenReturn(Optional.of(mongoEngine));
        when(databaseEngineMapper.toResponse(mongoEngine)).thenReturn(mongoResponse);

        // When
        DatabaseEngineResponse result = databaseEngineService.getEngineById(3L);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("MongoDB", result.getName());
        assertEquals("6.0", result.getVersion());
        assertEquals(27017, result.getDefaultPort());

        verify(databaseEngineRepository).findById(3L);
    }

    @Test
    void getEngineById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(databaseEngineRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            databaseEngineService.getEngineById(999L);
        });

        assertTrue(exception.getMessage().contains("DatabaseEngine"));
        assertTrue(exception.getMessage().contains("id"));
        assertTrue(exception.getMessage().contains("999"));

        verify(databaseEngineRepository).findById(999L);
        verify(databaseEngineMapper, never()).toResponse(any());
    }

    @Test
    void getAllEngines_VerifyMapperCalledForEachEngine() {
        // Given
        List<DatabaseEngine> engines = Arrays.asList(postgresEngine, mysqlEngine);
        List<DatabaseEngineResponse> expectedResponses = Arrays.asList(postgresResponse, mysqlResponse);
        when(databaseEngineRepository.findAll()).thenReturn(engines);
        when(databaseEngineMapper.toResponseList(engines)).thenReturn(expectedResponses);

        // When
        List<DatabaseEngineResponse> result = databaseEngineService.getAllEngines();

        // Then
        assertEquals(2, result.size());
        verify(databaseEngineMapper).toResponseList(engines);
    }
}
