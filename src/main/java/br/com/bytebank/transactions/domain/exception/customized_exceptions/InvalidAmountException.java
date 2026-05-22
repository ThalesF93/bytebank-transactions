package br.com.bytebank.transactions.domain.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class InvalidAmountException extends DefaultException {
    public InvalidAmountException(String message) {
        super("INVALID_AMOUNT", message, HttpStatus.CONFLICT);
    }
}
