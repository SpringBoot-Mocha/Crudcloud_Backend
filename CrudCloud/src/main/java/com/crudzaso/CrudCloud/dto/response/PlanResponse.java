package com.crudzaso.CrudCloud.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for plan response
 * Returned when fetching plan information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {

    private Long id;

    private String name;

    private Integer maxInstances;

    private Long maxStorageMB;

    private BigDecimal priceMonth;

    private String description;
}
