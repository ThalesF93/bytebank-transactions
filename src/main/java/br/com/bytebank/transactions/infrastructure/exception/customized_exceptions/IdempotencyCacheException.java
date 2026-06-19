package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;


import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class IdempotencyCacheException extends DefaultException {

    public enum Operation { SERIALIZE, DESERIALIZE }

    public IdempotencyCacheException(Operation operation) {
        super(
                "IDEMPOTENCY_CACHE_" + operation.name() + "_ERROR",
                "Failed to " + operation.name().toLowerCase() + " idempotency response",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

