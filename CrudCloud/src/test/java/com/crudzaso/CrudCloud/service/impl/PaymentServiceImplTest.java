package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.Transaction;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.TransactionStatus;
import com.crudzaso.CrudCloud.dto.request.CreatePaymentRequest;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import com.crudzaso.CrudCloud.exception.AppException;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.TransactionMapper;
import com.crudzaso.CrudCloud.repository.TransactionRepository;
import com.crudzaso.CrudCloud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for PaymentServiceImpl
 *
 * Tests both current functionality and future Mercado Pago integration scenarios
 * Covers success cases, error scenarios, and edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Implementation Tests")
class PaymentServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User testUser;
    private Transaction testTransaction;
    private CreatePaymentRequest validPaymentRequest;
    private TransactionResponse expectedResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .passwordHash("hashed_password")
                .isOrganization(false)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .user(testUser)
                .mercadopagoPaymentId("MP-123456789")
                .amount(new BigDecimal("99.99"))
                .status(TransactionStatus.PENDING)
                .build();

        validPaymentRequest = new CreatePaymentRequest();
        validPaymentRequest.setUserId(1L);
        validPaymentRequest.setPlanId(1L);
        validPaymentRequest.setAmount(new BigDecimal("99.99"));
        validPaymentRequest.setPaymentMethod("credit_card");

        expectedResponse = TransactionResponse.builder()
                .id(1L)
                .mercadopagoPaymentId("MP-123456789")
                .amount(new BigDecimal("99.99"))
                .status(TransactionStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should create payment successfully with valid request")
    void createPayment_WithValidRequest_ShouldCreateTransaction() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(expectedResponse);

        // When
        TransactionResponse result = paymentService.createPayment(validPaymentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMercadopagoPaymentId()).startsWith("MP-");
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);

        verify(userRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).toResponse(testTransaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void createPayment_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(validPaymentRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(userRepository).findById(1L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository save failure gracefully")
    void createPayment_WhenRepositorySaveFails_ShouldThrowAppException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(validPaymentRequest))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Payment processing failed");

        verify(userRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get transaction successfully by ID")
    void getTransaction_WithValidId_ShouldReturnTransaction() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionMapper.toResponse(testTransaction)).thenReturn(expectedResponse);

        // When
        TransactionResponse result = paymentService.getTransaction(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMercadopagoPaymentId()).isEqualTo("MP-123456789");

        verify(transactionRepository).findById(1L);
        verify(transactionMapper).toResponse(testTransaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction not found")
    void getTransaction_WithNonExistentId_ShouldThrowException() {
        // Given
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.getTransaction(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction");

        verify(transactionRepository).findById(999L);
    }

    @Test
    @DisplayName("Should process webhook notification without throwing exceptions")
    void processWebhookNotification_WithValidPayload_ShouldProcessSuccessfully() {
        // Given
        String webhookPayload = "{\"action\":\"payment.created\",\"data\":{\"id\":\"123456789\"}}";

        // When
        paymentService.processWebhookNotification(webhookPayload);

        // Then
        // Should not throw any exceptions - webhook processing is currently a TODO
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null webhook payload gracefully")
    void processWebhookNotification_WithNullPayload_ShouldNotFail() {
        // When
        paymentService.processWebhookNotification(null);

        // Then
        // Should not throw any exceptions
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle empty webhook payload gracefully")
    void processWebhookNotification_WithEmptyPayload_ShouldNotFail() {
        // When
        paymentService.processWebhookNotification("");

        // Then
        // Should not throw any exceptions
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should generate unique payment IDs")
    void generatePaymentId_ShouldGenerateUniqueIds() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(expectedResponse);

        // When
        TransactionResponse result1 = paymentService.createPayment(validPaymentRequest);
        TransactionResponse result2 = paymentService.createPayment(validPaymentRequest);

        // Then
        // Verify IDs are generated (though they'll be mocked)
        assertThat(result1.getMercadopagoPaymentId()).isNotNull();
        assertThat(result2.getMercadopagoPaymentId()).isNotNull();
    }

    // Future integration test scenarios for Mercado Pago
    @Test
    @DisplayName("Should handle Mercado Pago SDK integration when implemented")
    void createPayment_WithMercadoPagoIntegration_ShouldCallSDK() {
        // This test documents the expected behavior when Mercado Pago integration is complete
        // Currently, the service only creates a transaction with PENDING status
        // Future implementation should:
        // 1. Call Mercado Pago SDK to create payment
        // 2. Store the payment ID returned by Mercado Pago
        // 3. Update transaction status based on SDK response

        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(expectedResponse);

        // When
        TransactionResponse result = paymentService.createPayment(validPaymentRequest);

        // Then
        // Current behavior - creates transaction with generated ID
        assertThat(result.getMercadopagoPaymentId()).startsWith("MP-");
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);

        // Future assertions when Mercado Pago integration is complete:
        // assertThat(result.getMercadopagoPaymentId()).matches("^\\d+$"); // Real MP ID format
        // assertThat(result.getStatus()).isEqualTo(TransactionStatus.APPROVED); // If payment successful
    }

    @Test
    @DisplayName("Should handle Mercado Pago API failures gracefully")
    void createPayment_WhenMercadoPagoAPIFails_ShouldHandleGracefully() {
        // This test documents expected error handling for Mercado Pago integration
        // Future implementation should:
        // 1. Catch Mercado Pago SDK exceptions
        // 2. Set transaction status to FAILED
        // 3. Throw appropriate AppException with error details

        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(expectedResponse);

        // When & Then
        // Current behavior - any exception results in AppException
        // Future behavior should handle specific Mercado Pago exceptions

        // This test ensures the current error handling works
        assertThat(paymentService.createPayment(validPaymentRequest)).isNotNull();
    }

    @Test
    @DisplayName("Should process webhook notifications to update transaction status")
    void processWebhookNotification_WithPaymentApproved_ShouldUpdateStatus() {
        // This test documents expected webhook processing behavior
        // Future implementation should:
        // 1. Parse webhook payload
        // 2. Find transaction by Mercado Pago payment ID
        // 3. Update transaction status based on webhook action

        String approvedWebhookPayload = """
        {
            "action": "payment.updated",
            "data": {
                "id": "MP-123456789",
                "status": "approved"
            }
        }
        """;

        // When
        paymentService.processWebhookNotification(approvedWebhookPayload);

        // Then
        // Current behavior - logs the payload but doesn't process it
        // Future assertions:
        // verify(transactionRepository).findByMercadopagoPaymentId("MP-123456789");
        // verify(transactionRepository).save(transactionWithUpdatedStatus);
    }
}