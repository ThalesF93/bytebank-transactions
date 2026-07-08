package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends DefaultException {
    public ServiceUnavailableException(String message) {
        super("SERVICE_UNAVAILABLE", message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
