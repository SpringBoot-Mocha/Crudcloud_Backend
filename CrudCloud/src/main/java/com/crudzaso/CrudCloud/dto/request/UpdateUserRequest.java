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
     * User's updated full name or organization name
     * Required field
     */
    @NotBlank(message = "Name is required")
    private String name;
}
