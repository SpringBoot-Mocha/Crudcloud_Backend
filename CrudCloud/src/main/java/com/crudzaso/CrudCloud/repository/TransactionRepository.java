package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity
 * Provides database operations for Transaction records
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific user
     * @param userId the user ID to search for
     * @return List of transactions made by the user
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Find a transaction by Mercado Pago payment ID
     * @param mercadopagoPaymentId the Mercado Pago payment ID
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findByMercadopagoPaymentId(String mercadopagoPaymentId);
}
