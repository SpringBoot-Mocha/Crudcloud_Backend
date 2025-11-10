package com.crudzaso.CrudCloud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response
 * Returned after successful login
 * Contains JWT token and user information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * JWT token for subsequent authenticated requests
     * Include in Authorization header: Bearer {token}
     */
    private String token;

    /**
     * Authenticated user's information
     */
    private UserResponse user;
}
