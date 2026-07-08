package br.com.bytebank.transactions.infrastructure.dtos.requests;

import br.com.bytebank.transactions.domain.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTransactionStatusRequestDTO(

        @NotNull
        TransactionStatus status
) {
}
