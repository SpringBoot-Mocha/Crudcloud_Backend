package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.mapper.UserMapper;
import com.crudzaso.CrudCloud.repository.PlanRepository;
import com.crudzaso.CrudCloud.repository.SubscriptionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl using Mockito
 * Coverage: All methods in UserServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // ===== TEST CASES AQUÍ =====
    // (Copiar los 4 casos de prueba del TESTING_PLAN.md)

    @Test
    @DisplayName("Cuando se crea un usuario, debe crear automáticamente suscripción Free")
    void testCreateUserWithFreeSubscription() {
        // GIVEN
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test123");
        request.setFirstName("Test");
        request.setLastName("User");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        Plan freePlan = Plan.builder()
                .id(1L)
                .name("Free")
                .maxInstances(2)
                .build();

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setUserId(1L);
        expectedResponse.setEmail("test@example.com");

        // WHEN
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(planRepository.findByName("Free")).thenReturn(Optional.of(freePlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(new Subscription());
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.createUser(request);

        // THEN
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        // Verificar que se llamó save en subscription
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("La suscripción Free debe estar marcada como activa")
    void testFreeSubscriptionIsActive() {
        // GIVEN
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test2@example.com");
        request.setPassword("Test123");
        request.setFirstName("Test");
        request.setLastName("User2");

        User user = User.builder()
                .id(2L)
                .email("test2@example.com")
                .build();

        Plan freePlan = Plan.builder()
                .id(1L)
                .name("Free")
                .maxInstances(2)
                .build();

        Subscription subscription = Subscription.builder()
                .id(1L)
                .user(user)
                .plan(freePlan)
                .startDate(LocalDateTime.now())
                .isActive(true)
                .build();

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setUserId(2L);

        // WHEN
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(planRepository.findByName("Free")).thenReturn(Optional.of(freePlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.createUser(request);

        // THEN
        assertThat(result.getUserId()).isEqualTo(2L);
        verify(subscriptionRepository, times(1)).save(argThat(sub -> sub.getIsActive()));
    }

    @Test
    @DisplayName("Si Plan Free no existe, debe lanzar AppException")
    void testCreateUserFailsIfFreePlanNotFound() {
        // GIVEN
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test3@example.com");
        request.setPassword("Test123");
        request.setFirstName("Test");
        request.setLastName("User3");

        User user = User.builder()
                .id(3L)
                .email("test3@example.com")
                .build();

        // WHEN
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(planRepository.findByName("Free")).thenReturn(Optional.empty()); // Plan no existe

        // THEN
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Free plan not found");
    }

    @Test
    @DisplayName("Si la suscripción falla, todo debe hacer rollback")
    void testTransactionRollbackOnSubscriptionFailure() {
        // GIVEN
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test4@example.com");
        request.setPassword("Test123");
        request.setFirstName("Test");
        request.setLastName("User4");

        User user = User.builder()
                .id(4L)
                .email("test4@example.com")
                .build();

        Plan freePlan = Plan.builder()
                .id(1L)
                .name("Free")
                .build();

        // WHEN
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(planRepository.findByName("Free")).thenReturn(Optional.of(freePlan));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenThrow(new RuntimeException("Database error"));

        // THEN
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Failed to create subscription");
    }
}
