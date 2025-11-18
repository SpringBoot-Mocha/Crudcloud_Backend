package com.crudzaso.CrudCloud.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity representing a CrudCloud user or organization
 * Maps to the 'Users' table with indexes on email and created_at for performance optimization.
 * Supports both individual users and organization accounts.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_created_at",  columnList = "created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String name;

    /**
     * Flag indicating if this account represents an organization
     * Default value: false (individual user)
     * Affects billing and feature availability
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isOrganization = false;

    /**
     * Timestamp when the user account was created
     * Automatically set on first persistence
     * Immutable - cannot be updated after creation
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated
     * Automatically updated on every modification
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - executed before first insert
     * Automatically sets createdAt and updatedAt to current timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback - executed before update
     * Automatically updates the updatedAt timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
