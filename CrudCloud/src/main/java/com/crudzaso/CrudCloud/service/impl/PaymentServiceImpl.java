package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Transaction;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.TransactionStatus;
import com.crudzaso.CrudCloud.dto.request.CreatePaymentRequest;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.repository.TransactionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import com.crudzaso.CrudCloud.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of PaymentService
 * Handles payment processing and transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public TransactionResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for user ID: {} with amount: {}",
                request.getUserId(), request.getAmount());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        try {
            // TODO: Integrate with Mercado Pago SDK
            // For now, create transaction with PENDING status
            String mercadopagoPaymentId = generatePaymentId();

            Transaction transaction = Transaction.builder()
                    .user(user)
                    .mercadopagoPaymentId(mercadopagoPaymentId)
                    .amount(request.getAmount())
                    .status(TransactionStatus.PENDING)
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

            return modelMapper.map(savedTransaction, TransactionResponse.class);

        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage());
            throw new AppException("Payment processing failed", "PAYMENT_ERROR");
        }
    }

    @Override
    public TransactionResponse getTransaction(Long id) {
        log.debug("Fetching transaction with ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        return modelMapper.map(transaction, TransactionResponse.class);
    }

    @Override
    public void processWebhookNotification(String payload) {

        log.info("Processing webhook notification from Mercado Pago");
        // TODO: Implement webhook processing logic
        log.debug("Webhook payload: {}", payload);
    }

    private String generatePaymentId() {
        return "MP-" + UUID.randomUUID().toString();
    }
}
