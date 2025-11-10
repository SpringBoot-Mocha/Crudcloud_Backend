package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.request.CreateUserRequest;
import com.crudzaso.CrudCloud.dto.request.UpdateUserRequest;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import com.crudzaso.CrudCloud.exception.AppException;

public interface UserService {

    /**
     * Create a new user account
     * @param request the user creation request with email, password, name
     * @return the created user response
     * @throws AppException if email already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Get a user by ID
     * @param id the user ID
     * @return the user response
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse getUserById(Long id);

    /**
     * Get a user by email address
     * @param email the email to search for
     * @return the user response
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse getUserByEmail(String email);

    /**
     * Update an existing user
     * @param id the user ID to update
     * @param request the update request
     * @return the updated user response
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Delete a user by ID
     * @param id the user ID to delete
     * @throws ResourceNotFoundException if user not found
     */
    void deleteUser(Long id);
}
