package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class SameAccountException extends DefaultException {
    public SameAccountException(String message) {
        super("IDENTICAL_ACCOUNTS", message, HttpStatus.CONFLICT);
    }
}

