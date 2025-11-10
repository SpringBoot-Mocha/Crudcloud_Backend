package com.crudzaso.CrudCloud.dto.request;

import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating database instance status
 * Validates status change requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInstanceStatusRequest {

    /**
     * New status for the database instance
     * Valid values: CREATING, RUNNING, SUSPENDED, DELETED
     * Required field
     */
    @NotNull(message = "Status is required")
    private InstanceStatus status;
}
