package br.com.bytebank.transactions.infrastructure.messaging.kafka.publisher;

import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudNotificationEvent;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.producer.FraudNotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FraudNotificationPublisher {

    private final FraudNotificationProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFraudNotification(FraudNotificationEvent event){
        producer.publish(event);
    }
}
