package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.request.CreateSubscriptionRequest;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;

/**
 * Service interface for subscription management
 * Handles user subscription to plans
 */
public interface SubscriptionService {

    /**
     * Create a new subscription for a user
     * @param request the subscription creation request
     * @return the created subscription response
     * @throws ResourceNotFoundException if user or plan not found
     */
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    /**
     * Get the current subscription for a user
     * @param userId the user ID
     * @return the subscription response
     * @throws ResourceNotFoundException if subscription not found
     */
    SubscriptionResponse getUserSubscription(Long userId);

    /**
     * Get subscription by ID
     * @param id the subscription ID
     * @return the subscription response
     * @throws ResourceNotFoundException if subscription not found
     */
    SubscriptionResponse getSubscriptionById(Long id);
}
