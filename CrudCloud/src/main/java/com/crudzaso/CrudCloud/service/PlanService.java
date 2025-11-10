package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.response.PlanResponse;

import java.util.List;

/**
 * Service interface for plan operations.
 */
public interface PlanService {

    /**
     * Get all available subscription plans.
     *
     * @return list of all plans
     */
    List<PlanResponse> getAllPlans();

    /**
     * Get a specific plan by ID.
     *
     * @param id the plan ID
     * @return plan response
     */
    PlanResponse getPlanById(Long id);
}
