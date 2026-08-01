package br.com.bytebank.transactions.infrastructure.messaging.kafka.event;

import java.util.UUID;

public record FraudNotificationEvent(

        UUID transactionId,

        String name,

        String phone,

        String email
) {
}
