package br.com.bytebank.transactions.infrastructure.dtos.requests;

import br.com.bytebank.transactions.domain.enums.FraudScore;

import java.util.UUID;

public record FraudServiceRequestDTO(
        UUID transactionID,

        FraudScore score
) {
}
