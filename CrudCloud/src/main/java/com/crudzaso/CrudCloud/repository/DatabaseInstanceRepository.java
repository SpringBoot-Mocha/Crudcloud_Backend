package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DatabaseInstance entity
 * Provides database operations for DatabaseInstance records
 */
@Repository
public interface DatabaseInstanceRepository extends JpaRepository<DatabaseInstance, Long> {

    /**
     * Find all database instances owned by a specific user
     * @param userId the user ID to search for
     * @return List of database instances owned by the user
     */
    List<DatabaseInstance> findByUserId(Long userId);

    /**
     * Find all database instances for a specific subscription
     * @param subscriptionId the subscription ID to search for
     * @return List of database instances for the subscription
     */
    List<DatabaseInstance> findBySubscriptionId(Long subscriptionId);
}
