package com.crudzaso.CrudCloud.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login request
 * Validates credentials for authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Email address for login
     * Must be a valid email format
     * Required field
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Password for authentication
     * Will be compared against BCrypt hashed password
     * Required field
     */
    @NotBlank(message = "Password is required")
    private String password;
}
