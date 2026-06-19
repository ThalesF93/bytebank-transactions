package br.com.bytebank.transactions.infrastructure.api.dtos.responses;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response information after withdraw")
public record WithdrawResponseDTO(

        @Schema(description = "Account thas has been debited a value")
        UUID originId,

        @Schema(description = "The Operation type")
        OperationType type,

        @Schema(description = "The value of withdraw")
        BigDecimal amount,

        @Schema(description = "The current status")
        TransactionStatus status,

        @Schema(description = "A message for the user")
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
