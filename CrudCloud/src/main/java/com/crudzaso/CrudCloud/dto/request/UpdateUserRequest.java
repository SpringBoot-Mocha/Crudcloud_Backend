package com.crudzaso.CrudCloud.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user update request
 * Validates user input when updating an existing user account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /**
     * User's updated first name
     * Required field
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * User's updated last name
     * Required field
     */
    @NotBlank(message = "Last name is required")
    private String lastName;
}
