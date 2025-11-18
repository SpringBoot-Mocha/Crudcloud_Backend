package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.BaseIntegrationTest;
import com.crudzaso.CrudCloud.domain.entity.Transaction;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.domain.enums.TransactionStatus;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionMapper using Spring Boot Test
 * Tests the actual MapStruct implementation with Spring context
 * Specifically tests the relationship mapping: user.id → userId
 * 
 * Extends BaseIntegrationTest for:
 * - Automatic Spring Boot context loading
 * - PostgreSQL database configuration (test profile)
 * - Automatic transaction rollback after each test (data cleanup)
 */
@ExtendWith(SpringExtension.class)
class TransactionMapperTest extends BaseIntegrationTest {

    @Autowired
    private TransactionMapper transactionMapper;

    private Transaction transaction;
    private User user;

    @BeforeEach
    void setUp() {
        // Setup test entities
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        LocalDateTime now = LocalDateTime.now();
        transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_123456789")
                .amount(new BigDecimal("29.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void toResponse_ValidTransaction_ReturnsCorrectResponse() {
        // When
        TransactionResponse response = transactionMapper.toResponse(transaction);

        // Then
        assertNotNull(response);
        assertEquals(transaction.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId()); // Tests user.id → userId mapping
        assertEquals(transaction.getMercadopagoPaymentId(), response.getMercadopagoPaymentId());
        assertEquals(transaction.getAmount(), response.getAmount());
        assertEquals(transaction.getStatus(), response.getStatus());
        assertEquals(transaction.getCreatedAt(), response.getCreatedAt());
        assertEquals(transaction.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void toResponse_NullTransaction_ReturnsNull() {
        // When
        TransactionResponse response = transactionMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toResponse_TransactionWithNullUser_ReturnsResponseWithNullUserId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction transactionWithNullUser = Transaction.builder()
                .id(2L)
                .user(null)
                .mercadopagoPaymentId("mp_payment_987654321")
                .amount(new BigDecimal("19.99"))
                .status(TransactionStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(transactionWithNullUser);

        // Then
        assertNotNull(response);
        assertEquals(transactionWithNullUser.getId(), response.getId());
        assertNull(response.getUserId()); // userId should be null when user is null
        assertEquals(transactionWithNullUser.getMercadopagoPaymentId(), response.getMercadopagoPaymentId());
        assertEquals(transactionWithNullUser.getAmount(), response.getAmount());
        assertEquals(transactionWithNullUser.getStatus(), response.getStatus());
    }

    @Test
    void toResponse_TransactionWithNullFields_ReturnsResponseWithNullFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction transactionWithNullFields = Transaction.builder()
                .id(3L)
                .user(user)
                .mercadopagoPaymentId(null)
                .amount(new BigDecimal("9.99"))
                .status(TransactionStatus.FAILED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(transactionWithNullFields);

        // Then
        assertNotNull(response);
        assertEquals(transactionWithNullFields.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId());
        assertNull(response.getMercadopagoPaymentId()); // mercadopagoPaymentId should be null
        assertEquals(transactionWithNullFields.getAmount(), response.getAmount());
        assertEquals(transactionWithNullFields.getStatus(), response.getStatus());
    }

    @Test
    void toResponse_TransactionWithPendingStatus_ReturnsResponseWithPendingStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction pendingTransaction = Transaction.builder()
                .id(4L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_111111")
                .amount(new BigDecimal("49.99"))
                .status(TransactionStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(pendingTransaction);

        // Then
        assertNotNull(response);
        assertEquals(pendingTransaction.getId(), response.getId());
        assertEquals(TransactionStatus.PENDING, response.getStatus());
    }

    @Test
    void toResponse_TransactionWithFailedStatus_ReturnsResponseWithFailedStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction failedTransaction = Transaction.builder()
                .id(5L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_222222")
                .amount(new BigDecimal("99.99"))
                .status(TransactionStatus.FAILED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(failedTransaction);

        // Then
        assertNotNull(response);
        assertEquals(failedTransaction.getId(), response.getId());
        assertEquals(TransactionStatus.FAILED, response.getStatus());
    }

    @Test
    void toResponse_TransactionWithZeroAmount_ReturnsResponseWithZeroAmount() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction zeroAmountTransaction = Transaction.builder()
                .id(6L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_333333")
                .amount(BigDecimal.ZERO)
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(zeroAmountTransaction);

        // Then
        assertNotNull(response);
        assertEquals(zeroAmountTransaction.getId(), response.getId());
        assertEquals(BigDecimal.ZERO, response.getAmount());
    }

    @Test
    void toResponse_TransactionWithLargeAmount_ReturnsResponseWithLargeAmount() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction largeAmountTransaction = Transaction.builder()
                .id(7L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_444444")
                .amount(new BigDecimal("9999.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(largeAmountTransaction);

        // Then
        assertNotNull(response);
        assertEquals(largeAmountTransaction.getId(), response.getId());
        assertEquals(new BigDecimal("9999.99"), response.getAmount());
    }

    @Test
    void toResponse_TransactionWithSpecialCharactersInPaymentId_ReturnsCorrectResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction specialPaymentIdTransaction = Transaction.builder()
                .id(8L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_!@#$%^&*()_+-=[]{}|;:,.<>?")
                .amount(new BigDecimal("14.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(specialPaymentIdTransaction);

        // Then
        assertNotNull(response);
        assertEquals(specialPaymentIdTransaction.getId(), response.getId());
        assertEquals(specialPaymentIdTransaction.getMercadopagoPaymentId(), response.getMercadopagoPaymentId());
    }

    @Test
    void toResponseList_ValidTransactions_ReturnsCorrectResponseList() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .firstName("Another")
                .lastName("User")
                .build();

        LocalDateTime now = LocalDateTime.now();
        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .user(user2)
                .mercadopagoPaymentId("mp_payment_555555")
                .amount(new BigDecimal("39.99"))
                .status(TransactionStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Transaction transaction3 = Transaction.builder()
                .id(3L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_666666")
                .amount(new BigDecimal("59.99"))
                .status(TransactionStatus.FAILED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<Transaction> transactions = Arrays.asList(transaction, transaction2, transaction3);

        // When
        List<TransactionResponse> responses = transactionMapper.toResponseList(transactions);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        // Verify first transaction
        TransactionResponse response1 = responses.get(0);
        assertEquals(transaction.getId(), response1.getId());
        assertEquals(user.getId(), response1.getUserId());
        assertEquals(transaction.getMercadopagoPaymentId(), response1.getMercadopagoPaymentId());
        assertEquals(transaction.getAmount(), response1.getAmount());
        assertEquals(transaction.getStatus(), response1.getStatus());

        // Verify second transaction
        TransactionResponse response2 = responses.get(1);
        assertEquals(transaction2.getId(), response2.getId());
        assertEquals(user2.getId(), response2.getUserId());
        assertEquals(transaction2.getMercadopagoPaymentId(), response2.getMercadopagoPaymentId());
        assertEquals(transaction2.getAmount(), response2.getAmount());
        assertEquals(transaction2.getStatus(), response2.getStatus());

        // Verify third transaction
        TransactionResponse response3 = responses.get(2);
        assertEquals(transaction3.getId(), response3.getId());
        assertEquals(user.getId(), response3.getUserId());
        assertEquals(transaction3.getMercadopagoPaymentId(), response3.getMercadopagoPaymentId());
        assertEquals(transaction3.getAmount(), response3.getAmount());
        assertEquals(transaction3.getStatus(), response3.getStatus());
    }

    @Test
    void toResponseList_EmptyList_ReturnsEmptyList() {
        // Given
        List<Transaction> emptyList = Arrays.asList();

        // When
        List<TransactionResponse> responses = transactionMapper.toResponseList(emptyList);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void toResponseList_NullList_ReturnsNull() {
        // When
        List<TransactionResponse> responses = transactionMapper.toResponseList(null);

        // Then
        assertNull(responses);
    }

    @Test
    void toResponseList_MixedStatusTransactions_ReturnsCorrectResponses() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction approvedTransaction = Transaction.builder()
                .id(9L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_777777")
                .amount(new BigDecimal("24.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Transaction pendingTransaction = Transaction.builder()
                .id(10L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_888888")
                .amount(new BigDecimal("34.99"))
                .status(TransactionStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Transaction failedTransaction = Transaction.builder()
                .id(11L)
                .user(user)
                .mercadopagoPaymentId("mp_payment_999999")
                .amount(new BigDecimal("44.99"))
                .status(TransactionStatus.FAILED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<Transaction> mixedTransactions = Arrays.asList(approvedTransaction, pendingTransaction, failedTransaction);

        // When
        List<TransactionResponse> responses = transactionMapper.toResponseList(mixedTransactions);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        // Verify approved transaction
        TransactionResponse approvedResponse = responses.get(0);
        assertEquals(TransactionStatus.APPROVED, approvedResponse.getStatus());

        // Verify pending transaction
        TransactionResponse pendingResponse = responses.get(1);
        assertEquals(TransactionStatus.PENDING, pendingResponse.getStatus());

        // Verify failed transaction
        TransactionResponse failedResponse = responses.get(2);
        assertEquals(TransactionStatus.FAILED, failedResponse.getStatus());
    }

    @Test
    void toResponse_TransactionWithDifferentUser_ReturnsCorrectUserId() {
        // Given
        User differentUser = User.builder()
                .id(999L)
                .email("different@example.com")
                .firstName("Different")
                .lastName("User")
                .build();

        LocalDateTime now = LocalDateTime.now();
        Transaction transactionWithDifferentUser = Transaction.builder()
                .id(12L)
                .user(differentUser)
                .mercadopagoPaymentId("mp_payment_101010")
                .amount(new BigDecimal("79.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(transactionWithDifferentUser);

        // Then
        assertNotNull(response);
        assertEquals(transactionWithDifferentUser.getId(), response.getId());
        assertEquals(differentUser.getId(), response.getUserId());
        assertEquals(transactionWithDifferentUser.getMercadopagoPaymentId(), response.getMercadopagoPaymentId());
        assertEquals(transactionWithDifferentUser.getAmount(), response.getAmount());
        assertEquals(transactionWithDifferentUser.getStatus(), response.getStatus());
    }

    @Test
    void toResponse_TransactionWithEmptyPaymentId_ReturnsResponseWithEmptyPaymentId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Transaction emptyPaymentIdTransaction = Transaction.builder()
                .id(13L)
                .user(user)
                .mercadopagoPaymentId("")
                .amount(new BigDecimal("19.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        TransactionResponse response = transactionMapper.toResponse(emptyPaymentIdTransaction);

        // Then
        assertNotNull(response);
        assertEquals(emptyPaymentIdTransaction.getId(), response.getId());
        assertEquals("", response.getMercadopagoPaymentId());
        assertEquals(emptyPaymentIdTransaction.getAmount(), response.getAmount());
        assertEquals(emptyPaymentIdTransaction.getStatus(), response.getStatus());
    }
}