package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import com.crudzaso.CrudCloud.service.DatabaseEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    private final ModelMapper modelMapper;

    /**
     * Get all available database engines.
     *
     * @return list of all database engines
     */
    @Override
    public List<DatabaseEngineResponse> getAllEngines() {
        log.info("Fetching all database engines");
        return databaseEngineRepository.findAll()
                .stream()
                .map(engine -> modelMapper.map(engine, DatabaseEngineResponse.class))
                .collect(Collectors.toList());
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
                .map(engine -> modelMapper.map(engine, DatabaseEngineResponse.class))
                .orElseThrow(() -> new ResourceNotFoundException("DatabaseEngine", "id", id));
    }
}
