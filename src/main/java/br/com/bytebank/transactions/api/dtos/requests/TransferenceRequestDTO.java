package br.com.bytebank.transactions.api.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenceRequestDTO(

        @NotNull
        UUID originAccountId,

        @NotNull
        UUID destinationAccountId,

        @NotNull
        @Positive
        BigDecimal amount
) implements AccountOperationRequest {
        @Override
        public UUID accountId() {
                return originAccountId;
        }
}
