package br.com.bytebank.transactions.infrastructure.messaging.kafka.event;

import br.com.bytebank.transactions.domain.entity.Transaction;

public record TransactionCreatedDomainEvent(Transaction transaction) {
    public TransactionCreatedEvent toKafkaEvent() {
        return TransactionCreatedEvent.from(transaction);
    }
}
