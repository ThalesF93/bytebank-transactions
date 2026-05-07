package br.com.bytebank.transactions.domain.exception;

public class SameAccountException extends RuntimeException {
    public SameAccountException(String message) {
        super(message);
    }
}

