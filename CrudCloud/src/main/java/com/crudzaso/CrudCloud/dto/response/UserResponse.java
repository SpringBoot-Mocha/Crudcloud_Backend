package com.crudzaso.CrudCloud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response
 * Returned when fetching user information
 * Does NOT expose sensitive fields like passwordHash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private LocalDateTime createdAt;
}
