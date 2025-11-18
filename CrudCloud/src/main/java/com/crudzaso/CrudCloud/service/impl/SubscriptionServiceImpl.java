package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.CreateSubscriptionRequest;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.SubscriptionService;
import com.crudzaso.CrudCloud.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crudzaso.CrudCloud.mapper.SubscriptionMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Implementation of SubscriptionService
 * Handles subscription management logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final EmailService emailService;

    @Override
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Creating subscription for user ID: {} and plan ID: {}",
                request.getUserId(), request.getPlanId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        // Validate plan exists
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));

        // Create subscription
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .isActive(true)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with ID: {}", savedSubscription.getId());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public SubscriptionResponse getUserSubscription(Long userId) {

        log.debug("Fetching subscription for user ID: {}", userId);

        Subscription subscription = subscriptionRepository.findByUserIdAndIsActive(userId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", userId));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse getSubscriptionById(Long id) {
        log.debug("Fetching subscription with ID: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updatePlan(Long userId, Long newPlanId) {
        log.info("Updating subscription plan for user ID: {} to plan ID: {}", userId, newPlanId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate new plan exists
        Plan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", newPlanId));

        // Get current subscription
        Subscription subscription = subscriptionRepository.findByUserIdAndIsActive(userId, true)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", userId));

        // Store old plan for logging
        Plan oldPlan = subscription.getPlan();

        // Update plan
        subscription.setPlan(newPlan);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        log.info("✅ Subscription updated successfully. User: {}, Old Plan: {}, New Plan: {}",
                user.getEmail(), oldPlan.getName(), newPlan.getName());

        // Send automatic email notification about plan change
        try {
            String userName = user.getFirstName() + " " + user.getLastName();
            Integer storageMB = (int) (newPlan.getMaxStorageGB() * 1024); // Convert GB to MB
            String price = newPlan.getPricePerMonth().toString();

            emailService.notifyPlanChanged(
                    user.getEmail(),
                    userName,
                    newPlan.getName(),
                    newPlan.getMaxInstances(),
                    storageMB,
                    price
            );
            log.info("✅ Plan change notification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("⚠️ Failed to send plan change notification email to {}: {}", user.getEmail(), e.getMessage());
            // Don't throw exception - plan was updated successfully, email failure is non-critical
        }

        return subscriptionMapper.toResponse(updatedSubscription);
    }

}
