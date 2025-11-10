package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.CreateSubscriptionRequest;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;
import com.crudzaso.CrudCloud.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Subscription management endpoints.
 *
 * Provides endpoints for managing user subscriptions and plans.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Create or upgrade user subscription.
     *
     * @param request subscription request with plan ID
     * @return created/updated subscription response
     */
    @PostMapping("/upgrade")
    @Operation(summary = "Create or upgrade subscription", description = "Creates or upgrades user subscription to a plan")
    public ResponseEntity<SubscriptionResponse> upgradeSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Creating/upgrading subscription for user: {}", request.getUserId());
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get current active subscription for the user.
     *
     * @param userId the user ID
     * @return current subscription response
     */
    @GetMapping("/current")
    @Operation(summary = "Get current subscription", description = "Retrieves the current active subscription for a user")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription(
            @RequestParam Long userId) {
        log.info("Getting current subscription for user: {}", userId);
        SubscriptionResponse response = subscriptionService.getUserSubscription(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get subscription by ID.
     *
     * @param id the subscription ID
     * @return subscription response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get subscription details", description = "Retrieves subscription details by ID")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @PathVariable Long id) {
        log.info("Getting subscription with ID: {}", id);
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(response);
    }
}
