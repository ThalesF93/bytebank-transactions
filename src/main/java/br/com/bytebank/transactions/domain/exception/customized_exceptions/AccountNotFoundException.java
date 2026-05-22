package br.com.bytebank.transactions.domain.exception.customized_exceptions;

import br.com.bytebank.transactions.domain.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AccountNotFoundException extends DefaultException {
    public AccountNotFoundException(UUID id) {
        super("ACCOUNT_NOT_FOUND", String.format("Account with id %s not found", id), HttpStatus.NOT_FOUND);
    }
}
