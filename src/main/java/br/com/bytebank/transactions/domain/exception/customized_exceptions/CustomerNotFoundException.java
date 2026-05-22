package br.com.bytebank.transactions.domain.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CustomerNotFoundException extends DefaultException {
    public CustomerNotFoundException(String message) {
        super("CUSTOMER_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
