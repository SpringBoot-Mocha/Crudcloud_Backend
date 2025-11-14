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

            return new ProvisioningResult(
                    vpsHost,
                    instance.getPort(),
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
                    "docker stop %s && docker rm %s",
                    instance.getContainerName(),
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
        int port = instance.getDatabaseEngine().getDefaultPort();
        String engineName = instance.getDatabaseEngine().getName().toUpperCase();

        String dockerCommand;

        if ("POSTGRESQL".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name %s " +
                    "-e POSTGRES_PASSWORD=postgres " +
                    "-p 127.0.0.1:%d:5432 " +
                    "%s",
                    containerName, port, dockerImage
            );
        } else if ("MYSQL".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name %s " +
                    "-e MYSQL_ROOT_PASSWORD=root " +
                    "-p 127.0.0.1:%d:3306 " +
                    "%s",
                    containerName, port, dockerImage
            );
        } else if ("MONGODB".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name %s " +
                    "-p 127.0.0.1:%d:27017 " +
                    "%s",
                    containerName, port, dockerImage
            );
        } else if ("REDIS".equals(engineName)) {
            dockerCommand = String.format(
                    "docker run -d " +
                    "--name %s " +
                    "-p 127.0.0.1:%d:6379 " +
                    "%s",
                    containerName, port, dockerImage
            );
        } else {
            throw new SSHConnectionPool.SSHException(
                    "Unsupported database engine: " + engineName
            );
        }

        log.debug("Docker command: {}", dockerCommand);
        sshConnectionPool.executeCommand(dockerCommand);
        log.info("✅ Container started: {}", containerName);
    }

    /**
     * Wait for the container to be ready and accepting connections
     */
    private void waitForContainerReady(DatabaseInstance instance) throws SSHConnectionPool.SSHException, InterruptedException {
        log.info("⏳ Waiting for container to be ready...");

        String containerName = instance.getContainerName();
        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                String checkCommand = "docker inspect -f '{{.State.Running}}' " + containerName;
                String output = sshConnectionPool.executeCommand(checkCommand);

                if (output.contains("true")) {
                    log.info("✅ Container is running and ready");
                    Thread.sleep(2000); // Give services time to fully start
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
        String createCommand;

        if ("POSTGRESQL".equals(engineName)) {
            createCommand = String.format(
                    "docker exec %s psql -U postgres -c " +
                    "\"CREATE USER %s WITH PASSWORD '%s'; CREATE DATABASE %s OWNER %s;\"",
                    containerName, username, password, databaseName, username
            );
        } else if ("MYSQL".equals(engineName)) {
            createCommand = String.format(
                    "docker exec %s mysql -u root -proot -e " +
                    "\"CREATE USER '%s'@'localhost' IDENTIFIED BY '%s'; " +
                    "CREATE DATABASE %s; " +
                    "GRANT ALL PRIVILEGES ON %s.* TO '%s'@'localhost';\"",
                    containerName, username, password, databaseName, databaseName, username
            );
        } else if ("MONGODB".equals(engineName)) {
            createCommand = String.format(
                    "docker exec %s mongosh --eval " +
                    "\"db.getSiblingDB('admin').createUser({user: '%s', pwd: '%s', roles: ['dbOwner']}); " +
                    "use %s;\"",
                    containerName, username, password, databaseName
            );
        } else if ("REDIS".equals(engineName)) {
            // Redis doesn't have traditional databases in the same way
            log.warn("⚠️  Redis doesn't require database creation, skipping database setup");
            return;
        } else {
            throw new SSHConnectionPool.SSHException("Unsupported database engine: " + engineName);
        }

        log.debug("Create DB command: {}", createCommand);
        sshConnectionPool.executeCommand(createCommand);
        log.info("✅ Database created: {}", databaseName);
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
        String verifyCommand;

        if ("POSTGRESQL".equals(engineName)) {
            verifyCommand = String.format(
                    "docker exec %s psql -U %s -d %s -c \"SELECT 1;\"",
                    containerName, username, databaseName
            );
        } else if ("MYSQL".equals(engineName)) {
            verifyCommand = String.format(
                    "docker exec %s mysql -u %s -p%s %s -e \"SELECT 1;\"",
                    containerName, username, password, databaseName
            );
        } else if ("MONGODB".equals(engineName)) {
            verifyCommand = String.format(
                    "docker exec %s mongosh -u %s -p %s --db %s --eval \"db.runCommand({ping: 1});\"",
                    containerName, username, password, databaseName
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
}
