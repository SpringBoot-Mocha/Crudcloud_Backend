package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.domain.enums.TransactionStatus;
import com.crudzaso.CrudCloud.dto.request.CreatePaymentRequest;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import com.crudzaso.CrudCloud.service.PaymentService;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for PaymentController.
 *
 * Tests payment processing, transaction retrieval, and webhook handling.
 */
public class PaymentControllerTest extends BaseControllerTest {

    @MockBean
    private PaymentService paymentService;

    @Test
    public void testCreatePaymentSuccess() throws Exception {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setUserId(1L);
        request.setPlanId(2L);
        request.setAmount(new BigDecimal("29.99"));
        request.setPaymentMethod("credit_card");

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .userId(1L)
                .mercadopagoPaymentId("MP-12345678")
                .amount(new BigDecimal("29.99"))
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.amount").value(29.99))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testCreatePaymentInvalidUser() throws Exception {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setUserId(999L);
        request.setPlanId(2L);
        request.setAmount(new BigDecimal("29.99"));
        request.setPaymentMethod("credit_card");

        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenThrow(new ResourceNotFoundException("User", 999L));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testCreatePaymentValidationFail() throws Exception {
        // Arrange - amount is 0 (invalid)
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setUserId(1L);
        request.setPlanId(2L);
        request.setAmount(BigDecimal.ZERO);
        request.setPaymentMethod("credit_card");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    public void testGetTransactionSuccess() throws Exception {
        // Arrange
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .userId(1L)
                .mercadopagoPaymentId("MP-12345678")
                .amount(new BigDecimal("29.99"))
                .status(TransactionStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentService.getTransaction(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.mercadopagoPaymentId").value("MP-12345678"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void testGetTransactionNotFound() throws Exception {
        // Arrange
        when(paymentService.getTransaction(999L))
                .thenThrow(new ResourceNotFoundException("Transaction", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    public void testWebhookNotificationSuccess() throws Exception {
        // Arrange
        String webhookPayload = "{\"action\":\"payment.updated\",\"data\":{\"id\":\"MP-12345678\"," +
                "\"status\":\"approved\"},\"timestamp\":\"2024-11-10T10:30:00Z\"}";

        doNothing().when(paymentService).processWebhookNotification(anyString());

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
                .andExpect(status().isOk());
    }

    @Test
    public void testWebhookNotificationErrorHandling() throws Exception {
        // Arrange
        String webhookPayload = "{\"action\":\"payment.updated\",\"data\":{\"id\":\"MP-invalid\"}}";

        doThrow(new RuntimeException("Invalid webhook payload"))
                .when(paymentService).processWebhookNotification(anyString());

        // Act & Assert - Always returns 200 to prevent webhook retries
        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookPayload))
                .andExpect(status().isOk());
    }

    @Test
    public void testWebhookNotificationEmptyPayload() throws Exception {
        // Arrange
        String emptyPayload = "{}";

        doNothing().when(paymentService).processWebhookNotification(anyString());

        // Act & Assert - Always returns 200 even with empty payload
        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyPayload))
                .andExpect(status().isOk());
    }
}
