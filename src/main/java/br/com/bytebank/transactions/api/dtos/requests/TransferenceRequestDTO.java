package br.com.bytebank.transactions.api.dtos.requests;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenceRequestDTO(


        UUID originAccountId,


        UUID destinationAccountId,

        @Positive
        BigDecimal amount
) {
}
