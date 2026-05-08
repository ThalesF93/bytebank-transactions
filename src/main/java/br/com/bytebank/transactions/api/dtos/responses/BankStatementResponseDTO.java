package br.com.bytebank.transactions.api.dtos.responses;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BankStatementResponseDTO(
        UUID originId,
        UUID targetId,
        OperationType type,
        TransactionStatus status,
        BigDecimal amount,
        LocalDateTime dateTime
) {

    public static BankStatementResponseDTO generateStatement(Transaction transaction){
        return new BankStatementResponseDTO(
                transaction.getOriginAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getDateTime()
        );
    };
}
