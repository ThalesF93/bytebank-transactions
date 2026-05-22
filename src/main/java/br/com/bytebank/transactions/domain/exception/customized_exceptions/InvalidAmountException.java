package br.com.bytebank.transactions.domain.exception.customized_exceptions;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
