package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;

import java.util.List;

/**
 * Service interface for database engine operations.
 */
public interface DatabaseEngineService {

    /**
     * Get all available database engines.
     *
     * @return list of all database engines
     */
    List<DatabaseEngineResponse> getAllEngines();

    /**
     * Get a specific database engine by ID.
     *
     * @param id the engine ID
     * @return database engine response
     */
    DatabaseEngineResponse getEngineById(Long id);
}
