package com.crudzaso.CrudCloud.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for database instance creation request
 * Validates input when creating a new database instance
 *
 * Note: User ID is extracted from the JWT token in the Authorization header.
 * The userId field is set by the controller, not provided by the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstanceRequest {

    /**
     * User ID - extracted from JWT token by the controller
     * Not provided in the request body by the client
     */
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
