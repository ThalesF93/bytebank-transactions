package br.com.bytebank.transactions.api.dtos.responses;

import br.com.bytebank.transactions.domain.entity.Transaction;
import br.com.bytebank.transactions.domain.enums.OperationType;
import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Attributes existing in the Bank Statement")
public record BankStatementResponseDTO(

        @Schema(description = "The account id of the statement")
        UUID originId,

        @Schema(description = "In case of transference, it will inform the target account id")
        UUID targetId,

        @Schema(description = "The type os transaction", example = "DEPOSIT")
        OperationType type,

        @Schema(description = "What is the current status of the operation", example = "COMPLETED")
        TransactionStatus status,

        @Schema(description = "The value of the specific transaction")
        BigDecimal amount,

        @Schema(description = "The date and time when it happens")
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
