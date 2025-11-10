package com.crudzaso.CrudCloud.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Credential entity representing database access credentials
 *
 * Maps to the 'credentials' table and stores encrypted credentials
 * for accessing database instances. Password is encrypted using AES-256.
 */
@Entity
@Table(name = "credentials", indexes = {
        @Index(name = "idx_credentials_database_instance_id", columnList = "database_instance_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the database instance these credentials are for
     * Many-to-One relationship
     * Required field
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "database_instance_id", nullable = false)
    private DatabaseInstance databaseInstance;

    @Column(nullable = false)
    private String username;

    /**
     * Encrypted password for database access
     * IMPORTANT: This is encrypted using AES-256, NOT hashed
     * Encryption/decryption must be handled in service layer
     * Required field
     */
    @Column(nullable = false)
    private String encryptedPassword;

    /**
     * Timestamp when the credential was created
     * Automatically set on first persistence
     * Immutable - cannot be updated after creation
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA lifecycle callback - executed before first insert
     * Automatically sets createdAt to current timestamp
     */
    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
