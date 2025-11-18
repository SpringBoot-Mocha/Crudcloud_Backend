package com.crudzaso.CrudCloud.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request
 * Validates user input when creating a new user account
 *
 * Password requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one number (0-9)
 * - At least one symbol (!@#$%^&* etc)
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
     * Must meet all security requirements:
     * - Minimum 8 characters
     * - Uppercase letter (A-Z)
     * - Lowercase letter (a-z)
     * - Number (0-9)
     * - Symbol (!@#$%^&*()_+-=[]{};':"\\|,.<>/?)
     * Will be hashed with BCrypt before storage
     */
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must have at least 8 characters, one uppercase, one lowercase, one number, and one symbol"
    )
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;
}
