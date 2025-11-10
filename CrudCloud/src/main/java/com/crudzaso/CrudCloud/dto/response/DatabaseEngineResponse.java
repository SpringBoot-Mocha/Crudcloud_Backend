package com.crudzaso.CrudCloud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for database engine response
 * Returned when fetching available database engines
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseEngineResponse {

    private Long id;

    private String name;

    private String version;

    private Integer defaultPort;

    private String dockerImage;

    private String description;
}
