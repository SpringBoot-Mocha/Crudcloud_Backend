package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.PlanMapper;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import com.crudzaso.CrudCloud.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of PlanService.
 *
 * Handles retrieval of subscription plans.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    /**
     * Get all available subscription plans ordered by price.
     *
     * @return list of all plans
     */
    @Override
    public List<PlanResponse> getAllPlans() {
        log.info("Fetching all subscription plans");
        return planMapper.toResponseList(planRepository.findAll(Sort.by(Sort.Direction.ASC, "priceMonth")));
    }

    /**
     * Get a specific plan by ID.
     *
     * @param id the plan ID
     * @return plan response
     * @throws ResourceNotFoundException if plan not found
     */
    @Override
    public PlanResponse getPlanById(Long id) {
        log.info("Fetching plan with ID: {}", id);
        return planRepository.findById(id)
                .map(planMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));
    }
}
