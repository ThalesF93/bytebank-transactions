package br.com.bytebank.transactions.api.dtos.responses;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawResponseDTO(
        UUID originId,
        OperationType type,
        BigDecimal amount,
        TransactionStatus status,
        String message
) {
    public static WithdrawResponseDTO response(Transaction transaction){
        return new WithdrawResponseDTO(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                "Withdraw successfully done!"
        );
    }
}
