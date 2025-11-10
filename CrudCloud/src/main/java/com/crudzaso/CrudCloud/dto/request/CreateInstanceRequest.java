package com.crudzaso.CrudCloud.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for database instance creation request
 * Validates input when creating a new database instance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstanceRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;

    /**
     * ID of the database engine to use
     * Examples: PostgreSQL (1), MySQL (2), MongoDB (3)
     * Required field
     */
    @NotNull(message = "Database Engine ID is required")
    private Long databaseEngineId;

    /**
     * Custom name for the database instance (optional)
     * If not provided, will be auto-generated
     */
    private String instanceName;
}
