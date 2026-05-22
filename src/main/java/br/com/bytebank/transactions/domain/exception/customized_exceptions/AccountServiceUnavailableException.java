package br.com.bytebank.transactions.domain.exception.customized_exceptions;

public class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException(String message) {
        super(message);
    }
}
