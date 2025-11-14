package com.crudzaso.CrudCloud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for instance statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceStatsResponse {

    private Long id;
    private Long instanceId;

    private Double cpuUsagePercent;
    private Double memoryUsageMb;
    private Double memoryLimitMb;
    private Double storageUsageGb;
    private Double storageLimitGb;
    private Integer activeConnections;
    private Long totalQueries;

    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;

    // Calculated fields
    private Double memoryUsagePercent;
    private Double storageUsagePercent;
}
