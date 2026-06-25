package br.com.bytebank.transactions.infrastructure.messaging.kafka.consumer;

import br.com.bytebank.transactions.application.usecase.FraudCallBackUseCase;
import br.com.bytebank.transactions.infrastructure.exception.customized_exceptions.KafkaProcessingException;
import br.com.bytebank.transactions.infrastructure.messaging.kafka.event.FraudScoreEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudServiceConsumer {

    private final FraudCallBackUseCase fraudCallBackUseCase;

    @KafkaListener(topics = "score.response", groupId = "fraud-group", containerFactory = "KafkaListenerContainerFactory")
    public void consume(@Payload FraudScoreEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset,
                        Acknowledgment ack){

        log.info("Event received from Kafka. Partition={} offset={} transactionId={}", partition, offset, event.transactionId());

        try {
            fraudCallBackUseCase.execute(event);
            ack.acknowledge();
            log.info("Operation persisted from Kafka event. transactionId={}", event.transactionId());
        } catch (Exception e) {
            throw new KafkaProcessingException();
        }
    }
}
