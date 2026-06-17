package br.com.bytebank.transactions.infrastructure.messaging.kafka.publisher;

import br.com.bytebank.transactions.infrastructure.messaging.kafka.producer.TransactionEventProducer;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.TransactionCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TransactionKafkaPublisher {

    private final TransactionEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedDomainEvent event) {
        producer.publish(event.toKafkaEvent());
    }
}
