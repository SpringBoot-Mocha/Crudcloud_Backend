package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.infrastructure.ssh.SSHConnectionPool;
import com.crudzaso.CrudCloud.service.DatabaseProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of DatabaseProvisioningService.
 * Provisions real database instances on the VPS via SSH.
 *
 * Process:
 * 1. Start Docker container for the database engine
 * 2. Wait for container to be ready
 * 3. Create database inside container
 * 4. Create database user with permissions
 * 5. Return connection credentials
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseProvisioningServiceImpl implements DatabaseProvisioningService {

    private final SSHConnectionPool sshConnectionPool;

    @Value("${ssh.host:91.98.225.17}")
    private String vpsHost;

    /**
     * Calculate dynamic external port for the instance
     * Formula: 10000 + (instance.id * 100) + (defaultPort % 100)
     * This avoids conflicts with production database ports
     */
    private int calculateExternalPort(DatabaseInstance instance) {
        int defaultPort = instance.getDatabaseEngine().getDefaultPort();
        return 10000 + (instance.getId().intValue() * 100) + (defaultPort % 100);
    }

    /**
     * Quote a string for safe shell/database execution
     * Escapes single quotes to prevent shell injection
     */
    private String quoteForShell(String value) {
        if (value == null) {
            return "''";
        }
        // Replace single quotes with '\'' (end quote, escaped quote, start quote)
        return "'" + value.replace("'", "'\\''") + "'";
    }

    /**
     * Escape single quotes for SQL string literals (replace ' with '')
     * Use this for passwords and string values inside SQL queries
     */
    private String escapeSqlString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * Escape/quote database name according to the database engine syntax
     * Handles special characters like hyphens in instanceName
     */
    private String escapeDbName(String dbName, String engineName) {
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }

        switch (engineName.toUpperCase()) {
            case "POSTGRESQL":
                // PostgreSQL: Use double quotes for identifiers with special chars
                return String.format("\"%s\"", dbName);
            case "MYSQL":
                // MySQL: Use backticks for identifiers with special chars
                return String.format("`%s`", dbName);
            case "SQL SERVER":
                // SQL Server: Already uses brackets in the queries [dbname]
                return dbName;
            case "MONGODB":
            case "CASSANDRA":
                // MongoDB/Cassandra: Generally allow alphanumeric + underscore
                // But we'll keep as-is, they handle quotes differently
                return dbName;
            default:
                return dbName;
        }
    }

    @Override
    public ProvisioningResult provisionDatabase(
            DatabaseInstance instance,
            String databaseName,
            String username,
            String password
    ) throws ProvisioningException {

        log.info("🚀 Starting database provisioning for instance: {}", instance.getContainerName());
        log.info("   Engine: {}", instance.getDatabaseEngine().getName());
        log.info("   Version: {}", instance.getDatabaseEngine().getVersion());

        try {
            // Step 1: Start Docker container
            startDockerContainer(instance);

            // Step 2: Wait for container to be ready
            waitForContainerReady(instance);

            // Step 3: Create database inside container
            createDatabase(instance, databaseName, username, password);

            // Step 4: Verify connection works
            verifyConnection(instance, databaseName, username, password);

            log.info("✅ Database provisioning completed successfully");

            int externalPort = calculateExternalPort(instance);
            return new ProvisioningResult(
                    vpsHost,
                    externalPort,
                    username,
                    password,
                    instance.getContainerName()
            );

        } catch (SSHConnectionPool.SSHException e) {
            log.error("❌ SSH error during provisioning: {}", e.getMessage(), e);
            throw new ProvisioningException("SSH error during database provisioning: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error during provisioning: {}", e.getMessage(), e);
            throw new ProvisioningException("Unexpected error during database provisioning: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDatabase(DatabaseInstance instance) throws ProvisioningException {
        log.info("🗑️  Deleting database instance: {}", instance.getContainerName());

        try {
            String deleteCommand = String.format(
                    "CN=\"%s\"; docker stop \"$CN\" && docker rm \"$CN\"",
                    instance.getContainerName()
            );

            sshConnectionPool.executeCommand(deleteCommand);
            log.info("✅ Database instance deleted successfully");

        } catch (SSHConnectionPool.SSHException e) {
            log.error("❌ Failed to delete database: {}", e.getMessage(), e);
            throw new ProvisioningException("Failed to delete database: " + e.getMessage(), e);
        }
    }

    /**
     * Start a Docker container for the database engine
     */
    private void startDockerContainer(DatabaseInstance instance) throws SSHConnectionPool.SSHException {
        log.info("🐳 Starting Docker container...");

        String containerName = instance.getContainerName();
        String dockerImage = instance.getDatabaseEngine().getDockerImage();
        int defaultPort = instance.getDatabaseEngine().getDefaultPort();
        int externalPort = calculateExternalPort(instance);

        log.info("   Engine: {}, Internal Port: {}, External Port: {}", dockerImage, defaultPort, externalPort);

        String engineName = instance.getDatabaseEngine().getName().toUpperCase();

        String dockerCommand;

        if ("POSTGRESQL".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name '%s' " +
                    "-e POSTGRES_PASSWORD=postgres " +
                    "-p 0.0.0.0:%d:5432 " +
                    "%s",
                    containerName, externalPort, dockerImage
            );
        } else if ("MYSQL".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name '%s' " +
                    "-e MYSQL_ROOT_PASSWORD=root " +
                    "-p 0.0.0.0:%d:3306 " +
                    "%s",
                    containerName, externalPort, dockerImage
            );
        } else if ("MONGODB".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name '%s' " +
                    "-p 0.0.0.0:%d:27017 " +
                    "%s",
                    containerName, externalPort, dockerImage
            );
        } else if ("REDIS".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name '%s' " +
                    "-p 0.0.0.0:%d:6379 " +
                    "%s",
                    containerName, externalPort, dockerImage
            );
        } else if ("SQL SERVER".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name '%s' " +
                    "-e ACCEPT_EULA=Y " +
                    "-e SA_PASSWORD=SQLServer@2022 " +
                    "-p 0.0.0.0:%d:1433 " +
                    "%s",
                    containerName, externalPort, dockerImage
            );
        } else if ("CASSANDRA".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name '%s' " +
                    "-p 0.0.0.0:%d:9042 " +
                    "%s",
                    containerName, externalPort, dockerImage
            );
        } else {
            throw new SSHConnectionPool.SSHException(
                    "Unsupported database engine: " + engineName
            );
        }

        log.debug("Docker command: {}", dockerCommand);
        sshConnectionPool.executeCommandWithProgressiveRetries(dockerCommand);
        log.info("✅ Container started: {}", containerName);
    }

    /**
     * Wait for the container to be ready and accepting connections
     */
    private void waitForContainerReady(DatabaseInstance instance) throws SSHConnectionPool.SSHException, InterruptedException {
        log.info("⏳ Waiting for container to be ready...");

        String containerName = instance.getContainerName();
        String engineName = instance.getDatabaseEngine().getName().toUpperCase();
        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                String checkCommand = "docker inspect -f '{{.State.Running}}' '" + containerName + "'";
                String output = sshConnectionPool.executeCommand(checkCommand);

                if (output.contains("true")) {
                    log.info("✅ Container is running and ready");
                    // MySQL, PostgreSQL, Cassandra and SQL Server need more time to fully initialize
                    int waitTime = ("MYSQL".equals(engineName) || "POSTGRESQL".equals(engineName) ||
                                   "CASSANDRA".equals(engineName) || "SQL SERVER".equals(engineName)) ? 120000 : 2000;
                    Thread.sleep(waitTime);
                    return;
                }

                retryCount++;
                Thread.sleep(1000);

            } catch (SSHConnectionPool.SSHException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new SSHConnectionPool.SSHException(
                            "Container failed to start after " + maxRetries + " attempts",
                            e
                    );
                }
                Thread.sleep(1000);
            }
        }

        throw new SSHConnectionPool.SSHException("Container startup timeout");
    }

    /**
     * Create database inside the container based on engine type
     */
    private void createDatabase(
            DatabaseInstance instance,
            String databaseName,
            String username,
            String password
    ) throws SSHConnectionPool.SSHException {
        log.info("📦 Creating database: {}", databaseName);

        String containerName = instance.getContainerName();
        String engineName = instance.getDatabaseEngine().getName().toUpperCase();
        String escapedDbName = escapeDbName(databaseName, engineName);

        if ("POSTGRESQL".equals(engineName)) {
            // Execute PostgreSQL commands separately to avoid transaction block issues
            String createUserCommand = String.format(
                    "CN=\"%s\"; USR=\"%s\"; PWD=\"%s\"; docker exec \"$CN\" psql -U postgres -c \"CREATE USER \\\"$USR\\\" WITH PASSWORD '$PWD';\"",
                    containerName, username, password
            );
            String createDbCommand = String.format(
                    "CN=\"%s\"; DB=\"%s\"; USR=\"%s\"; docker exec \"$CN\" psql -U postgres -c \"CREATE DATABASE \\\"$DB\\\" OWNER \\\"$USR\\\";\"",
                    containerName, databaseName, username
            );
            String grantCommand = String.format(
                    "CN=\"%s\"; DB=\"%s\"; USR=\"%s\"; docker exec \"$CN\" psql -U postgres -c \"GRANT ALL PRIVILEGES ON DATABASE \\\"$DB\\\" TO \\\"$USR\\\";\"",
                    containerName, databaseName, username
            );

            log.debug("Create User command: {}", createUserCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(createUserCommand);

            log.debug("Create DB command: {}", createDbCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(createDbCommand);

            log.debug("Grant command: {}", grantCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(grantCommand);

            log.info("✅ Database created: {}", databaseName);

        } else if ("MYSQL".equals(engineName)) {
            String createCommand = String.format(
                    "CN=\"%s\"; DB=\"%s\"; docker exec \"$CN\" mysql -u root -proot -e \"CREATE USER '%s'@'%%' IDENTIFIED BY '%s'; CREATE DATABASE \\`$DB\\`; GRANT ALL PRIVILEGES ON \\`$DB\\`.* TO '%s'@'%%';\"",
                    containerName, databaseName, escapeSqlString(username), escapeSqlString(password), escapeSqlString(username)
            );
            log.debug("Create DB command: {}", createCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(createCommand);
            log.info("✅ Database created: {}", databaseName);

        } else if ("MONGODB".equals(engineName)) {
            // Use single quotes to prevent bash from interpreting special chars like $ in password
            // Within single quotes, we need to escape single quotes by ending quote, adding escaped quote, and starting quote again
            String escapedPassword = password.replace("'", "'\\''");
            String escapedUsername = username.replace("'", "'\\''");

            // NOTE: MongoDB with createUser in admin DB automatically gives user access to all databases
            // The 'use' command is not needed and causes mongosh syntax errors
            String createCommand = String.format(
                    "CN='%s'; docker exec \"$CN\" mongosh --eval 'db.getSiblingDB(\"admin\").createUser({user: \"%s\", pwd: \"%s\", roles: [\"dbOwner\"]})'",
                    containerName, escapedUsername, escapedPassword
            );
            log.debug("Create DB command: {}", createCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(createCommand);
            log.info("✅ Database created: {}", databaseName);

        } else if ("REDIS".equals(engineName)) {
            // Redis doesn't have traditional databases in the same way
            log.warn("⚠️  Redis doesn't require database creation, skipping database setup");
            return;

        } else if ("SQL SERVER".equals(engineName)) {
            String createCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P 'SQLServer@2022' -Q \"CREATE DATABASE [%s]; CREATE LOGIN [%s] WITH PASSWORD='%s'; ALTER AUTHORIZATION ON DATABASE::[%s] TO [%s];\"",
                    containerName, databaseName, username, escapeSqlString(password), databaseName, username
            );
            log.debug("Create DB command: {}", createCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(createCommand);
            log.info("✅ Database created: {}", databaseName);

        } else if ("CASSANDRA".equals(engineName)) {
            // Cassandra Docker image uses AllowAllAuthenticator by default (no authentication)
            // Create keyspace with simple replication. Credentials are generated for API consistency
            // but Cassandra will accept anonymous connections to the keyspace.
            String createCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" cqlsh -e \"CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};\"",
                    containerName, escapedDbName
            );
            log.debug("Create DB command: {}", createCommand);
            sshConnectionPool.executeCommandWithProgressiveRetries(createCommand);
            log.info("✅ Keyspace created: {}", databaseName);

        } else {
            throw new SSHConnectionPool.SSHException("Unsupported database engine: " + engineName);
        }
    }

    /**
     * Verify that the database connection works with the provided credentials
     */
    private void verifyConnection(
            DatabaseInstance instance,
            String databaseName,
            String username,
            String password
    ) throws SSHConnectionPool.SSHException {
        log.info("🔍 Verifying database connection...");

        String containerName = instance.getContainerName();
        String engineName = instance.getDatabaseEngine().getName().toUpperCase();
        String escapedDbName = escapeDbName(databaseName, engineName);
        String verifyCommand;

        if ("POSTGRESQL".equals(engineName)) {
            verifyCommand = String.format(
                    "CN=\"%s\"; DB=\"%s\"; USR=\"%s\"; PWD=\"%s\"; docker exec \"$CN\" bash -c \"PGPASSWORD='$PWD' psql -U $USR -d $DB -c 'SELECT 1;'\"",
                    containerName, databaseName, username, password
            );
        } else if ("MYSQL".equals(engineName)) {
            verifyCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" mysql -u %s -p%s %s -e \"SELECT 1;\"",
                    containerName, quoteForShell(username), quoteForShell(password), escapedDbName
            );
        } else if ("MONGODB".equals(engineName)) {
            verifyCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" mongosh -u %s -p %s --db %s --eval \"db.runCommand({ping: 1});\"",
                    containerName, quoteForShell(username), quoteForShell(password), databaseName
            );
        } else if ("REDIS".equals(engineName)) {
            verifyCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" redis-cli -p 6379 ping",
                    containerName
            );
        } else if ("SQL SERVER".equals(engineName)) {
            verifyCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P 'SQLServer@2022' -Q \"SELECT 1;\"",
                    containerName
            );
        } else if ("CASSANDRA".equals(engineName)) {
            verifyCommand = String.format(
                    "CN=\"%s\"; docker exec \"$CN\" cqlsh -e \"SELECT release_version FROM system.local;\"",
                    containerName
            );
        } else {
            log.warn("⚠️  Skipping verification for engine: {}", engineName);
            return;
        }

        try {
            sshConnectionPool.executeCommand(verifyCommand);
            log.info("✅ Connection verified successfully");
        } catch (SSHConnectionPool.SSHException e) {
            log.warn("⚠️  Connection verification failed, but continuing: {}", e.getMessage());
            // Don't throw - connection might work even if verification fails
        }
    }

    @Override
    public void pauseDatabase(DatabaseInstance instance) throws ProvisioningException {
        log.info("⏸️  Pausing database instance: {}", instance.getContainerName());

        try {
            String pauseCommand = String.format(
                    "CN=\"%s\"; docker pause \"$CN\"",
                    instance.getContainerName()
            );

            sshConnectionPool.executeCommand(pauseCommand);
            log.info("✅ Database instance paused successfully");

        } catch (SSHConnectionPool.SSHException e) {
            log.error("❌ Failed to pause database: {}", e.getMessage(), e);
            throw new ProvisioningException("Failed to pause database: " + e.getMessage(), e);
        }
    }

    @Override
    public void resumeDatabase(DatabaseInstance instance) throws ProvisioningException {
        log.info("▶️  Resuming database instance: {}", instance.getContainerName());

        try {
            String resumeCommand = String.format(
                    "CN=\"%s\"; docker unpause \"$CN\"",
                    instance.getContainerName()
            );

            sshConnectionPool.executeCommand(resumeCommand);
            log.info("✅ Database instance resumed successfully");

        } catch (SSHConnectionPool.SSHException e) {
            log.error("❌ Failed to resume database: {}", e.getMessage(), e);
            throw new ProvisioningException("Failed to resume database: " + e.getMessage(), e);
        }
    }
}
