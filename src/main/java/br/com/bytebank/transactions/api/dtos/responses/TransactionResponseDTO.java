package br.com.bytebank.transactions.api.dtos.responses;



import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID originId,
        UUID targetId,
        OperationType type,
        TransactionStatus status,
        BigDecimal amount,
        String message

) {
    public static TransactionResponseDTO transferenceCompletedResponse(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getOriginAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                "Transference completed successfully"


        );
    }

    public static TransactionResponseDTO transferencePendingResponse(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getOriginAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                "Transference is pending, please wait."


        );
    }


}

