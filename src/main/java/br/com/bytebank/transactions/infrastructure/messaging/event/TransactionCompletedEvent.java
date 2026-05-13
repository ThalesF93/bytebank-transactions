package br.com.bytebank.transactions.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCompletedEvent(
        UUID transactionId,
        UUID accountId,
        String customerEmail,
        String customerName,
        BigDecimal amount,
        String operationType,
        LocalDateTime occurredAt
) {
}
