package br.com.bytebank.transactions.infrastructure.dtos.requests;

import br.com.bytebank.transactions.domain.enums.FraudScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FraudServiceRequestDTO(

        @NotNull
        UUID transactionID,

        @NotBlank
        FraudScore score
) {
}
