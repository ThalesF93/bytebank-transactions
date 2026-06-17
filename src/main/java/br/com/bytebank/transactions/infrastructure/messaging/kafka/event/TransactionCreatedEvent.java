package br.com.bytebank.transactions.infrastructure.messaging.kafka.event;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID transactionId,
        UUID originAccountId,
        UUID targetAccountId,
        String type,
        String status,
        BigDecimal amount,
        LocalDateTime dateTime,
        String description
) {
    public static TransactionCreatedEvent from(Transaction transaction) {
        return new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getOriginAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType().toString(),
                transaction.getStatus().toString(),
                transaction.getAmount(),
                transaction.getDateTime(),
                transaction.getDescription()
        );
    }
}