package br.com.bytebank.transactions.infrastructure.messaging.kafka.event;

import br.com.bytebank.transactions.domain.enums.FraudScore;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FraudScoreEvent(

        @NotNull
        UUID transactionId,

        @NotNull
        FraudScore score
) {
}
