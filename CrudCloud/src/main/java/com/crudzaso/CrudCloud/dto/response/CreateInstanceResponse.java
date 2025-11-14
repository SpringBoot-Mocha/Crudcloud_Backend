package com.crudzaso.CrudCloud.dto.response;

import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for the response when creating a database instance.
 *
 * This response includes:
 * 1. The created database instance information
 * 2. The credentials for accessing the database (ONLY ONCE)
 *
 * IMPORTANT: Credentials are shown ONLY in this response.
 * Subsequent GET requests will NOT return the plaintext password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInstanceResponse {

    // ========== INSTANCE INFORMATION ==========

    /**
     * Unique identifier of the database instance
     */
    private Long id;

    /**
     * User ID who owns this instance
     */
    private Long userId;

    /**
     * Subscription ID associated with this instance
     */
    private Long subscriptionId;

    /**
     * Database engine ID (PostgreSQL, MySQL, MongoDB, etc.)
     */
    private Long databaseEngineId;

    /**
     * Engine name for reference
     */
    private String engineName;

    /**
     * Container name on the VPS
     */
    private String containerName;

    /**
     * Host/IP of the VPS where the database is running
     */
    private String host;

    /**
     * Port number for connection
     */
    private Integer port;

    /**
     * Current status of the instance (CREATING, RUNNING, SUSPENDED, DELETED)
     */
    private InstanceStatus status;

    /**
     * When the instance was created
     */
    private LocalDateTime createdAt;

    // ========== CREDENTIALS (ONLY THIS RESPONSE) ==========

    /**
     * Database credentials
     * IMPORTANT: This field is ONLY present in the create response.
     * Subsequent GET requests will NOT include the plaintext password.
     */
    private CredentialResponse credentials;

    /**
     * Warning message to user about password security
     */
    private String warning;
}
