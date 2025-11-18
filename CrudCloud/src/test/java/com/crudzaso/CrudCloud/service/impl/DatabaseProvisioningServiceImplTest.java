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
import static org.mockito.ArgumentMatchers.contains;

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

        // Mock all SSH commands with flexible matchers
        // PostgreSQL now makes separate calls for CREATE USER and CREATE DATABASE
        doReturn("container_id").when(sshConnectionPool).executeCommand(anyString());
        doReturn("true").when(sshConnectionPool).executeCommand(contains("docker inspect"));
        doReturn("CREATE USER created").when(sshConnectionPool).executeCommand(contains("CREATE USER"));
        doReturn("CREATE DATABASE created").when(sshConnectionPool).executeCommand(contains("CREATE DATABASE"));
        doReturn("1").when(sshConnectionPool).executeCommand(contains("SELECT 1"));

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(postgresInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertNotEquals(5432, result.port); // Port should be dynamic, not default
        assertEquals(username, result.username);
        assertEquals(password, result.password);
        assertEquals("db-user1-abc123", result.containerName);

        // Verify SSH commands were attempted (should be more than before due to separate CREATE USER and CREATE DATABASE)
        verify(sshConnectionPool, atLeast(4)).executeCommand(anyString());
    }

    @Test
    void provisionDatabase_MySQL_Success() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock all SSH commands with flexible matchers (port is dynamic)
        doReturn("container_id").when(sshConnectionPool).executeCommand(anyString());
        doReturn("true").when(sshConnectionPool).executeCommand(contains("docker inspect"));

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(mysqlInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertNotEquals(3306, result.port); // Port should be dynamic
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

        // Mock all SSH commands with flexible matchers (port is dynamic)
        doReturn("container_id").when(sshConnectionPool).executeCommand(anyString());
        doReturn("true").when(sshConnectionPool).executeCommand(contains("docker inspect"));
        doReturn("Success").when(sshConnectionPool).executeCommand(contains("mongosh"));

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(mongodbInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertNotEquals(27017, result.port); // Port should be dynamic
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
        doReturn("container_id").when(sshConnectionPool).executeCommand(anyString());
        doReturn("true").when(sshConnectionPool).executeCommand(contains("docker inspect"));

        // Act
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(redisInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertNotEquals(6379, result.port); // Port should be dynamic
        assertEquals(username, result.username);
        assertEquals(password, result.password);
        assertEquals("db-user1-jkl012", result.containerName);

        // Verify no database creation commands for Redis (skipped in code)
        verify(sshConnectionPool, never()).executeCommand(contains("CREATE USER"));
    }

    @Test
    void provisionDatabase_ContainerStartFailure_ThrowsProvisioningException() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup failure
        when(sshConnectionPool.executeCommand(contains("docker run -d --name db-user1-abc123")))
                .thenThrow(new SSHConnectionPool.SSHException("Docker daemon not available"));

        // Act & Assert
        DatabaseProvisioningService.ProvisioningException exception = assertThrows(
                DatabaseProvisioningService.ProvisioningException.class,
                () -> databaseProvisioningService.provisionDatabase(postgresInstance, databaseName, username, password)
        );

        assertTrue(exception.getMessage().contains("SSH error during database provisioning"));
    }

    @Test
    void provisionDatabase_ContainerNotReady_ThrowsProvisioningException() throws Exception {
        // Arrange
        String databaseName = "test_db";
        String username = "test_user";
        String password = "secure_password";

        // Mock Docker container startup
        doReturn("container_id").when(sshConnectionPool).executeCommand(contains("docker run"));

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

        // Mock Docker container startup and readiness
        doReturn("container_id").when(sshConnectionPool).executeCommand(anyString());
        doReturn("true").when(sshConnectionPool).executeCommand(contains("docker inspect"));

        // Mock database creation failure - throw exception on CREATE USER
        doThrow(new SSHConnectionPool.SSHException("Database already exists"))
                .when(sshConnectionPool).executeCommand(contains("CREATE USER"));

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
        when(sshConnectionPool.executeCommand(anyString()))
                .thenReturn("container_stopped_and_removed");

        // Act
        databaseProvisioningService.deleteDatabase(postgresInstance);

        // Assert
        verify(sshConnectionPool).executeCommand(contains("docker stop"));
    }

    @Test
    void deleteDatabase_Failure_ThrowsProvisioningException() throws Exception {
        // Arrange
        when(sshConnectionPool.executeCommand(contains("docker stop")))
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
        doReturn("container_id").when(sshConnectionPool).executeCommand(anyString());
        doReturn("true").when(sshConnectionPool).executeCommand(contains("docker inspect"));
        doReturn("CREATE USER created").when(sshConnectionPool).executeCommand(contains("CREATE USER"));
        doReturn("CREATE DATABASE created").when(sshConnectionPool).executeCommand(contains("CREATE DATABASE"));

        // Mock connection verification failure (but should not throw exception)
        doThrow(new SSHConnectionPool.SSHException("Connection refused"))
                .when(sshConnectionPool).executeCommand(contains("SELECT 1"));

        // Act - Should not throw exception even if verification fails
        DatabaseProvisioningService.ProvisioningResult result = databaseProvisioningService
                .provisionDatabase(postgresInstance, databaseName, username, password);

        // Assert
        assertNotNull(result);
        assertEquals("91.98.225.17", result.host);
        assertNotEquals(5432, result.port); // Port should be dynamic
        assertEquals(username, result.username);
        assertEquals(password, result.password);

        // Verify commands were attempted (verification failure is logged but not thrown)
        verify(sshConnectionPool, atLeast(4)).executeCommand(anyString());
    }
}