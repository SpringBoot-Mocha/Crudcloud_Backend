package com.crudzaso.CrudCloud.domain.entity;

import com.crudzaso.CrudCloud.domain.enums.InstanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DatabaseInstance entity representing a user's running database instance
 *
 * Maps to the 'instance_stats' table and represents actual database
 * instances running in Docker containers. Tracks status, connection details,
 * and relationships to users and their subscriptions.
 */
@Entity
@Table(name = "instance_stats", indexes = {
        @Index(name = "idx_instance_stats_user_id", columnList = "user_id"),
        @Index(name = "idx_instance_stats_subscription_id", columnList = "subscription_id"),
        @Index(name = "idx_instance_stats_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user who owns this instance
     * Many-to-One relationship
     * Required field
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Reference to the subscription this instance belongs to
     * Many-to-One relationship
     * Required field
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /**
     * Reference to the database engine type
     * Many-to-One relationship
     * Required field
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "database_engine_id", nullable = false)
    private DatabaseEngine databaseEngine;

    @Column(nullable = false, unique = true)
    private String containerName;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    /**
     * Current status of the database instance
     * CREATING, RUNNING, SUSPENDED, DELETED
     * Required field
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InstanceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - executed before first insert
     * Automatically sets createdAt and updatedAt to current timestamp
     * Sets initial status to CREATING
     */
    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null){
            status = InstanceStatus.CREATING;
        }
    }

    /**
     * JPA lifecycle callback - executed before update
     * Automatically updates the updatedAt timestamp
     */
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }


}
