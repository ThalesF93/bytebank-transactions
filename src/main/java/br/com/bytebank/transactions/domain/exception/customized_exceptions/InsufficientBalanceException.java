package br.com.bytebank.transactions.domain.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends DefaultException {
    public InsufficientBalanceException(String message) {
        super("INSUFFICIENT_BALANCE", message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
