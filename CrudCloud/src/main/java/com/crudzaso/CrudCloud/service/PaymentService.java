package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.request.CreatePaymentRequest;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import com.crudzaso.CrudCloud.exception.AppException;

/**
 * Service interface for payment operations
 * Handles payment processing and transaction management
 */
public interface PaymentService {

    /**
     * Create a new payment transaction
     * @param request the payment creation request
     * @return the transaction response
     * @throws ResourceNotFoundException if user not found
     * @throws AppException if payment processing fails
     */
    TransactionResponse createPayment(CreatePaymentRequest request);

    /**
     * Get a transaction by ID
     * @param id the transaction ID
     * @return the transaction response
     * @throws ResourceNotFoundException if transaction not found
     */
    TransactionResponse getTransaction(Long id);

    /**
     * Process webhook notification from Mercado Pago
     * Updates transaction status based on payment provider response
     * @param payload the webhook payload
     */
    void processWebhookNotification(String payload);
}
