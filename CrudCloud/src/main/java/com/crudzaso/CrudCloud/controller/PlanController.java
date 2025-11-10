package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import com.crudzaso.CrudCloud.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Subscription plan endpoints for browsing available plans.
 *
 * Read-only endpoints that do not require authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Browse available subscription plans (public endpoints)")
public class PlanController {

    private final PlanService planService;

    /**
     * Get all available subscription plans.
     *
     * @return list of all plans ordered by price
     */
    @GetMapping
    @Operation(summary = "List all plans", description = "Retrieves all available subscription plans ordered by price")
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        log.info("Getting all subscription plans");
        List<PlanResponse> plans = planService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get a specific subscription plan by ID.
     *
     * @param id the plan ID
     * @return plan response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get plan details", description = "Retrieves details of a specific subscription plan")
    public ResponseEntity<PlanResponse> getPlanById(@PathVariable Long id) {
        log.info("Getting plan with ID: {}", id);
        PlanResponse plan = planService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }
}
