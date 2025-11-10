package com.crudzaso.CrudCloud.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request
 * Validates user input when creating a new user account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Password for the new user account
     * Must be at least 6 characters long
     * Will be hashed with BCrypt before storage
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * User's full name or organization name
     * Required field
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * Flag indicating if this is an organization account
     * Default: false (individual user)
     * Must be explicitly provided
     */
    @NotNull(message = "isOrganization must be provided")
    private Boolean isOrganization = false;
}
