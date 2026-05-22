package br.com.bytebank.transactions.domain.exception.customized_exceptions;

public class SameAccountException extends RuntimeException {
    public SameAccountException(String message) {
        super(message);
    }
}

