package br.com.bytebank.transactions.api.dtos.responses;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response information after deposit")
public record DepositResponseDTO(

        @Schema(description = "Account thas has been credited a value")
        UUID originId,

        @Schema(description = "The Operation type", example = "Deposit")
        OperationType type,

        @Schema(description = "The value of deposit")
        BigDecimal amount,

        @Schema(description = "The current status", example = "PENDING")
        TransactionStatus status,

        @Schema(description = "A message for the user", example = "Deposited successfully")
        String message
) {

    public static DepositResponseDTO response(Transaction transaction){
        return new DepositResponseDTO(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                "Deposited successfully!"
        );
    }
}
