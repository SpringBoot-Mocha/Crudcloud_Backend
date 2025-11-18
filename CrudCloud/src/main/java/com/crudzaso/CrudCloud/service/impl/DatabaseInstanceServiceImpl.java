package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Credential;
import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.CreateInstanceResponse;
import com.crudzaso.CrudCloud.dto.response.CredentialResponse;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import com.crudzaso.CrudCloud.exception.BusinessException;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.DatabaseInstanceMapper;
import com.crudzaso.CrudCloud.repository.CredentialRepository;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import com.crudzaso.CrudCloud.repository.DatabaseInstanceRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.DatabaseInstanceService;
import com.crudzaso.CrudCloud.service.DatabaseProvisioningService;
import com.crudzaso.CrudCloud.service.CredentialEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


/**
 * Implementation of DatabaseInstanceService
 * Handles database instance management logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseInstanceServiceImpl implements DatabaseInstanceService {

    private final DatabaseInstanceRepository databaseInstanceRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DatabaseEngineRepository databaseEngineRepository;
    private final CredentialRepository credentialRepository;
    private final DatabaseInstanceMapper databaseInstanceMapper;
    private final DatabaseProvisioningService provisioningService;
    private final CredentialEncryptionService encryptionService;

    @Value("${provisioning.enabled:false}")
    private boolean provisioningEnabled;

    @Value("${database.host:localhost}")
    private String databaseHost;

    @Override
    @Transactional
    public DatabaseInstanceResponse createInstance(CreateInstanceRequest request) {
        log.info("Creating database instance for user ID: {} and subscription ID: {}",
                request.getUserId(), request.getSubscriptionId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        // Validate subscription exists
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.getSubscriptionId()));

        // VALIDATE PLAN LIMITS (CRITICAL)
        int currentInstances = databaseInstanceRepository.countByUserIdAndStatusNot(
                request.getUserId(), InstanceStatus.DELETED);
        int maxInstances = subscription.getPlan().getMaxInstances();

        if (currentInstances >= maxInstances) {
            log.warn("User {} exceeded plan limit: {}/{} instances",
                    request.getUserId(), currentInstances, maxInstances);
            throw new BusinessException(
                    String.format("Plan limit reached: %d/%d instances. Please upgrade your plan.",
                            currentInstances, maxInstances),
                    HttpStatus.FORBIDDEN.value());
        }

        // Validate database engine exists
        DatabaseEngine engine = databaseEngineRepository.findById(request.getDatabaseEngineId())
                .orElseThrow(() -> new ResourceNotFoundException("Database Engine", request.getDatabaseEngineId()));

        // Generate container name if not provided
        String containerName = request.getInstanceName() != null
                ? request.getInstanceName()
                : generateContainerName(user.getId());

        // Create instance with CREATING status
        DatabaseInstance instance = DatabaseInstance.builder()
                .user(user)
                .subscription(subscription)
                .databaseEngine(engine)
                .containerName(containerName)
                .host(databaseHost)  // Will be updated by provisioning if enabled
                .port(engine.getDefaultPort())
                .status(InstanceStatus.CREATING)
                .build();

        DatabaseInstance savedInstance = databaseInstanceRepository.save(instance);
        log.info("Database instance record created with ID: {}", savedInstance.getId());

        // ============================================================================
        // PROVISIONING: Create real database on VPS or use development mode
        // ============================================================================
        try {
            // Generate secure credentials
            String username = "user_" + savedInstance.getId();  // e.g., user_123
            String plainPassword = encryptionService.generateSecurePassword();

            // IMPORTANT: Initialize lazy-loaded databaseEngine relationship
            // Access it to ensure it's loaded before provisioning
            String engineName = engine.getName();  // Use the engine variable we already have
            log.info("Instance engine verified: {}", engineName);

            if (provisioningEnabled) {
                log.info("🚀 Starting provisioning for instance ID: {} (VPS Mode)", savedInstance.getId());

                // Provision database on VPS
                // Note: savedInstance may have lazy-loaded databaseEngine, so we ensure engine is set
                savedInstance.setDatabaseEngine(engine);  // Ensure the engine is properly set
                DatabaseProvisioningService.ProvisioningResult result = provisioningService.provisionDatabase(
                        savedInstance,
                        containerName,  // Use container name as database name
                        username,
                        plainPassword
                );

                // Update instance with real connection details
                savedInstance.setHost(result.host);
                savedInstance.setPort(result.port);
                savedInstance.setStatus(InstanceStatus.RUNNING);

            } else {
                // DEVELOPMENT MODE: Use configured database host without SSH provisioning
                log.info("🏗️  Development Mode: Skipping remote provisioning, using host: {}", databaseHost);

                // Use configured database host with default port for development
                savedInstance.setHost(databaseHost);
                savedInstance.setPort(engine.getDefaultPort());
                savedInstance.setStatus(InstanceStatus.RUNNING);
            }

            DatabaseInstance updatedInstance = databaseInstanceRepository.save(savedInstance);
            log.info("✅ Instance configuration completed successfully");

            // ================================================================
            // SAVE ENCRYPTED CREDENTIALS
            // ================================================================
            Credential credential = Credential.builder()
                    .databaseInstance(updatedInstance)
                    .username(username)
                    .encryptedPassword(encryptionService.encrypt(plainPassword))
                    .build();

            credentialRepository.save(credential);
            log.info("✅ Credentials saved (encrypted)");

            // ================================================================
            // BUILD RESPONSE WITH PLAINTEXT CREDENTIALS (ONLY ONCE)
            // ================================================================
            // Note: We return credentials in plaintext ONLY in this response
            log.info("📧 Returning credentials to user (plaintext shown only once)");

            // TODO: Email credentials to user as well

            DatabaseInstanceResponse response = databaseInstanceMapper.toResponse(updatedInstance);
            // Enrich with plaintext credentials from this creation
            response.setUsername(username);
            response.setPassword(plainPassword);
            // Ensure databaseEngine name is set correctly (from engine variable which has the correct engine)
            response.setDatabaseEngine(engine.getName());
            return response;

        } catch (DatabaseProvisioningService.ProvisioningException e) {
            log.error("❌ Provisioning failed: {}", e.getMessage(), e);

            // Update instance status to indicate failure (but keep record)
            savedInstance.setStatus(InstanceStatus.CREATING);  // Could be FAILED in future
            databaseInstanceRepository.save(savedInstance);

            throw new BusinessException(
                    "Failed to provision database: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DatabaseInstanceResponse getInstance(Long id) {
        log.debug("Fetching database instance with ID: {}", id);

        DatabaseInstance instance = databaseInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", id));

        DatabaseInstanceResponse response = databaseInstanceMapper.toResponse(instance);
        return enrichWithCredentials(response, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatabaseInstanceResponse> getUserInstances(Long userId) {
        log.debug("Fetching all instances for user ID: {}", userId);

        List<DatabaseInstance> instances = databaseInstanceRepository.findByUserId(userId);
        return instances.stream()
                .map(databaseInstanceMapper::toResponse)
                .map(response -> enrichWithCredentials(response, response.getId()))
                .toList();
    }

    /**
     * Enrich instance response with decrypted credentials
     * @param response the response DTO to enrich
     * @param instanceId the database instance ID
     * @return the enriched response with username and decrypted password
     */
    private DatabaseInstanceResponse enrichWithCredentials(DatabaseInstanceResponse response, Long instanceId) {
        try {
            credentialRepository.findByDatabaseInstanceId(instanceId).ifPresent(credential -> {
                try {
                    response.setUsername(credential.getUsername());
                    String decryptedPassword = encryptionService.decrypt(credential.getEncryptedPassword());
                    response.setPassword(decryptedPassword);
                } catch (Exception e) {
                    log.error("Failed to decrypt password for instance {}: {}", instanceId, e.getMessage(), e);
                    response.setPassword("[Error: unable to decrypt]");
                }
            });
        } catch (Exception e) {
            log.error("Failed to enrich credentials for instance {}: {}", instanceId, e.getMessage(), e);
        }
        return response;
    }

    @Override
    @Transactional
    public DatabaseInstanceResponse updateInstanceStatus(Long id, InstanceStatus status) {
        log.info("Updating database instance status with ID: {} to status: {}", id, status);

        DatabaseInstance instance = databaseInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", id));

        try {
            // Execute Docker command based on desired status
            if (InstanceStatus.SUSPENDED == status) {
                log.info("🐳 Executing PAUSE command via Docker...");
                provisioningService.pauseDatabase(instance);
            } else if (InstanceStatus.RUNNING == status) {
                log.info("🐳 Executing RESUME command via Docker...");
                provisioningService.resumeDatabase(instance);
            }

            // Update database status
            instance.setStatus(status);
            DatabaseInstance updatedInstance = databaseInstanceRepository.save(instance);

            log.info("✅ Database instance status updated successfully with ID: {}", id);
            return databaseInstanceMapper.toResponse(updatedInstance);

        } catch (DatabaseProvisioningService.ProvisioningException e) {
            log.error("❌ Failed to update instance status: {}", e.getMessage(), e);
            throw new BusinessException(
                    "Failed to update instance status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    private String generateContainerName(Long userId) {
        // Simple container name without hyphens or special chars: db{userId}{timestamp}
        return "db" + userId + System.currentTimeMillis();
    }

    /**
     * Build a CredentialResponse with plaintext password (only for creation response)
     */
    private CredentialResponse buildCredentialResponse(
            DatabaseInstance instance,
            String username,
            String plainPassword) {

        String engineName = instance.getDatabaseEngine().getName();
        String connectionString = String.format(
                "%s://%s@%s:%d/%s",
                engineName.toLowerCase(),
                username,
                instance.getHost(),
                instance.getPort(),
                instance.getContainerName()
        );

        String sampleConnections = buildSampleConnections(
                engineName,
                username,
                plainPassword,
                instance.getHost(),
                instance.getPort(),
                instance.getContainerName()
        );

        return CredentialResponse.builder()
                .username(username)
                .password(plainPassword)
                .databaseName(instance.getContainerName())
                .host(instance.getHost())
                .port(instance.getPort())
                .connectionString(connectionString)
                .sampleConnections(sampleConnections)
                .build();
    }

    /**
     * Build sample connection commands for different database engines
     */
    private String buildSampleConnections(
            String engineName,
            String username,
            String password,
            String host,
            Integer port,
            String databaseName) {

        StringBuilder sb = new StringBuilder();
        sb.append("=== Sample Connection Commands ===\n\n");

        switch (engineName.toUpperCase()) {
            case "POSTGRESQL":
                sb.append("# psql (command line)\n");
                sb.append(String.format("psql -h %s -U %s -d %s -p %d\n\n", host, username, databaseName, port));
                sb.append("# Connection string for apps\n");
                sb.append(String.format("postgresql://%s:%s@%s:%d/%s\n\n", username, password, host, port, databaseName));
                break;

            case "MYSQL":
                sb.append("# mysql (command line)\n");
                sb.append(String.format("mysql -h %s -u %s -p -P %d %s\n", host, username, port, databaseName));
                sb.append(String.format("(Enter password when prompted: %s)\n\n", password));
                sb.append("# Connection string for apps\n");
                sb.append(String.format("mysql://%s:%s@%s:%d/%s\n\n", username, password, host, port, databaseName));
                break;

            case "MONGODB":
                sb.append("# mongosh (MongoDB shell)\n");
                sb.append(String.format("mongosh -u %s -p %s --db %s\n\n", username, password, databaseName));
                sb.append("# Connection string for apps\n");
                sb.append(String.format("mongodb://%s:%s@%s:%d/%s\n\n", username, password, host, port, databaseName));
                break;

            case "REDIS":
                sb.append("# redis-cli (Redis CLI)\n");
                sb.append(String.format("redis-cli -h %s -p %d -a %s\n\n", host, port, password));
                sb.append("# Connection string for apps\n");
                sb.append(String.format("redis://%s@%s:%d\n\n", password, host, port));
                break;

            case "SQL SERVER":
                sb.append("# sqlcmd (SQL Server)\n");
                sb.append(String.format("sqlcmd -S %s,%d -U %s -P %s -d %s\n\n", host, port, username, password, databaseName));
                sb.append("# Connection string for apps\n");
                sb.append(String.format("mssql://%s:%s@%s:%d/%s\n\n", username, password, host, port, databaseName));
                break;

            case "CASSANDRA":
                sb.append("# cqlsh (Cassandra)\n");
                sb.append(String.format("cqlsh -u %s -p %s %s %d\n\n", username, password, host, port));
                break;

            default:
                sb.append("# Connection details\n");
                sb.append(String.format("Host: %s\n", host));
                sb.append(String.format("Port: %d\n", port));
                sb.append(String.format("Username: %s\n", username));
                sb.append(String.format("Password: %s\n", password));
        }

        return sb.toString();
    }

    @Override
    public void deleteInstance(Long id) {
        log.info("Deleting database instance with ID: {}", id);

        DatabaseInstance instance = databaseInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", id));

        // Soft delete: Set status to DELETED instead of physically removing
        instance.setStatus(InstanceStatus.DELETED);
        databaseInstanceRepository.save(instance);

        log.info("Database instance with ID: {} marked as DELETED", id);
    }

    @Override
    public DatabaseInstanceResponse rotatePassword(Long id) {
        log.info("Rotating password for database instance with ID: {}", id);

        DatabaseInstance instance = databaseInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", id));

        // Get the current credential
        Credential credential = credentialRepository.findByDatabaseInstanceId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", id));

        // Generate new password
        String newPlainPassword = encryptionService.generateSecurePassword();

        // Update credential with new encrypted password
        credential.setEncryptedPassword(encryptionService.encrypt(newPlainPassword));
        credentialRepository.save(credential);

        log.info("Password rotated successfully for instance ID: {}", id);

        return databaseInstanceMapper.toResponse(instance);
    }
}
