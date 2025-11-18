package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.UpdateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.UserMapper;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        log.info("Creating new user with email; {}", request.getEmail());

        // Validate email is not already registered
        if (userRepository.existsByEmail(request.getEmail())){
            log.warn("Email already registered: {}", request.getEmail());
            throw new AppException("Email already registered", "EMAIL_ALREADY_EXISTS");
        }

        // Create and save user entity
        // Parse name into first and last name
        String[] nameParts = request.getName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .firstName(firstName)
                .lastName(lastName)
                .isOrganization(request.getIsOrganization())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
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
                .orElseThrow(() -> new AppException("User not found with email: " + email, "USER_NOT_FOUND"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Update only the name field
        user.setName(request.getName());
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
}
