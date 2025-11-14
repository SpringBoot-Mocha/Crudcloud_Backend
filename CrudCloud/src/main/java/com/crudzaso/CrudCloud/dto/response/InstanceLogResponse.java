package com.crudzaso.CrudCloud.dto.response;

import com.crudzaso.CrudCloud.domain.enums.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for instance log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceLogResponse {

    private Long id;
    private Long instanceId;

    private LogLevel level;
    private String title;
    private String message;
    private String source;

    private LocalDateTime createdAt;
}
