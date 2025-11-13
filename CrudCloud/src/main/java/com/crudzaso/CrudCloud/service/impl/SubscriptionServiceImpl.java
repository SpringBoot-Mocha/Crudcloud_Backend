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

}
