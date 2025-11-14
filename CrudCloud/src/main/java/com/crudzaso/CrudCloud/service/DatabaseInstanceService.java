package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import com.crudzaso.CrudCloud.dto.request.CreateInstanceRequest;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;

import java.util.List;

/**
 * Service interface for database instance management
 * Handles creation and management of database instances
 */
public interface DatabaseInstanceService {

    /**
     * Create a new database instance
     * @param request the instance creation request
     * @return the created instance response
     * @throws ResourceNotFoundException if related entities not found
     */
    DatabaseInstanceResponse createInstance(CreateInstanceRequest request);

    /**
     * Get a database instance by ID
     * @param id the instance ID
     * @return the instance response
     * @throws ResourceNotFoundException if instance not found
     */
    DatabaseInstanceResponse getInstance(Long id);

    /**
     * Get all instances for a specific user
     * @param userId the user ID
     * @return list of instances owned by the user
     */
    List<DatabaseInstanceResponse> getUserInstances(Long userId);

    /**
     * Update the status of a database instance
     * @param id the instance ID
     * @param status the new status
     * @return the updated instance response
     * @throws ResourceNotFoundException if instance not found
     */
    DatabaseInstanceResponse updateInstanceStatus(Long id, InstanceStatus status);

    /**
     * Delete a database instance (soft delete)
     * @param id the instance ID
     * @throws ResourceNotFoundException if instance not found
     */
    void deleteInstance(Long id);

    /**
     * Rotate password for a database instance
     * @param id the instance ID
     * @return the instance response with new credentials
     * @throws ResourceNotFoundException if instance not found
     */
    DatabaseInstanceResponse rotatePassword(Long id);
}
