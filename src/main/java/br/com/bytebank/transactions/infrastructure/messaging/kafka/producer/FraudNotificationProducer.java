package br.com.bytebank.transactions.infrastructure.messaging.kafka.producer;

import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudNotificationProducer {

    private static final String TOPIC = "fraud.notification";
    private final KafkaTemplate<String, FraudNotificationEvent> kafkaTemplate;

    public void publish(FraudNotificationEvent event){

        var message = kafkaTemplate.send(TOPIC, event.transactionId().toString(), event);

        message.whenComplete((result, ex)->
        {
            if (ex != null){
                log.error("Event publish failed: {}", ex.getMessage());
            }
            else {
                log.info("Event published to Kafka → topic={} partition={} offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

}
