package com.crudzaso.CrudCloud.domain.entity;

import com.crudzaso.CrudCloud.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a payment transaction
 *
 * Maps to the 'transactions' table and stores payment information
 * from Mercado Pago or other payment providers. Tracks transaction status
 * and links to the user who made the payment.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_user_id", columnList = "user_id"),
    @Index(name = "idx_transactions_mercadopago_payment_id", columnList = "mercadopago_payment_id"),
    @Index(name = "idx_transactions_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Payment ID from Mercado Pago
     * Used to track transactions from the payment provider
     * Unique identifier in Mercado Pago system
     * Required field
     */
    @Column(nullable = false, unique = true)
    private String mercadopagoPaymentId;

    /**
     * Transaction amount in currency units
     * Stored as BigDecimal for financial accuracy
     * Required field
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Current status of the transaction
     * PENDING, APPROVED, FAILED
     * Required field
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback - executed before first insert
     * Automatically sets createdAt and updatedAt to current timestamp
     * Sets initial status to PENDING if not provided
     */
    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null){
            status = TransactionStatus.PENDING;
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
