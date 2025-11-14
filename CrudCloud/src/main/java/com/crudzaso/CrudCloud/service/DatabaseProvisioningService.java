package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;

/**
 * Service for provisioning database instances on the remote VPS.
 *
 * Handles:
 * - Creating actual Docker containers
 * - Creating databases inside containers
 * - Creating database users with permissions
 * - Retrieving connection credentials
 *
 * IMPORTANT: This service connects to VPS at 91.98.225.17 via SSH
 */
public interface DatabaseProvisioningService {

    /**
     * Provision a database instance on the remote VPS.
     *
     * Process:
     * 1. Connect to VPS via SSH
     * 2. Create Docker container for the database engine
     * 3. Wait for container to be ready
     * 4. Create database inside container
     * 5. Create user with permissions
     * 6. Return connection credentials
     *
     * @param instance The DatabaseInstance entity to provision
     * @param databaseName The name of the database to create
     * @param username The username for database access
     * @param password The password for database access
     * @return ProvisioningResult with host, port, credentials
     * @throws ProvisioningException if any step fails
     */
    ProvisioningResult provisionDatabase(
        DatabaseInstance instance,
        String databaseName,
        String username,
        String password
    ) throws ProvisioningException;

    /**
     * Delete/destroy a database instance on the remote VPS.
     *
     * @param instance The DatabaseInstance to delete
     * @throws ProvisioningException if deletion fails
     */
    void deleteDatabase(DatabaseInstance instance) throws ProvisioningException;

    /**
     * Result of successful database provisioning
     */
    class ProvisioningResult {
        public final String host;          // e.g., 91.98.225.17
        public final int port;              // e.g., 5432
        public final String username;       // Database user
        public final String password;       // Database password
        public final String containerName;  // Docker container name

        public ProvisioningResult(String host, int port, String username, String password, String containerName) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.containerName = containerName;
        }
    }

    /**
     * Exception for provisioning failures
     */
    class ProvisioningException extends Exception {
        public ProvisioningException(String message) {
            super(message);
        }

        public ProvisioningException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
