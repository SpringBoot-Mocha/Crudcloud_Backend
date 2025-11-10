package com.crudzaso.CrudCloud.dto.response;

import com.crudzaso.CrudCloud.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction response
 * Returned when fetching payment transaction information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;

    private Long userId;

    private String mercadopagoPaymentId;

    private BigDecimal amount;

    /**
     * Current status of the transaction
     * PENDING, APPROVED, FAILED
     */
    private TransactionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
