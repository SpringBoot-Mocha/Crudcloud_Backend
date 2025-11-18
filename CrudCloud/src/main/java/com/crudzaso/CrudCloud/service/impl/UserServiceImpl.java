package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.UpdateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.UserMapper;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of UserService
 * Handles user management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        log.info("Creating new user with email: {}", request.getEmail());

        // Validate email is not already registered
        if (userRepository.existsByEmail(request.getEmail())){
            log.warn("Email already registered: {}", request.getEmail());
            throw new AppException("El correo electrónico ya está registrado", "EMAIL_ALREADY_EXISTS");
        }

        // Create and save user entity
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // ============================================================================
        // AUTO-CREATE FREE SUBSCRIPTION FOR NEW USER
        // ============================================================================
        try {
            Plan freePlan = planRepository.findByName("Free")
                    .orElseThrow(() -> new AppException("Plan gratuito no encontrado", "PLAN_NOT_FOUND"));

            Subscription subscription = Subscription.builder()
                    .user(savedUser)
                    .plan(freePlan)
                    .startDate(LocalDateTime.now())
                    .isActive(true)
                    .build();

            subscriptionRepository.save(subscription);
            log.info("✅ Free subscription created automatically for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("❌ Failed to create Free subscription for user ID: {}", savedUser.getId(), e);
            throw new AppException(
                    "No se pudo crear la suscripción: " + e.getMessage(),
                    "SUBSCRIPTION_CREATION_FAILED"
            );
        }

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {

        log.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Usuario no encontrado con el correo electrónico: " + email, "USER_NOT_FOUND"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Update firstName and lastName fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void createFreeSubscriptionForUser(Long userId) {
        log.info("Creating free subscription for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        try {
            Plan freePlan = planRepository.findByName("Free")
                    .orElseThrow(() -> new AppException("Plan gratuito no encontrado", "PLAN_NOT_FOUND"));

            Subscription subscription = Subscription.builder()
                    .user(user)
                    .plan(freePlan)
                    .startDate(LocalDateTime.now())
                    .isActive(true)
                    .build();

            subscriptionRepository.save(subscription);
            log.info("✅ Free subscription created for user ID: {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to create Free subscription for user ID: {}", userId, e);
            throw new AppException(
                    "No se pudo crear la suscripción: " + e.getMessage(),
                    "SUBSCRIPTION_CREATION_FAILED"
            );
        }
    }
}
