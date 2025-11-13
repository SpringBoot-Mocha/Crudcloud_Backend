package com.crudzaso.CrudCloud.dto.response;

import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for database instance response
 * Returned when fetching database instance information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseInstanceResponse {

    private Long id;

    private Long userId;

    private Long subscriptionId;

    private Long databaseEngine;

    private String containerName;

    private String host;

    private Integer port;

    private InstanceStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
