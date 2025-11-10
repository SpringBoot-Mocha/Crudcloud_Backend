package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.UpdateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User management endpoints.
 *
 * Provides endpoints for user profile operations. Users can only access/modify their own data.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    /**
     * Get user details by ID.
     *
     * @param id the user ID
     * @return user details response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Retrieves user details by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        log.info("Getting user with ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user information.
     *
     * @param id the user ID
     * @param request update user request
     * @return updated user response
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user information")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user account.
     *
     * @param id the user ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes user account (soft delete)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
