package com.crudzaso.CrudCloud.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Instance statistics tracking entity.
 * Records CPU, memory, storage, and connection metrics for database instances.
 */
@Entity
@Table(name = "instance_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instance_id", nullable = false)
    private DatabaseInstance instance;

    @Column(name = "cpu_usage_percent")
    private Double cpuUsagePercent;

    @Column(name = "memory_usage_mb")
    private Double memoryUsageMb;

    @Column(name = "memory_limit_mb")
    private Double memoryLimitMb;

    @Column(name = "storage_usage_gb")
    private Double storageUsageGb;

    @Column(name = "storage_limit_gb")
    private Double storageLimitGb;

    @Column(name = "active_connections")
    private Integer activeConnections;

    @Column(name = "total_queries")
    private Long totalQueries;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @jakarta.persistence.PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
    }
}
