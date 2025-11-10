package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.DatabaseEngineRepository;
import com.crudzaso.CrudCloud.repository.DatabaseInstanceRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.DatabaseInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
    private final ModelMapper modelMapper;

    @Override
    public DatabaseInstanceResponse createInstance(CreateInstanceRequest request) {
        log.info("Creating database instance for user ID: {} and subscription ID: {}",
                request.getUserId(), request.getSubscriptionId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        // Validate subscription exists
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.getSubscriptionId()));

        // Validate database engine exists
        DatabaseEngine engine = databaseEngineRepository.findById(request.getDatabaseEngineId())
                .orElseThrow(() -> new ResourceNotFoundException("Database Engine", request.getDatabaseEngineId()));

        // Generate container name if not provided
        String containerName = request.getInstanceName() != null
                ? request.getInstanceName()
                : generateContainerName(user.getId());

        // Create instance
        DatabaseInstance instance = DatabaseInstance.builder()
                .user(user)
                .subscription(subscription)
                .databaseEngine(engine)
                .containerName(containerName)
                .host("localhost")  // Will be updated by orchestration service
                .port(engine.getDefaultPort())
                .status(InstanceStatus.CREATING)
                .build();

        DatabaseInstance savedInstance = databaseInstanceRepository.save(instance);
        log.info("Database instance created successfully with ID: {}", savedInstance.getId());

        return modelMapper.map(savedInstance, DatabaseInstanceResponse.class);
    }

    @Override
    public DatabaseInstanceResponse getInstance(Long id) {
        log.debug("Fetching database instance with ID: {}", id);

        DatabaseInstance instance = databaseInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", id));

        return modelMapper.map(instance, DatabaseInstanceResponse.class);
    }

    @Override
    public List<DatabaseInstanceResponse> getUserInstances(Long userId) {
        log.debug("Fetching all instances for user ID: {}", userId);

        List<DatabaseInstance> instances = databaseInstanceRepository.findByUserId(userId);
        return instances.stream()
                .map(instance -> modelMapper.map(instance, DatabaseInstanceResponse.class))
                .toList();
    }

    @Override
    public DatabaseInstanceResponse updateInstanceStatus(Long id, InstanceStatus status) {
        log.info("Updating database instance status with ID: {} to status: {}", id, status);

        DatabaseInstance instance = databaseInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", id));

        instance.setStatus(status);
        DatabaseInstance updatedInstance = databaseInstanceRepository.save(instance);

        log.info("Database instance status updated successfully with ID: {}", id);
        return modelMapper.map(updatedInstance, DatabaseInstanceResponse.class);
    }

    private String generateContainerName(Long userId) {
        return "db-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
