package com.crudzaso.CrudCloud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning database credentials to the user.
 *
 * IMPORTANT: Credentials are returned ONLY ONCE at creation time.
 * After this, the password is encrypted in the database and cannot be retrieved.
 * Users must save this information immediately or request a password rotation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialResponse {

    /**
     * Username for database access
     */
    private String username;

    /**
     * Database access password
     * IMPORTANT: This is shown ONLY ONCE at creation time.
     * User must save this immediately. If lost, password must be rotated.
     */
    private String password;

    /**
     * Database name
     */
    private String databaseName;

    /**
     * Host/IP address of the database server
     * E.g., 91.98.225.17
     */
    private String host;

    /**
     * Port number of the database server
     * E.g., 5432 for PostgreSQL, 3306 for MySQL, etc.
     */
    private Integer port;

    /**
     * Connection string for quick reference
     * Format: [engine]://[username]@[host]:[port]/[databaseName]
     */
    private String connectionString;

    /**
     * Sample connection commands for different clients
     */
    private String sampleConnections;
}
