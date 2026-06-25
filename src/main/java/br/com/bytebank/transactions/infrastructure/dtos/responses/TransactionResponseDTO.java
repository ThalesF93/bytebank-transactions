package br.com.bytebank.transactions.infrastructure.dtos.responses;



import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response information after transference")
public record TransactionResponseDTO(

        @Schema(description = "Source account")
        UUID originId,

        @Schema(description = "Target Account")
        UUID targetId,

        @Schema(description = "The type of the operation", example = "TRANSFERENCE")
        OperationType type,

        @Schema(description = "The current status")
        TransactionStatus status,

        @Schema(description = "The value of withdraw")
        BigDecimal amount,

        @Schema(description = "A message for the user")
        String message

) implements Serializable {
    public static TransactionResponseDTO transactionResponseDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getOriginAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                "Transaction completed successfully"


        );
    }

    public static TransactionResponseDTO transactionPendingResponse(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getOriginAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                "Transaction is pending, please wait."


        );
    }


}

