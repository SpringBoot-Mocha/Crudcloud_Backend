package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity
 * Provides database operations for Subscription records
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find all subscriptions for a specific user
     * @param userId the user ID to search for
     * @return List of subscriptions for the user
     */
    List<Subscription> findByUserId(long userId);

    /**
     * Find the active subscription for a user
     * @param userId the user ID to search for
     * @param isActive whether to filter by active status
     * @return Optional containing the active subscription if found
     */
    Optional<Subscription> findByUserIdAndIsActive(Long userId, Boolean isActive);
}
