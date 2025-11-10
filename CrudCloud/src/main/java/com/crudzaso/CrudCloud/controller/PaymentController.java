package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.dto.request.CreatePaymentRequest;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import com.crudzaso.CrudCloud.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment processing endpoints.
 *
 * Provides endpoints for processing payments and handling payment webhooks.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and webhook endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a new payment transaction.
     *
     * @param request payment creation request
     * @return created transaction response with 201 status
     */
    @PostMapping
    @Operation(summary = "Create payment", description = "Creates a new payment transaction")
    public ResponseEntity<TransactionResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for user: {}", request.getUserId());
        TransactionResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get payment transaction details.
     *
     * @param id the transaction ID
     * @return transaction details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment details", description = "Retrieves payment transaction details")
    public ResponseEntity<TransactionResponse> getPayment(@PathVariable Long id) {
        log.info("Getting payment with ID: {}", id);
        TransactionResponse response = paymentService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Handle payment gateway webhook notifications.
     *
     * Receives notifications from Mercado Pago/payment provider and updates transaction status.
     * Always returns 200 OK to acknowledge receipt regardless of processing result.
     *
     * @param payload webhook payload from payment provider
     * @return 200 OK response
     */
    @PostMapping("/webhook")
    @Operation(summary = "Payment webhook", description = "Receives payment status updates from payment provider")
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestBody String payload) {
        log.info("Received payment webhook notification");
        try {
            paymentService.processWebhookNotification(payload);
        } catch (Exception ex) {
            log.error("Error processing webhook: {}", ex.getMessage());
        }
        // Always return 200 OK to prevent webhook retries
        return ResponseEntity.ok().build();
    }
}