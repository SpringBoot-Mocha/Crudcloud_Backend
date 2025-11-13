package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.DatabaseEngineMapper;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import com.crudzaso.CrudCloud.service.DatabaseEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of DatabaseEngineService.
 *
 * Handles retrieval of available database engines.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseEngineServiceImpl implements DatabaseEngineService {

    private final DatabaseEngineRepository databaseEngineRepository;
    private final DatabaseEngineMapper databaseEngineMapper;

    /**
     * Get all available database engines.
     *
     * @return list of all database engines
     */
    @Override
    public List<DatabaseEngineResponse> getAllEngines() {
        log.info("Fetching all database engines");
        return databaseEngineMapper.toResponseList(databaseEngineRepository.findAll());
    }

    /**
     * Get a specific database engine by ID.
     *
     * @param id the engine ID
     * @return database engine response
     * @throws ResourceNotFoundException if engine not found
     */
    @Override
    public DatabaseEngineResponse getEngineById(Long id) {
        log.info("Fetching database engine with ID: {}", id);
        return databaseEngineRepository.findById(id)
                .map(databaseEngineMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("DatabaseEngine", "id", id));
    }
}
