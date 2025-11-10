package com.crudzaso.CrudCloud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for subscription response
 * Returned when fetching subscription information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long id;

    private Long userId;

    private Long planId;

    private String planName;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
