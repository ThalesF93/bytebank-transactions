package br.com.bytebank.transactions.infrastructure.exception.customized_exceptions;

import br.com.bytebank.transactions.infrastructure.exception.default_exception.DefaultException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TransactionException extends DefaultException {
    public TransactionException(UUID uuid) {
        super("TRANSACTION_NOT_FOUND", String.format("Transaction with id %s not found", uuid), HttpStatus.NOT_FOUND);
    }
}
