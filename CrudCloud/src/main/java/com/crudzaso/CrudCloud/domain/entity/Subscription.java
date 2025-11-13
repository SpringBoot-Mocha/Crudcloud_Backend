package com.crudzaso.CrudCloud.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Subscription entity representing a user's subscription to a plan
 *
 * Maps to the 'subscriptions' table and links users to plans.
 * Tracks subscription start and end dates, and whether it's currently active.
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_user_id", columnList = "user_id"),
        @Index(name = "idx_subscriptions_plan_id", columnList = "plan_id"),
        @Index(name = "idx_subscriptions_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Reference to the user who owns this subscription
     * Many-to-One relationship with cascading delete
     * Required field
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Reference to the plan subscribed to
     * Many-to-One relationship
     * Required field
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    /**
     * Whether this subscription is currently active
     * Used for quick filtering of active subscriptions
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Timestamp when the subscription was created
     * Automatically set on first persistence
     * Immutable - cannot be updated after creation
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the subscription was last updated
     * Automatically updated on every modification
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - executed before first insert
     * Automatically sets createdAt and updatedAt to current timestamp
     */
    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null){
            isActive = true;
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
