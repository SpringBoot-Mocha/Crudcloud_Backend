package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import com.crudzaso.CrudCloud.service.DatabaseInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Database instance management endpoints.
 *
 * Provides endpoints for creating and managing database instances.
 * Users can only access/manage their own instances.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/instances")
@RequiredArgsConstructor
@Tag(name = "Database Instances", description = "Database instance management endpoints")
public class DatabaseInstanceController {

    private final DatabaseInstanceService databaseInstanceService;

    /**
     * Create a new database instance.
     *
     * @param request the instance creation request
     * @return created instance response with 201 status
     */
    @PostMapping
    @Operation(summary = "Create database instance", description = "Creates a new database instance for the user")
    public ResponseEntity<DatabaseInstanceResponse> createInstance(@Valid @RequestBody CreateInstanceRequest request) {
        log.info("Creating database instance for user: {}", request.getUserId());
        DatabaseInstanceResponse response = databaseInstanceService.createInstance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a specific database instance.
     *
     * @param id the instance ID
     * @return instance details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get instance details", description = "Retrieves details of a specific database instance")
    public ResponseEntity<DatabaseInstanceResponse> getInstance(@PathVariable Long id) {
        log.info("Getting database instance with ID: {}", id);
        DatabaseInstanceResponse response = databaseInstanceService.getInstance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List all instances for the authenticated user.
     *
     * @param userId the user ID to get instances for
     * @return list of instances
     */
    @GetMapping
    @Operation(summary = "List user instances", description = "Retrieves all database instances for the authenticated user")
    public ResponseEntity<List<DatabaseInstanceResponse>> getUserInstances(
            @RequestParam(required = false) Long userId) {
        log.info("Getting database instances for user: {}", userId);
        List<DatabaseInstanceResponse> response = databaseInstanceService.getUserInstances(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a database instance.
     *
     * @param id the instance ID to delete
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete instance", description = "Deletes a database instance")
    public ResponseEntity<Void> deleteInstance(@PathVariable Long id) {
        log.info("Deleting database instance with ID: {}", id);
        // Implementation will depend on service layer delete capability
        return ResponseEntity.noContent().build();
    }
}
