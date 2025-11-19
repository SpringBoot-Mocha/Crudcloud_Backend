package com.crudzaso.CrudCloud.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Plan entity representing a CrudCloud subscription plan
 *
 * Maps to the 'plans' table and defines the features and pricing available to users.
 * Plans are static data created by administrators.
 */
@Entity
@Table(name = "plans", indexes = {
        @Index(name = "idx_plans_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "max_instances", nullable = false)
    private Integer maxInstances;

    @Column(name = "max_storage_mb", nullable = false)
    private Long maxStorageMB;

    @Column(name = "price_per_month", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

    @Column(columnDefinition = "TEXT")
    private String description;
}
