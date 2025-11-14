package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.infrastructure.ssh.SSHConnectionPool;
import com.crudzaso.CrudCloud.service.DatabaseProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatabaseProvisioningServiceImpl
 *
 * IMPORTANT: This class tests the database provisioning service which orchestrates
 * Docker container creation and database setup on the VPS via SSH.
 * All SSH operations are mocked to avoid real connections during testing.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseProvisioningServiceImplTest {

    @Mock
    private SSHConnectionPool sshConnectionPool;

    @InjectMocks
    private DatabaseProvisioningServiceImpl databaseProvisioningService;

    private DatabaseInstance postgresInstance;
    private DatabaseInstance mysqlInstance;
    private DatabaseInstance mongodbInstance;
    private DatabaseInstance redisInstance;

    @BeforeEach
    void setUp() {
        // Set VPS host via reflection
        ReflectionTestUtils.setField(databaseProvisioningService, "vpsHost", "91.98.225.17");

        // Create PostgreSQL instance
        DatabaseEngine postgresEngine = DatabaseEngine.builder()
                .id(1L)
                .name("PostgreSQL")
                .version("14")
                .defaultPort(5432)
                .dockerImage("postgres:14")
                .build();

        postgresInstance = DatabaseInstance.builder()
                .id(1L)
                .containerName("db-user1-abc123")
                .databaseEngine(postgresEngine)
                .port(5432)
                .status(InstanceStatus.CREATING)
                .build();

        // Create MySQL instance
        DatabaseEngine mysqlEngine = DatabaseEngine.builder()
                .id(2L)
                .name("MySQL")
                .version("8.0")
                .defaultPort(3306)
                .dockerImage("mysql:8.0")
                .build();

        mysqlInstance = DatabaseInstance.builder()
                .id(2L)
                .containerName("db-user1-def456")
                .databaseEngine(mysqlEngine)
                .port(3306)
                .status(InstanceStatus.CREATING)
                .build();

        // Create MongoDB instance
        DatabaseEngine mongodbEngine = DatabaseEngine.builder()
                .id(3L)
                .name("MongoDB")
                .version("6.0")
                .defaultPort(27017)
                .dockerImage("mongo:6.0")
                .build();

        mongodbInstance = DatabaseInstance.builder()
                .id(3L)
                .containerName("db-user1-ghi789")
                .databaseEngine(mongodbEngine)
                .port(27017)
                .status(InstanceStatus.CREATING)
                .build();

        // Create Redis instance
        DatabaseEngine redisEngine = DatabaseEngine.builder()
                .id(4L)
                .name("Redis")
                .version("7.0")
                .defaultPort(6379)
                .dockerImage("redis:7.0")
                .build();

        redisInstance = DatabaseInstance.builder()
                .id(4L)
                .containerName("db-user1-jkl012")
                .databaseEngine(redisEngine)
                .port(6379)
                .status(InstanceStatus.CREATING)
                .build();
    }

    @Test
    void provisionDatabase_PostgreSQL_Success() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        String expectedDockerCommand = "docker run -d --name db-user1-abc123 -e POSTGRES_PASSWORD=postgres -p 127.0.0.1:5432:5432 postgres:14";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        // Mock container readiness check
        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-abc123"))
                .thenReturn("true");

        // Mock database creation
        String expectedCreateCommand = "docker exec db-user1-abc123 psql -U postgres -c \"CREATE USER test_user WITH PASSWORD 'secure_password'; CREATE DATABASE test_db OWNER test_user;\"";
        when(sshConnectionPool.executeCommand(expectedCreateCommand)).thenReturn("CREATE DATABASE");

        // Mock connection verification
        String expectedVerifyCommand = "docker exec db-user1-abc123 psql -U test_user -d test_db -c \"SELECT 1;\"";
        when(sshConnectionPool.executeCommand(expectedVerifyCommand)).thenReturn("1");

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(postgresInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertEquals(5432, result.port);
        assertEquals(username, result.username);
        assertEquals(password, result.password);
        assertEquals("db-user1-abc123", result.containerName);

        // Verify SSH commands were executed in correct order
        verify(sshConnectionPool).executeCommand(expectedDockerCommand);
        verify(sshConnectionPool).executeCommand("docker inspect -f '{{.State.Running}}' db-user1-abc123");
        verify(sshConnectionPool).executeCommand(expectedCreateCommand);
        verify(sshConnectionPool).executeCommand(expectedVerifyCommand);
    }

    @Test
    void provisionDatabase_MySQL_Success() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        String expectedDockerCommand = "docker run -d --name db-user1-def456 -e MYSQL_ROOT_PASSWORD=root -p 127.0.0.1:3306:3306 mysql:8.0";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        // Mock container readiness check
        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-def456"))
                .thenReturn("true");

        // Mock database creation
        String expectedCreateCommand = "docker exec db-user1-def456 mysql -u root -proot -e \"CREATE USER 'test_user'@'localhost' IDENTIFIED BY 'secure_password'; CREATE DATABASE test_db; GRANT ALL PRIVILEGES ON test_db.* TO 'test_user'@'localhost';\"";
        when(sshConnectionPool.executeCommand(expectedCreateCommand)).thenReturn("Query OK");

        // Mock connection verification
        String expectedVerifyCommand = "docker exec db-user1-def456 mysql -u test_user -psecure_password test_db -e \"SELECT 1;\"";
        when(sshConnectionPool.executeCommand(expectedVerifyCommand)).thenReturn("1");

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(mysqlInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertEquals(3306, result.port);
        assertEquals(username, result.username);
        assertEquals(password, result.password);
        assertEquals("db-user1-def456", result.containerName);
    }

    @Test
    void provisionDatabase_MongoDB_Success() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        String expectedDockerCommand = "docker run -d --name db-user1-ghi789 -p 127.0.0.1:27017:27017 mongo:6.0";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        // Mock container readiness check
        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-ghi789"))
                .thenReturn("true");

        // Mock database creation
        String expectedCreateCommand = "docker exec db-user1-ghi789 mongosh --eval \"db.getSiblingDB('admin').createUser({user: 'test_user', pwd: 'secure_password', roles: ['dbOwner']}); use test_db;\"";
        when(sshConnectionPool.executeCommand(expectedCreateCommand)).thenReturn("Success");

        // Mock connection verification
        String expectedVerifyCommand = "docker exec db-user1-ghi789 mongosh -u test_user -p secure_password --db test_db --eval \"db.runCommand({ping: 1});\"";
        when(sshConnectionPool.executeCommand(expectedVerifyCommand)).thenReturn("{ ok: 1 }");

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(mongodbInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertEquals(27017, result.port);
        assertEquals(username, result.username);
        assertEquals(password, result.password);
        assertEquals("db-user1-ghi789", result.containerName);
    }

    @Test
    void provisionDatabase_Redis_Success() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        String expectedDockerCommand = "docker run -d --name db-user1-jkl012 -p 127.0.0.1:6379:6379 redis:7.0";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        // Mock container readiness check
        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-jkl012"))
                .thenReturn("true");

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(redisInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertEquals(6379, result.port);
        assertEquals(username, result.username);
        assertEquals(password, result.password);
        assertEquals("db-user1-jkl012", result.containerName);

        // Verify no database creation commands for Redis
        verify(sshConnectionPool, never()).executeCommand(contains("CREATE USER"));
        verify(sshConnectionPool, never()).executeCommand(contains("CREATE DATABASE"));
    }

    @Test
    void provisionDatabase_ContainerStartFailure_ThrowsProvisioningException() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup failure
        String expectedDockerCommand = "docker run -d --name db-user1-abc123 -e POSTGRES_PASSWORD=postgres -p 127.0.0.1:5432:5432 postgres:14";
        when(sshConnectionPool.executeCommand(expectedDockerCommand))
                .thenThrow(new SSHConnectionPool.SSHException("Docker daemon not available"));

        // Act & Assert
        DatabaseProvisioningService.ProvisioningException exception = assertThrows(
                DatabaseProvisioningService.ProvisioningException.class,
                () -> databaseProvisioningService.provisionDatabase(postgresInstance, databaseName, username, password)
        );

        assertTrue(exception.getMessage().contains("SSH error during database provisioning"));
        assertTrue(exception.getCause() instanceof SSHConnectionPool.SSHException);
    }

    @Test
    void provisionDatabase_ContainerNotReady_ThrowsProvisioningException() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        String expectedDockerCommand = "docker run -d --name db-user1-abc123 -e POSTGRES_PASSWORD=postgres -p 127.0.0.1:5432:5432 postgres:14";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        // Mock container readiness check always returns false
        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-abc123"))
                .thenReturn("false");

        // Act & Assert
        DatabaseProvisioningService.ProvisioningException exception = assertThrows(
                DatabaseProvisioningService.ProvisioningException.class,
                () -> databaseProvisioningService.provisionDatabase(postgresInstance, databaseName, username, password)
        );

        assertTrue(exception.getMessage().contains("Container startup timeout"));
    }

    @Test
    void provisionDatabase_DatabaseCreationFailure_ThrowsProvisioningException() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        String expectedDockerCommand = "docker run -d --name db-user1-abc123 -e POSTGRES_PASSWORD=postgres -p 127.0.0.1:5432:5432 postgres:14";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        // Mock container readiness check
        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-abc123"))
                .thenReturn("true");

        // Mock database creation failure
        String expectedCreateCommand = "docker exec db-user1-abc123 psql -U postgres -c \"CREATE USER test_user WITH PASSWORD 'secure_password'; CREATE DATABASE test_db OWNER test_user;\"";
        when(sshConnectionPool.executeCommand(expectedCreateCommand))
                .thenThrow(new SSHConnectionPool.SSHException("Database already exists"));

        // Act & Assert
        DatabaseProvisioningService.ProvisioningException exception = assertThrows(
                DatabaseProvisioningService.ProvisioningException.class,
                () -> databaseProvisioningService.provisionDatabase(postgresInstance, databaseName, username, password)
        );

        assertTrue(exception.getMessage().contains("SSH error during database provisioning"));
    }

    @Test
    void deleteDatabase_Success() throws Exception {
        // Arrange
        String expectedDeleteCommand = "docker stop db-user1-abc123 && docker rm db-user1-abc123";
        when(sshConnectionPool.executeCommand(expectedDeleteCommand)).thenReturn("container_stopped_and_removed");

        // Act
        databaseProvisioningService.deleteDatabase(postgresInstance);

        // Assert
        verify(sshConnectionPool).executeCommand(expectedDeleteCommand);
    }

    @Test
    void deleteDatabase_Failure_ThrowsProvisioningException() throws Exception {
        // Arrange
        String expectedDeleteCommand = "docker stop db-user1-abc123 && docker rm db-user1-abc123";
        when(sshConnectionPool.executeCommand(expectedDeleteCommand))
                .thenThrow(new SSHConnectionPool.SSHException("Container not found"));

        // Act & Assert
        DatabaseProvisioningService.ProvisioningException exception = assertThrows(
                DatabaseProvisioningService.ProvisioningException.class,
                () -> databaseProvisioningService.deleteDatabase(postgresInstance)
        );

        assertTrue(exception.getMessage().contains("Failed to delete database"));
    }

    @Test
    void provisionDatabase_UnsupportedEngine_ThrowsSSHException() throws Exception {
        // Arrange
        DatabaseEngine unsupportedEngine = DatabaseEngine.builder()
                .id(99L)
                .name("UnsupportedDB")
                .version("1.0")
                .defaultPort(9999)
                .dockerImage("unsupported:1.0")
                .build();

        DatabaseInstance unsupportedInstance = DatabaseInstance.builder()
                .id(99L)
                .containerName("db-user1-unsupported")
                .databaseEngine(unsupportedEngine)
                .port(9999)
                .status(InstanceStatus.CREATING)
                .build();

        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Act & Assert
        DatabaseProvisioningService.ProvisioningException exception = assertThrows(
                DatabaseProvisioningService.ProvisioningException.class,
                () -> databaseProvisioningService.provisionDatabase(unsupportedInstance, databaseName, username, password)
        );

        assertTrue(exception.getMessage().contains("Unsupported database engine"));
    }

    @Test
    void provisionDatabase_ConnectionVerificationFailure_Continues() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock all successful operations except verification
        String expectedDockerCommand = "docker run -d --name db-user1-abc123 -e POSTGRES_PASSWORD=postgres -p 127.0.0.1:5432:5432 postgres:14";
        when(sshConnectionPool.executeCommand(expectedDockerCommand)).thenReturn("container_id");

        when(sshConnectionPool.executeCommand("docker inspect -f '{{.State.Running}}' db-user1-abc123"))
                .thenReturn("true");

        String expectedCreateCommand = "docker exec db-user1-abc123 psql -U postgres -c \"CREATE USER test_user WITH PASSWORD 'secure_password'; CREATE DATABASE test_db OWNER test_user;\"";
        when(sshConnectionPool.executeCommand(expectedCreateCommand)).thenReturn("CREATE DATABASE");

        // Mock connection verification failure
        String expectedVerifyCommand = "docker exec db-user1-abc123 psql -U test_user -d test_db -c \"SELECT 1;\"";
        when(sshConnectionPool.executeCommand(expectedVerifyCommand))
                .thenThrow(new SSHConnectionPool.SSHException("Connection refused"));

        // Act - Should not throw exception even if verification fails
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(postgresInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertEquals(5432, result.port);
        assertEquals(username, result.username);
        assertEquals(password, result.password);

        // Verify all commands were attempted
        verify(sshConnectionPool).executeCommand(expectedDockerCommand);
        verify(sshConnectionPool).executeCommand("docker inspect -f '{{.State.Running}}' db-user1-abc123");
        verify(sshConnectionPool).executeCommand(expectedCreateCommand);
        verify(sshConnectionPool).executeCommand(expectedVerifyCommand);
    }
}