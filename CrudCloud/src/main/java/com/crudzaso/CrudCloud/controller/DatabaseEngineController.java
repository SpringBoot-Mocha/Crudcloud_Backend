package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import com.crudzaso.CrudCloud.service.DatabaseEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Database engine endpoints for browsing available database options.
 *
 * Read-only endpoints that do not require authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/engines")
@RequiredArgsConstructor
@Tag(name = "Database Engines", description = "Browse available database engines (public endpoints)")
public class DatabaseEngineController {

    private final DatabaseEngineService databaseEngineService;

    /**
     * Get all available database engines.
     *
     * @return list of all database engines
     */
    @GetMapping
    @Operation(summary = "List all engines", description = "Retrieves all available database engines")
    public ResponseEntity<List<DatabaseEngineResponse>> getAllEngines() {
        log.info("Getting all database engines");
        List<DatabaseEngineResponse> engines = databaseEngineService.getAllEngines();
        return ResponseEntity.ok(engines);
    }

    /**
     * Get a specific database engine by ID.
     *
     * @param id the engine ID
     * @return database engine response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get engine details", description = "Retrieves details of a specific database engine")
    public ResponseEntity<DatabaseEngineResponse> getEngineById(@PathVariable Long id) {
        log.info("Getting database engine with ID: {}", id);
        DatabaseEngineResponse engine = databaseEngineService.getEngineById(id);
        return ResponseEntity.ok(engine);
    }
}
