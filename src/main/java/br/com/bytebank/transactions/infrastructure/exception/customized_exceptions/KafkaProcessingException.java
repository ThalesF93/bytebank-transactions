package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class KafkaProcessingException extends DefaultException {
    public KafkaProcessingException() {
        super("KAFKA_EXCEPTION","Failed to process transaction event", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
