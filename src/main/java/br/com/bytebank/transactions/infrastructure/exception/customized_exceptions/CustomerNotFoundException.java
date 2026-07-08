package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends DefaultException {
    public CustomerNotFoundException(String message) {
        super("CUSTOMER_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
