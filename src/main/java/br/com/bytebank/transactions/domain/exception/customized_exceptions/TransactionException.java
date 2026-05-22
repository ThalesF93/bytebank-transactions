package br.com.bytebank.transactions.domain.exception.customized_exceptions;

public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }
}
