package br.com.bytebank.transactions.domain.exception.customized_exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
